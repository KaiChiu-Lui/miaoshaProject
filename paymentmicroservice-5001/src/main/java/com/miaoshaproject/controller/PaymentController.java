package com.miaoshaproject.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.impl.PaymentServiceImpl;
import com.miaoshaproject.service.model.UserModel;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.nimbus.NimbusStyle;

import static com.miaoshaproject.controller.BaseController.CONTENT_TYPE_FORMED;

@Controller
@RequestMapping("/payment")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class PaymentController extends BaseController{

    @Autowired
    public PaymentServiceImpl paymentService;

    @Autowired
    public HttpServletRequest httpServletRequest;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public UserFeignClient userFeignClient;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@CookieValue(value = "is_login",required = false) String uid,
                                        @RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId",required = false) Integer promoId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        // System.out.println(uid);
        // //获取用户登录信息
        // if(uid==null||redisTemplate.opsForValue().get(uid)==null){
        //     throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录,不能下单");
        // }
        // UserModel userModel = (UserModel) redisTemplate.opsForValue().get(uid);
        // System.out.println(userModel);
        System.out.println(0);
        CommonReturnType commonReturnType = userFeignClient.getUser(58);
        System.out.println(00);
        String userVOStr = JSON.toJSONString(commonReturnType.getData());
        System.out.println(000);
        UserVO userVO = JSONObject.parseObject(userVOStr,UserVO.class);
        System.out.println(1);
        paymentService.createOrder(userVO.getId(), itemId, promoId, amount);
        System.out.println(2);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/islogin")
    @ResponseBody
    public CommonReturnType testLogin() throws BusinessException{
        CommonReturnType commonReturnType = userFeignClient.isLogin();
        System.out.println(commonReturnType);
        return commonReturnType;
    }

    @RequestMapping("/testRedis")
    @ResponseBody
    public CommonReturnType testRedis(){
        redisTemplate.opsForValue().set("testRedis","suceess");
        return CommonReturnType.create(redisTemplate.opsForValue().get("testRedis"));
    }

    @RequestMapping("/testCookie")
    @ResponseBody
    public CommonReturnType testCookie(@CookieValue(value = "is_login",required = false) String uid){
        return CommonReturnType.create(uid);
    }

    @RequestMapping("/testUserFeignClient")
    @ResponseBody
    public CommonReturnType testUserFeignClient() throws BusinessException {
        return userFeignClient.getUser(58);
    }
}
