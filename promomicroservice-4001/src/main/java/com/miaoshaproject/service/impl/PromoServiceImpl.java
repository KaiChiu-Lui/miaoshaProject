package com.miaoshaproject.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public PromoModel getPromoByPrimaryKey(Integer promoId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO==null) return null;
        PromoModel promoModel = convertFromDataObject(promoDO);
        return promoModel;
    }

    //根据itemId获取即将开始的或者正在进行的活动
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {
        //获取商品对应的秒杀信息
        System.out.println(0);
        System.out.println("itemId:"+itemId);
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        System.out.println(1);
        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if (promoModel == null) {
            return null;
        }
        System.out.println(2);
        //判断当前时间是否秒杀活动即将开始或正在进行
        if (promoModel.getStartDate().compareTo(new Date())>0) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().compareTo(new Date())<0) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        System.out.println(3);
        System.out.println(promoModel);
        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);

        promoModel.setStartDate(promoDO.getStartDate());
        promoModel.setEndDate(promoDO.getEndDate());

        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if (promoModel.getStartDate().compareTo(new Date())>0) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().compareTo(new Date())<0) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    @Override
    public List<PromoModel> listPromo() {
        List<PromoDO> promoDOs = promoDOMapper.listPromo();
        ArrayList<PromoModel> promoModels = new ArrayList<>();
        for(PromoDO promoDO : promoDOs){
            PromoModel promoModel = convertFromDataObject(promoDO);
            promoModels.add(promoModel);
        }
        return promoModels;
    }

    @Override
    public int deletePromo(int promoId) {
        return promoDOMapper.deleteByPrimaryKey(promoId);
    }

    @Override
    public int insertPromo(PromoModel promoModel) {
        PromoDO promoDO = convertFromModel(promoModel);
        System.out.println(promoDO);
        int result = promoDOMapper.insertSelective(promoDO);
        return result;
    }
    public PromoDO convertFromModel(PromoModel promoModel){
        PromoDO promoDO = new PromoDO();
        BeanUtils.copyProperties(promoModel,promoDO);
        return promoDO;
    }

    @Override
    public void publishPromo(Integer promoId) throws BusinessException{
        //通过活动id获取活动
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO.getItemId() == null || promoDO.getItemId().intValue() == 0){
            return;
        }
        ItemVO itemVO = JSONObject.parseObject(JSON.toJSONString(itemFeignClient.getItem(promoDO.getItemId()).getData()), ItemVO.class);
        if(itemVO==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"活动商品不存在");
        }
        //将库存同步到redis内
        redisTemplate.opsForValue().set("promo_item_stock_"+itemVO.getId(), itemVO.getStock());
    }

    @Override
    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) throws BusinessException{
        //1.判断是否库存已售罄，若对应的售罄key存在，则直接返回下单失败
        if(redisTemplate.hasKey("promo_item_stock_invalid_"+itemId)){
            return null;
        }
        System.out.println("判断售罄完成");

        //2.判断活动是否正在进行
        System.out.println(1);
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        //dataobject->model
        System.out.println(1);
        PromoModel promoModel = convertFromDataObject(promoDO);
        System.out.println(promoModel);
        System.out.println(1);
        if(promoModel == null){
            return null;
        }
        System.out.println(1);
        if(promoModel.getStatus().intValue() != 2){
            return null;
        }
        System.out.println(1);
        System.out.println("判断活动完成");

        //3.判断item信息是否存在
        ItemVO itemVO = itemFeignClient.getItemByIdInCache(itemId);
        System.out.println(itemVO);
        if(itemVO==null){
            return null;
        }
        System.out.println("判断商品信息完成");

        //4.判断用户信息是否存在
        UserVO userVO = userFeignClient.getUserByIdInCache(userId);
        if (userVO == null) {
            return null;
        }
        System.out.println("判断用户信息完成");

        //生成token并且存入redis内并给一个5分钟的有效期
        String token = UUID.randomUUID().toString().replace("-","");
        //生成对应的UserId->ItemId的5分钟的令牌
        redisTemplate.opsForValue().set("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,token);
        redisTemplate.expire("promo_token_"+promoId+"_userid_"+userId+"_itemid_"+itemId,5, TimeUnit.MINUTES);
        System.out.println("生成令牌完成");
        return token;
    }
}
