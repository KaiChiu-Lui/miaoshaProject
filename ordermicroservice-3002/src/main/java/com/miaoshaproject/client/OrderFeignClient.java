package com.miaoshaproject.client;

import com.miaoshaproject.response.CommonReturnType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;

@Component
@FeignClient(value = "order-microservice",path = "/order")
public interface OrderFeignClient {
    @RequestMapping("/insert")
    @ResponseBody
    public CommonReturnType insertOrder(@RequestParam(value = "userId",required = false) Integer userId,
                                        @RequestParam(value = "itemId",required = false) Integer itemId,
                                        @RequestParam(value = "promoId",required = false) Integer promoId,
                                        @RequestParam(value = "amount",required = false) Integer amount,
                                        @RequestParam(value = "itemPrice",required = false) String itemPrice);
}
