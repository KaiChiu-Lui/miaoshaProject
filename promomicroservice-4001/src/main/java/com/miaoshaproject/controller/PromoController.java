package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.PromoVO;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.impl.PromoServiceImpl;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@CrossOrigin(origins = {"*"}, allowCredentials = "true")
@RequestMapping("/promo")
public class PromoController extends BaseController{

    @Autowired
    private PromoServiceImpl promoService;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getPromoByItemId(@RequestParam("id") Integer itemId){
        System.out.println("什么情况");
        PromoModel promoModel = promoService.getPromoByItemId(itemId);
        System.out.println("promoModel:"+promoModel);
        PromoVO promoVO = convertFromModel(promoModel);
        if(promoVO==null) return CommonReturnType.create(null);
        promoVO.setStartDateString(simpleDateFormat.format(promoModel.getStartDate()));
        promoVO.setEndDateString(simpleDateFormat.format(promoModel.getEndDate()));
        CommonReturnType commonReturnType = CommonReturnType.create(promoVO);
        return commonReturnType;
    }

    @RequestMapping("/list")
    @ResponseBody
    public CommonReturnType listPromo(){
        List<PromoModel> promoModels = promoService.listPromo();
        ArrayList<PromoVO> promoVOs = new ArrayList<>();
        for(PromoModel promoModel : promoModels){
            System.out.println(promoModel);
            PromoVO promoVO = convertFromModel(promoModel);
            promoVO.setStartDateString(simpleDateFormat.format(promoModel.getStartDate()));
            promoVO.setEndDateString(simpleDateFormat.format(promoModel.getEndDate()));
            promoVOs.add(promoVO);
        }
        // System.out.println(promoVOs);
        return CommonReturnType.create(promoVOs);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public CommonReturnType deletePromo(@RequestParam("id") int promoId){
        return CommonReturnType.create(promoService.deletePromo(promoId));
    }

    @RequestMapping("/insert")
    @ResponseBody
    public CommonReturnType insertPromo(@RequestParam("startTime") String startTime,
                                        @RequestParam("endTime") String endTime,
                                        @RequestParam("promoName") String promoName,
                                        @RequestParam("itemId") Integer itemId,
                                        @RequestParam("promoItemPrice") BigDecimal promoItemPrice) throws Exception{
        PromoVO promoVO = new PromoVO();
        promoVO.setPromoName(promoName);
        promoVO.setItemId(itemId);
        promoVO.setPromoItemPrice(promoItemPrice);
        Date startDate = simpleDateFormat.parse(startTime);
        Date endDate = simpleDateFormat.parse(endTime);
        promoVO.setStartDate(startDate);
        promoVO.setEndDate(endDate);
        System.out.println(promoVO);
        PromoModel promoModel = convertFromVO(promoVO);
        int result = promoService.insertPromo(promoModel);
        return CommonReturnType.create(result);
    }

    public PromoVO convertFromModel(PromoModel promoModel){
        PromoVO promoVO = new PromoVO();
        if(promoModel==null) return null;
        BeanUtils.copyProperties(promoModel,promoVO);
        return promoVO;
    }

    public PromoModel convertFromVO(PromoVO promoVO){
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoVO,promoModel);
        return promoModel;
    }
}
