package com.miaoshaproject.controller.viewobject;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderVO {
    private Integer itemId;
    private String title;
    private String description;
    private String imgUrl;

    private String orderId;
    private BigDecimal itemPrice;
    private Integer amount;
    private BigDecimal orderPrice;
}
