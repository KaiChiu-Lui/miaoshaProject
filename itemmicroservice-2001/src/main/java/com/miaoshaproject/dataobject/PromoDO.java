package com.miaoshaproject.dataobject;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

@Component
@Data
public class PromoDO {

    private Integer id;

    private String promoName;

    private Date startDate;

    private Date endDate;

    private Integer itemId;

    private BigDecimal promoItemPrice;
}