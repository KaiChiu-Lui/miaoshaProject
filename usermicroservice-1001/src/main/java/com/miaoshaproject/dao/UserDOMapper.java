package com.miaoshaproject.dao;

import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.dataobject.UserDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UserDOMapper {

    int deleteByPrimaryKey(Integer id);


    int insert(UserDO record);


    int insertSelective(UserDO record);


    UserDO selectByPrimaryKey(Integer id);

    //根据电话号码取得用户对象
    UserDO selectByTelphone(String telphone);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);

    ArrayList<UserDO> list();
}