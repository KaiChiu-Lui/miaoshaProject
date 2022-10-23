package com.miaoshaproject.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.controller.viewobject.OrderVO;
import com.miaoshaproject.controller.viewobject.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.impl.OrderServiceImpl;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Controller("order")
@RequestMapping("/order")
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class OrderController extends BaseController {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private ItemFeignClient itemFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listOrder(@CookieValue(value = "is_login",required = false) String uid) throws BusinessException{
        if(uid==null||redisTemplate.opsForValue().get(uid)==null){
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN,"用户未登录");
        }
        UserVO userVO = (UserVO) redisTemplate.opsForValue().get(uid);

        List<OrderModel> orderModels = orderService.listOrder(userVO.getId());
        ArrayList<OrderVO> orderVOs = new ArrayList<OrderVO>();
        for(OrderModel orderModel : orderModels){
            int itemId = orderModel.getItemId();
            ItemVO itemVO = JSONObject.parseObject(JSON.toJSONString(itemFeignClient.getItem(itemId).getData()),ItemVO.class);
            if (itemVO==null) continue;
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(itemVO,orderVO);
            BeanUtils.copyProperties(orderModel,orderVO);
            orderVO.setOrderId(orderModel.getId());
            orderVO.setItemId(itemVO.getId());
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

    @RequestMapping("/insert")
    @ResponseBody
    public CommonReturnType insertOrder(Integer userId, Integer itemId, Integer promoId, Integer amount, String itemPrice){
        BigDecimal bigDecimal = new BigDecimal(itemPrice);
        Integer insertResult = orderService.insertOrder(userId, itemId, promoId, amount, bigDecimal);
        return CommonReturnType.create(insertResult);
    }
}
