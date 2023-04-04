package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;

public interface PaymentService {
    public void createOrder(Integer userId, Integer itemId, Integer amount) throws BusinessException;

    public void createPromoOrder(Integer userId, Integer itemId, Integer promoId, Integer amount,String stockLogId) throws BusinessException;

    public String initStockLog(Integer itemId, Integer amount);
}
