package com.miaoshaproject.service;

import com.miaoshaproject.dataobject.PromoDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.PromoModel;

import java.util.List;

/**
 * @author KaiChui
 * @date 2023-05-24  -21:26
 */
public interface PromoService {

    PromoModel getPromoByPrimaryKey(Integer promoId);

    PromoModel getPromoByItemId(Integer itemId);

    List<PromoModel> listPromo();

    int deletePromo(int promoId);

    int insertPromo(PromoModel promoModel);

    public void publishPromo(Integer promoId)  throws BusinessException;

    public String generateSecondKillToken(Integer promoId,Integer itemId,Integer userId) throws BusinessException;
}
