package com.miaoshaproject.service.impl;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.OrderFeignClient;
import com.miaoshaproject.client.PromoFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.controller.viewobject.UserVO;
// import com.miaoshaproject.dao.OrderDOMapper;
// import com.miaoshaproject.dao.SequenceDOMapper;
// import com.miaoshaproject.dataobject.OrderDO;
// import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.PaymentService;
// import com.miaoshaproject.service.UserService;
// import com.miaoshaproject.service.model.OrderModel;
// import com.miaoshaproject.service.model.UserModel;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PromoFeignClient promoFeignClient;

    @Override
    @Transactional
    public void createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException {
        //1.下单的商品是否存在
        ItemVO itemVO = JSONObject.parseObject(JSON.toJSONString(itemFeignClient.getItem(itemId).getData()),ItemVO.class);
       if(itemVO==null){
           throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
       }
        //2.用户是否登录
        UserVO userVO = JSONObject.parseObject(JSON.toJSONString(userFeignClient.getUser(userId).getData()), UserVO.class);
        if (userVO == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }
        //3.下单数量是否合法
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不存在");
        }
        //4.活动信息是否正确 //如果有活动才进行校验 无活动则不进行校验
        // if (promoId != null) {
        //     //(1)校验对应活动是否存在这个适用商品
        //     if (promoId.intValue() != itemVO.getPromoId()) {
        //         throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
        //         //(2)校验活动是否正在进行中
        //     } else if (itemVO.getPromoStatus() != 2) {
        //         throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
        //     }
        // }
        //5.落单减库存
        boolean result = (boolean) itemFeignClient.decreaseStock(itemId,amount).getData();
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //6.生成订单及其流水号
        BigDecimal itemPrice = itemVO.getPrice();
        orderFeignClient.insertOrder(userId,itemId,null,amount,itemPrice.toString());

        //7.加上商品的销量
        itemFeignClient.increaseSale(itemId,amount);
        //4.返回前端
        return;
    }

    @Override
    @Transactional
    public void createPromoOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException{
        System.out.println("正在进行活动商品下单接口的调用");
        //1.下单的商品是否存在
        ItemVO itemVO = itemFeignClient.getItemByIdInCache(itemId);
        System.out.println(itemVO);
        if(itemVO==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        //2.用户是否登录
        UserVO userVO = userFeignClient.getUserByIdInCache(userId);
        if (userVO == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }
        //3.下单数量是否合法
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不存在");
        }
        //4.活动信息是否正确 //如果有活动才进行校验 无活动则不进行校验
        if (promoId != null) {
            System.out.println("promoId:"+promoId);
            System.out.println("itemVO:"+itemVO.toString());
            //(1)校验对应活动是否存在这个适用商品
            if (promoId.intValue() != itemVO.getPromoId()) {
                System.out.println("活动信息不正确");
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
                //(2)校验活动是否正在进行中
            } else if (itemVO.getPromoStatus() != 2) {
                System.out.println("活动未开始或者已经结束了");
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动未开始或者已经结束了");
            }
            System.out.println("活动信息检查完毕");
        }
        //5.落单减库存
        CommonReturnType commonReturnType = promoFeignClient.decreaseStock(itemId,amount);
        if(commonReturnType.getStatus().equals("fail")){
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH,"扣减库存失败");
        }
        System.out.println("完成了落单减库存");
        //6.生成订单及其流水号
        BigDecimal itemPrice = itemVO.getPromoPrice();
        orderFeignClient.insertOrder(userId,itemId,promoId,amount,itemPrice.toString());
        System.out.println("完成生成订单及其流水号");
        //7.加上商品的销量
        itemFeignClient.increaseSale(itemId,amount);
        System.out.println("完成加上商品销量");
        //4.返回前端
        return;
    }
}
