package com.miaoshaproject.service.impl;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.OrderFeignClient;
import com.miaoshaproject.client.PromoFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.PaymentService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PaymentServiceImpl implements PaymentService{


    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;
    
    @Autowired
    private PromoFeignClient promoFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Override
    @Transactional
    public void createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        //1.下单的商品是否存在
        CommonReturnType commonReturnType = itemFeignClient.getItem(itemId);
        String str = JSON.toJSONString(commonReturnType.getData());
        if(str==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不存在");
        }
        ItemVO itemVO = JSONObject.parseObject(str,ItemVO.class);
        System.out.println("itemVO");
        System.out.println(itemVO);

        //2.用户是否登录
        CommonReturnType userresult = userFeignClient.getUser(userId);
        String s = JSON.toJSONString(commonReturnType.getData());
        if(s==null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不存在");
        }
        JSONObject.parseObject(s, UserModel.class);

        //3.下单数量是否合法
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不存在");
        }

        //4.活动信息是否正确
        if (promoId != null) {
            //(1)校验对应活动是否存在这个适用商品
            if (promoId.intValue() != itemVO.getPromoId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
                //(2)校验活动是否正在进行中
            } else if (itemVO.getPromoStatus() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
            }
        }

        //5.落单减库存
        boolean result = (boolean) itemFeignClient.decreaseStock(itemId,amount).getData();
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //6.生成订单及其流水号
        BigDecimal itemPrice;
        if (promoId != null) {
            itemPrice = itemVO.getPromoPrice();
        } else {
            itemPrice = itemVO.getPrice();
        }
        System.out.println("itemPrice");
        System.out.println(itemPrice);


        orderFeignClient.insertOrder(userId,itemId,promoId,amount,itemPrice.toString());

        //7.加上商品的销量
        itemFeignClient.increaseSale(itemId,amount);
        //4.返回前端
        return;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    //不管该方法是否在事务中，都会开启一个新的事务，不管外部事务是否成功
    //最终都会提交掉该事务，为了保证订单号的唯一性，防止下单失败后订单号的回滚
    public String generateOrderNo() {
        //订单有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位为时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);

        //中间6位为自增序列
        //获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");

        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        //拼接
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);

        //最后两位为分库分表位,暂时不考虑
        stringBuilder.append("00");

        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }
    private OrderModel convertModelFromDO(OrderDO orderDO){
        if(orderDO==null) return null;
        OrderModel orderModel = new OrderModel();
        BeanUtils.copyProperties(orderDO,orderModel);
        return orderModel;
    }
}
