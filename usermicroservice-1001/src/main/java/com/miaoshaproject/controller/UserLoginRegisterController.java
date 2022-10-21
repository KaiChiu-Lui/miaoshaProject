package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

import static com.miaoshaproject.controller.BaseController.CONTENT_TYPE_FORMED;

@Controller
@RequestMapping("/userlr")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*") //@CrossOrigin解决跨域请求错误
public class UserLoginRegisterController extends BaseController{

    @Autowired
    public RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    //用户登录接口
    @RequestMapping(value = "/login", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                      @RequestParam(name = "password") String password,
                                      HttpServletResponse response) throws BusinessException,
            UnsupportedEncodingException,
            NoSuchAlgorithmException{
        //入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登录服务，用来校验用户登录是否合法
        //用户加密后的密码
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));
        //将登陆凭证加入到Redis和cookie中
        String randomId = UUID.randomUUID().toString().replaceAll("-","");
        redisTemplate.opsForValue().set(randomId,userModel);
        System.out.println(redisTemplate.opsForValue().get(randomId));
        Cookie cookie = new Cookie("is_login",randomId);
        cookie.setMaxAge(60*5); //过期时间五分钟
        response.addCookie(cookie);
        // this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        // this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
        return CommonReturnType.create(null);
    }

    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") String gender,
                                     @RequestParam(name = "age") String age,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        //验证手机号和对应的otpCode相符合
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(Integer.valueOf(age));
        userModel.setGender(Byte.valueOf(gender));
        userModel.setTelphone(telphone);
        userModel.setRegisitMode("byphone");

        //密码加密
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);

    }

    //密码加密
    public String EncodeByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        //确定计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密字符串
        String newstr = base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {

        //需要按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将OTP验证码同对应用户的手机号关联，使用httpsession的方式绑定手机号与OTPCDOE
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        //将OTP验证码通过短信通道发送给用户，省略
        System.out.println("telphone=" + telphone + "&otpCode=" + otpCode);

        return CommonReturnType.create(null);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

    //判断某个用户是否登录 如果是则返回UserVO 否则返回null
    @RequestMapping("/islogin")
    @ResponseBody
    public CommonReturnType isLogin(@CookieValue(value = "is_login",required = false) String uid) throws BusinessException{
        // Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        // if (isLogin == null || !isLogin.booleanValue()) {
        //     System.out.println(1);
        //     System.out.println(2);
        //     throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能下单");
        // }
        // UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        // UserVO userVO = convertFromModel(userModel);
        // return CommonReturnType.create(userModel);
        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        return CommonReturnType.create(redisTemplate.opsForValue().get(uid));
    }

    @RequestMapping("/testLogin")
    @ResponseBody
    public CommonReturnType testLogin(@RequestParam(name = "telphone") String telphone,
                                      @RequestParam(name = "password") String password,
                                      HttpServletResponse response) throws BusinessException,
            UnsupportedEncodingException,
            NoSuchAlgorithmException{
        //入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //用户登录服务，用来校验用户登录是否合法
        //用户加密后的密码
        UserModel userModel = userService.validateLogin(telphone, this.EncodeByMd5(password));
        //将登陆凭证加入到Redis和cookie中
        String randomId = UUID.randomUUID().toString().replaceAll("-","");
        redisTemplate.opsForValue().set(randomId,userModel);
        System.out.println(redisTemplate.opsForValue().get(randomId));
        Cookie cookie = new Cookie("is_login",randomId);
        cookie.setMaxAge(60*5); //过期时间五分钟
        response.addCookie(cookie);
        // this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        // this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/testIsLogin")
    @ResponseBody
    public CommonReturnType testIsLogin(@CookieValue(value = "is_login",required = false) String uid) throws BusinessException{
        System.out.println(1);
        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        return CommonReturnType.create(redisTemplate.opsForValue().get(uid));
    }

    @RequestMapping("/testCookie")
    @ResponseBody
    public CommonReturnType testCookie(@CookieValue(value = "is_login",required = false) String uid){
        return CommonReturnType.create(uid);
    }

}
