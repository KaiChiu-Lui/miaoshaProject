package com.miaoshaproject.controller;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/userm")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*") //@CrossOrigin解决跨域请求错误
public class UserManagementController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private HttpServletRequest httpServletRequest;

    /**
     * @Author LvQiChao
     * @Description 获取用户信息
     * @Date 15:34 2022/10/14 0014
     * @Param [id]
     * @return com.miaoshaproject.response.CommonReturnType
     **/
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

    /**
     * @Author LvQiChao
     * @Description 查看所有的User
     * @Date 15:40 2022/10/14 0014
     * @Param
     * @return
     **/
    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listUser(){
        List<UserModel> list = userService.list();
        return CommonReturnType.create(list);
    }
    
    /**
     * @Author LvQiChao
     * @Description 删除User
     * @Date 15:40 2022/10/14 0014
     * @Param []
     * @return com.miaoshaproject.response.CommonReturnType
     **/
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
