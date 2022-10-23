package com.miaoshaproject.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.swing.plaf.nimbus.NimbusStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/userm")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*") //@CrossOrigin解决跨域请求错误
public class UserManagementController extends BaseController{

    @Autowired
    private UserService userService;
    
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的对应用户信息不存在
        if (userModel == null) {
//            userModel.setEncrptPassword("123");
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        //将核心领域模型用户对象转化为可供UI使用的viewobject
        UserVO userVO = convertFromModel(userModel);

        //返回通用对象
        return CommonReturnType.create(userVO);
    }

    @RequestMapping("/getUserByIdInCache")
    public UserVO getUserByIdInCache(Integer id) throws BusinessException {
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get("user_validate_"+id);
        if(userVO==null){
            userVO = JSONObject.parseObject(JSON.toJSONString(this.getUser(id).getData()),UserVO.class);
            redisTemplate.opsForValue().set("user_validate_"+id,userVO);
            redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);
        }
        return userVO;
    }

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listUser(){
        List<UserModel> list = userService.list();
        return CommonReturnType.create(list);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public CommonReturnType deleteUser(@RequestParam("id") int userId){
        int i = userService.deleteUser(userId);
        System.out.println("删除成功");
        return CommonReturnType.create(i);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }
}
