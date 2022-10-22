package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.SequenceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SequenceDOMapper {

    public SequenceDO getSequenceByName(String name);

    public Integer updateByPrimaryKeySelective(SequenceDO sequenceDO);
}
