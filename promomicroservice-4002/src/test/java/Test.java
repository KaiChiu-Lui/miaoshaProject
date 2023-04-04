import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.miaoshaproject.client.PromoFeignClient;
import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class Test {
    @Autowired
    public PromoFeignClient promoFeignClient;

    @org.junit.Test
    public void testCommonReturnType(){
        ItemVO itemVO = new ItemVO();
        itemVO.setPromoPrice(BigDecimal.valueOf(10));
        itemVO.setId(10);
        itemVO.setStartDate("xx");
        itemVO.setPromoStatus(1);

        CommonReturnType commonReturnType = CommonReturnType.create(itemVO);
        String str = JSON.toJSONString(commonReturnType);
        CommonReturnType commonReturnType1 = JSONObject.parseObject(str, CommonReturnType.class);
        System.out.println(str);
        System.out.println(commonReturnType1);
        System.out.println("--------------");
        System.out.println(commonReturnType1.getData().getClass());
        ItemVO itemVO1 = JSON.toJavaObject(commonReturnType1.getData(), ItemVO.class);
        System.out.println(itemVO1);
    }
}
