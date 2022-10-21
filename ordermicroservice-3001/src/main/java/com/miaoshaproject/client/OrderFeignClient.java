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
    public CommonReturnType insertOrder(@RequestParam("userId") Integer userId,
                                        @RequestParam("itemId") Integer itemId,
                                        @RequestParam("promoId") Integer promoId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam("itemPrice") String itemPrice);
}
