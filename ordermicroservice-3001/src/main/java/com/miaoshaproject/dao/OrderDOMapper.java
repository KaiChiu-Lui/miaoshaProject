package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.OrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDOMapper {
    public Integer deleteByPrimaryKey(String orderId);

    public List<OrderDO> listOrder(Integer userId);

    public Integer insertSelective(OrderDO orderDO);
}
