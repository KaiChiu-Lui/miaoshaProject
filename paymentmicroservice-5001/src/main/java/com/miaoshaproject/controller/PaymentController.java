package com.miaoshaproject.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.impl.PaymentServiceImpl;
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

    // @Autowired
    // public HttpServletRequest httpServletRequest;

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    public UserFeignClient userFeignClient;

    @Auto
    private ItemFeignClient itemFeignClient;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@CookieValue(value = "is_login",required = false) String uid,
                                        @RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId",required = false) Integer promoId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(uid);
        if(promoId==null) paymentService.createOrder(userVO.getId(), itemId, amount);
        else paymentService.createPromoOrder(userVO.getId(), itemId, promoId,amount);
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
    public CommonReturnType testCookie(@CookieValue(value = "is_login",required = false) String uid) throws BusinessException{
        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(uid);
        return CommonReturnType.create(userVO);
    }

    @RequestMapping("/testReturnType")
    @ResponseBody
    public CommonReturnType testReturnType() throws BusinessException {
        Integer itemId = 7;
        System.out.println("itemId:"+itemId);
        System.out.println(itemFeignClient.getItemByIdInCache(7)==null);
        System.out.println(itemFeignClient.getItemByIdInCache(7).getClass());
        return CommonReturnType.create(itemFeignClient.getItemByIdInCache(7).toString());
    }
}
