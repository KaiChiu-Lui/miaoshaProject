package com.miaoshaproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class OrderDO {

    private String id;

    private Integer userId;

    private Integer itemId;

    private BigDecimal itemPrice;

    private Integer amount;

    private BigDecimal orderPrice;

    private Integer promoId;

}