package com.miaoshaproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class PromoDO {

    private Integer id;

    private String promoName;

    private Date startDate;

    private Date endDate;

    private Integer itemId;

    private BigDecimal promoItemPrice;
}