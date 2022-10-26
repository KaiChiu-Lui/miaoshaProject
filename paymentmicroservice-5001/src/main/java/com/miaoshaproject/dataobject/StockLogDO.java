package com.miaoshaproject.dataobject;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class StockLogDO {

    private String stockLogId;

    private Integer itemId;

    private Integer amount;

    private Integer status;
}