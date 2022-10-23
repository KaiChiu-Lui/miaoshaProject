package com.miaoshaproject.client;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@FeignClient(value = "promo-microservice",path = "/promo")
public interface PromoFeignClient {

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getPromoByItemId(@RequestParam("id") Integer itemId);

    @RequestMapping("/decreaseStock")
    @ResponseBody
    public CommonReturnType decreaseStock(@RequestParam("itemId") Integer itemId,
                                          @RequestParam("amount") Integer amount) throws BusinessException;

}
