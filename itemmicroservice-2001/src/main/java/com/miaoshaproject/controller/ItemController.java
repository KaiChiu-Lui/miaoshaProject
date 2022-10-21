package com.miaoshaproject.controller;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.ItemFeignClient;
import com.miaoshaproject.client.PromoFeignClient;
import com.miaoshaproject.client.UserFeignClient;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.controller.viewobject.PromoVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/item")
//跨域请求中，不能做到session共享
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoFeignClient promoFeignClient;

    //创建商品的controller
    @RequestMapping(value = "/create", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn,null);
        return CommonReturnType.create(itemVO);

    }

    @RequestMapping("/delete")
    @ResponseBody
    public CommonReturnType deleteItem(@RequestParam("id") int itemId){
        int result = itemService.deleteItem(itemId);
        System.out.println("删除成功");
        CommonReturnType commonReturnType = CommonReturnType.create(result);
        System.out.println(commonReturnType);
        return commonReturnType;
    }

    private ItemVO convertVOFromModel(ItemModel itemModel,PromoVO promoVO) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel, itemVO);
        if(promoVO!=null){
            itemVO.setPromoStatus(promoVO.getStatus());
            itemVO.setPromoId(promoVO.getId());
            itemVO.setStartDate(promoVO.getStartDateString());
            itemVO.setPromoPrice(promoVO.getPromoItemPrice());
        }
        else {
            itemVO.setPromoStatus(0);
        }
        return itemVO;
    }

    @RequestMapping("/decreaseStock")
    @ResponseBody
    public CommonReturnType decreaseStock(Integer itemId,Integer amount) throws BusinessException{
        return CommonReturnType.create(itemService.decreaseStock(itemId,amount));
    }

    @RequestMapping("/increaseSale")
    @ResponseBody
    public CommonReturnType increaseSale(Integer itemId,Integer amount) throws BusinessException{
        itemService.increaseSales(itemId,amount);
        return CommonReturnType.create(null);
    }
    //商品详情页浏览
    @RequestMapping(value = "/get", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id") Integer id) {
        CommonReturnType commonReturnType = promoFeignClient.getPromoByItemId(id);
        PromoVO promoVO = null;
        if(commonReturnType.getData()!=null){
            String str = JSON.toJSONString(commonReturnType.getData());
            promoVO = JSONObject.parseObject(str, PromoVO.class);
        }
        ItemModel itemModel = itemService.getItemById(id);
        // System.out.println(promoVO);
        ItemVO itemVO = convertVOFromModel(itemModel,promoVO);
        return CommonReturnType.create(itemVO);
    }

    //商品列表页面浏览
    @RequestMapping(value = "/list", method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem() {
        List<ItemModel> itemModelList = itemService.listItem();
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            CommonReturnType commonReturnType = promoFeignClient.getPromoByItemId(itemModel.getId());
            PromoVO promoVO = null;
            if(commonReturnType.getData()!=null){
                String str = JSON.toJSONString(commonReturnType.getData());
                promoVO = JSONObject.parseObject(str, PromoVO.class);
            }
            ItemVO itemVO = this.convertVOFromModel(itemModel,promoVO);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);
    }

    public RestTemplate restTemplate = new RestTemplate();


    @Autowired
    public ItemFeignClient itemFeignClient;


    @RequestMapping("/test")
    @ResponseBody
    public void test(){
        CommonReturnType commonReturnType = itemFeignClient.getItem(7);
        System.out.println(commonReturnType);
        System.out.println(commonReturnType.getStatus().getClass());
        System.out.println(commonReturnType.getData().getClass());
        System.out.println(commonReturnType.getData());
        String data = JSON.toJSONString(commonReturnType.getData());
        ItemVO itemVO = JSONObject.parseObject(data,ItemVO.class);
        System.out.println(itemVO.getClass());
        System.out.println(itemVO);
    }
}
