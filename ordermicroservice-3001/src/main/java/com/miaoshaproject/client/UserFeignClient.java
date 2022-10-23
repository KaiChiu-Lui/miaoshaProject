package com.miaoshaproject.client;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@FeignClient(value = "user-microservice")
public interface UserFeignClient {
    @RequestMapping("/userlr/islogin")
    @ResponseBody
    public CommonReturnType isLogin() throws BusinessException;

    @RequestMapping("/userm/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException;

    @RequestMapping("/userm/getUserByIdInCache")
    public UserVO getUserByIdInCache(Integer id) throws BusinessException;
}
