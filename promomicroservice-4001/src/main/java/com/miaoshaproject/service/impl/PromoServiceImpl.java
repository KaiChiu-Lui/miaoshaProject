package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.PromoDOMapper;
import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author KiroScarlet
 * @date 2019-05-24  -21:46
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;


    @Override
    public PromoModel getPromoByPrimaryKey(Integer promoId) {
        PromoDO promoDO = promoDOMapper.selectByPrimaryKey(promoId);
        if(promoDO==null) return null;
        PromoModel promoModel = convertFromDataObject(promoDO);
        return promoModel;
    }

    //根据iremId获取即将开始的或者正在进行的活动
    @Override
    public PromoModel getPromoByItemId(Integer itemId) {

        //获取商品对应的秒杀信息
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);

        //dataobject->model
        PromoModel promoModel = convertFromDataObject(promoDO);
        if (promoModel == null) {
            return null;
        }
        //判断当前时间是否秒杀活动即将开始或正在进行
        if (promoModel.getStartDate().compareTo(new Date())>0) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().compareTo(new Date())<0) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    private PromoModel convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoModel promoModel = new PromoModel();
        BeanUtils.copyProperties(promoDO, promoModel);

        promoModel.setStartDate(promoDO.getStartDate());
        promoModel.setEndDate(promoDO.getEndDate());

        //判断当前时间是否秒杀活动即将开始或正在进行
        DateTime now = new DateTime();
        if (promoModel.getStartDate().compareTo(new Date())==-1) {
            promoModel.setStatus(1);
        } else if (promoModel.getEndDate().compareTo(new Date())==1) {
            promoModel.setStatus(3);
        } else {
            promoModel.setStatus(2);
        }
        return promoModel;
    }

    @Override
    public List<PromoModel> listPromo() {
        List<PromoDO> promoDOs = promoDOMapper.listPromo();
        ArrayList<PromoModel> promoModels = new ArrayList<>();
        for(PromoDO promoDO : promoDOs){
            PromoModel promoModel = convertFromDataObject(promoDO);
            promoModels.add(promoModel);
        }
        return promoModels;
    }

    @Override
    public int deletePromo(int promoId) {
        return promoDOMapper.deleteByPrimaryKey(promoId);
    }

    @Override
    public int insertPromo(PromoModel promoModel) {
        PromoDO promoDO = convertFromModel(promoModel);
        System.out.println(promoDO);
        int result = promoDOMapper.insertSelective(promoDO);
        return result;
    }
    public PromoDO convertFromModel(PromoModel promoModel){
        PromoDO promoDO = new PromoDO();
        BeanUtils.copyProperties(promoModel,promoDO);
        return promoDO;
    }
}
