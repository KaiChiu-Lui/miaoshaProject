package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.OrderVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    //封装下单请求
    @RequestMapping(value = "/createorder", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "promoId",required = false) Integer promoId,
                                        @RequestParam(name = "amount") Integer amount) throws BusinessException {

        //获取用户登录信息
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能下单");
        }
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");


        OrderModel orderModel = orderService.createOrder(userModel.getId(), itemId, promoId, amount);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listOrder() throws BusinessException{
        int userId = 0;
        //获取用户登录信息
        Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "用户还未登录，不能查看订单");
        }
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");
        userId = userModel.getId();
        List<OrderModel> orderModels = orderService.listOrder(userId);
        ArrayList<OrderVO> orderVOs = new ArrayList<OrderVO>();
        for(OrderModel orderModel : orderModels){
            int itemId = orderModel.getItemId();
            ItemModel itemModel = itemService.getItemById(itemId);
            if(itemModel==null) continue;
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(itemModel,orderVO);
            BeanUtils.copyProperties(orderModel,orderVO);
            orderVO.setOrderId(orderModel.getId());
            orderVO.setItemId(itemModel.getId());
            orderVOs.add(orderVO);
        }
        return CommonReturnType.create(orderVOs);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public CommonReturnType deleteOrder(@RequestParam("id") String orderId){
        int result = orderService.deleteOrder(orderId);
        System.out.println("已经删掉了");
        return CommonReturnType.create(result);
    }
}
