package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;

public interface PaymentService {
    public void createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;
}
