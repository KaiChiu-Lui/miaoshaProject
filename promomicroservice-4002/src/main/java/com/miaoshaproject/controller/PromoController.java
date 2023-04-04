package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.PromoVO;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.impl.PromoServiceImpl;
import com.miaoshaproject.service.model.PromoModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
@RequestMapping("/promo")
public class PromoController extends BaseController{
    
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PromoServiceImpl promoService;

    @Autowired
    private MqProducer mqProducer;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getPromoByItemId(@RequestParam("id") Integer itemId){
        PromoModel promoModel = promoService.getPromoByItemId(itemId);
        PromoVO promoVO = convertFromModel(promoModel);
        if(promoVO==null) return CommonReturnType.create(null);
        promoVO.setStartDateString(simpleDateFormat.format(promoModel.getStartDate()));
        promoVO.setEndDateString(simpleDateFormat.format(promoModel.getEndDate()));
        CommonReturnType commonReturnType = CommonReturnType.create(promoVO);
        return commonReturnType;
    }

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listPromo(){
        List<PromoModel> promoModels = promoService.listPromo();
        ArrayList<PromoVO> promoVOs = new ArrayList<>();
        for(PromoModel promoModel : promoModels){
            System.out.println(promoModel);
            PromoVO promoVO = convertFromModel(promoModel);
            promoVO.setStartDateString(simpleDateFormat.format(promoModel.getStartDate()));
            promoVO.setEndDateString(simpleDateFormat.format(promoModel.getEndDate()));
            promoVOs.add(promoVO);
        }
        // System.out.println(promoVOs);
        return CommonReturnType.create(promoVOs);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public CommonReturnType deletePromo(@RequestParam("id") int promoId){
        return CommonReturnType.create(promoService.deletePromo(promoId));
    }

    @RequestMapping("/insert")
    @ResponseBody
    public CommonReturnType insertPromo(@RequestParam("startTime") String startTime,
                                        @RequestParam("endTime") String endTime,
                                        @RequestParam("promoName") String promoName,
                                        @RequestParam("itemId") Integer itemId,
                                        @RequestParam("promoItemPrice") BigDecimal promoItemPrice) throws Exception{
        PromoVO promoVO = new PromoVO();
        promoVO.setPromoName(promoName);
        promoVO.setItemId(itemId);
        promoVO.setPromoItemPrice(promoItemPrice);
        Date startDate = simpleDateFormat.parse(startTime);
        Date endDate = simpleDateFormat.parse(endTime);
        promoVO.setStartDate(startDate);
        promoVO.setEndDate(endDate);
        System.out.println(promoVO);
        PromoModel promoModel = convertFromVO(promoVO);
        int result = promoService.insertPromo(promoModel);
        return CommonReturnType.create(result);
    }

    //缓存预热
    @RequestMapping(value = "/publishpromo",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType publishpromo(@RequestParam(name = "id") Integer promoId) throws BusinessException {
        promoService.publishPromo(promoId);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/decreaseStock")
    @ResponseBody
    public CommonReturnType decreaseStock(@RequestParam("itemId") Integer itemId,
                                           @RequestParam("amount") Integer amount) throws BusinessException{
        System.out.println("promoController.decreaseStock");
        if(redisTemplate.opsForValue().get("promo_item_stock_"+itemId)==null){
            System.out.println("没有缓存库存,抛异常");
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH,"没有缓存库存,无法下单");
        }
        long result = redisTemplate.opsForValue().increment("promo_item_stock_"+itemId,amount.intValue() * -1);
        if(result >0){
            //更新库存成功
            //并异步向数据库扣减库存
            return CommonReturnType.create(null);
        }else if(result == 0){
            //打上库存已售罄的标识
            redisTemplate.opsForValue().set("promo_item_stock_invalid_"+itemId,"true");
            //更新库存成功
            return CommonReturnType.create(null);
        }else{
            //更新库存失败
            this.increaseStock(itemId,amount);
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH,"库存扣减操作失败");
        }
    }

    @RequestMapping("/increaseStock")
    @ResponseBody
    public CommonReturnType increaseStock(@RequestParam("itemId") Integer itemId,
                                          @RequestParam("amount") Integer amount) throws BusinessException{
        Long result = redisTemplate.opsForValue().decrement("promo_item_stock_"+itemId,amount.intValue() * -1);
        if(result==null) throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"库存回滚失败,原因未知");
        return CommonReturnType.create(result);
    }

    @RequestMapping(value = "/generatetoken",method = {RequestMethod.POST},consumes={CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType generatetoken(@CookieValue(value = "is_login",required = false) String uid,
                                          @RequestParam(name="itemId")Integer itemId,
                                          @RequestParam(name="promoId")Integer promoId) throws BusinessException {
        //1.根据token获取用户信息
        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录,无法下单");
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(uid);

        //2.获取秒杀访问令牌
        String promoToken = promoService.generateSecondKillToken(promoId,itemId,userVO.getId());

        if(promoToken == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"生成令牌失败");
        }
        //返回对应的结果
        return CommonReturnType.create(promoToken);
    }

    public PromoVO convertFromModel(PromoModel promoModel){
        PromoVO promoVO = new PromoVO();
        if(promoModel==null) return null;
        BeanUtils.copyProperties(promoModel,promoVO);
        return promoVO;
    }

    public PromoModel convertFromVO(PromoVO promoVO){
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoVO,promoModel);
        return promoModel;
    }

    @RequestMapping("/testMQ")
    @ResponseBody
    public CommonReturnType testMQ(){
        Integer itemId = 7;
        Integer amount = 7;
        return CommonReturnType.create(mqProducer.asyncReduceStock(itemId,amount));
    }


}
