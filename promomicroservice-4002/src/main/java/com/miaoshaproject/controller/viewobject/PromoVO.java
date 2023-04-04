package com.miaoshaproject.controller.viewobject;

import lombok.Data;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PromoVO {
    private Integer id;

    //秒杀活动状态：1表示还未开始，2表示正在进行，3表示已结束
    private Integer status;

    //秒杀活动名称
    private String promoName;

    //秒杀活动的开始时间
    private Date startDate;

    //秒杀活动的结束时间
    private Date endDate;

    //秒杀活动的适用商品
    private Integer itemId;

    //秒杀活动的商品价格
    private BigDecimal promoItemPrice;

    //格式转化后的开始日期
    private String startDateString;

    //格式转化后的结束日期
    private String endDateString;
}
