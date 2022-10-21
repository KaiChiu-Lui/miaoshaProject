package com.miaoshaproject.client;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@FeignClient(value = "item-microservice",path = "/item")
public interface ItemFeignClient {
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id);

    @RequestMapping("/decreaseStock")
    @ResponseBody
    public CommonReturnType decreaseStock(@RequestParam("itemId") Integer itemId,@RequestParam("amount")Integer amount) throws BusinessException;

    @RequestMapping("/increaseSale")
    @ResponseBody
    public CommonReturnType increaseSale(@RequestParam("itemId")Integer itemId,@RequestParam("amount") Integer amount) throws BusinessException;
}
