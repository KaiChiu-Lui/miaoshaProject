package com.miaoshaproject.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.mq.MqProducer;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.impl.PaymentServiceImpl;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.nimbus.NimbusStyle;

import static com.miaoshaproject.controller.BaseController.CONTENT_TYPE_FORMED;

@Controller
@RequestMapping("/payment")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class PaymentController extends BaseController{

    @Autowired
    public PaymentServiceImpl paymentService;

    // @Autowired
    // public HttpServletRequest httpServletRequest;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public UserFeignClient userFeignClient;

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private MqProducer mqProducer;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@CookieValue(value = "is_login",required = false) String uid,
                                        @RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId",required = false) Integer promoId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name="promoToken",required = false) String promoToken) throws BusinessException {

        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(uid);

        //再去完成对应的下单事务型消息机制
        if(promoId==null) paymentService.createOrder(userVO.getId(), itemId, amount);
        else{
            //对商品售罄的判断改成对秒杀令牌进行校验
            // if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            //     throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
            // }
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_"+promoId+"_userid_"+userVO.getId()+"_itemid_"+itemId);
            if(inRedisPromoToken == null){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }
            if(!org.apache.commons.lang3.StringUtils.equals(promoToken,inRedisPromoToken)){
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"秒杀令牌校验失败");
            }

            //加入库存流水init状态
            String stockLogId = paymentService.initStockLog(itemId,amount);
            if(!mqProducer.transactionAsyncReduceStock(userVO.getId(),itemId,promoId,amount,stockLogId)){
                throw new BusinessException(EmBusinessError.UNKNOWN_ERROR,"MQ执行本地事务失败");
            }
        }
        return CommonReturnType.create(null);
    }

    @RequestMapping("/islogin")
    @ResponseBody
    public CommonReturnType testLogin() throws BusinessException{
        CommonReturnType commonReturnType = userFeignClient.isLogin();
        System.out.println(commonReturnType);
        return commonReturnType;
    }

    @RequestMapping("/testRedis")
    @ResponseBody
    public CommonReturnType testRedis(){
        redisTemplate.opsForValue().set("testRedis","suceess");
        return CommonReturnType.create(redisTemplate.opsForValue().get("testRedis"));
    }

    @RequestMapping("/testCookie")
    @ResponseBody
    public CommonReturnType testCookie(@CookieValue(value = "is_login",required = false) String uid) throws BusinessException{
        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(uid);
        return CommonReturnType.create(userVO);
    }

    @RequestMapping("/testReturnType")
    @ResponseBody
    public CommonReturnType testReturnType() throws BusinessException {
        Integer itemId = 7;
        System.out.println("itemId:"+itemId);
        System.out.println(itemFeignClient.getItemByIdInCache(7)==null);
        System.out.println(itemFeignClient.getItemByIdInCache(7).getClass());
        return CommonReturnType.create(itemFeignClient.getItemByIdInCache(7).toString());
    }
}
