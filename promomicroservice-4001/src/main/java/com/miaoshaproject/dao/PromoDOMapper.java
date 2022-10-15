package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.PromoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PromoDOMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(PromoDO record);

    int insertSelective(PromoDO record);

    PromoDO selectByItemId(Integer itemId);

    PromoDO selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PromoDO record);

    int updateByPrimaryKey(PromoDO record);

    List<PromoDO> listPromo();
}