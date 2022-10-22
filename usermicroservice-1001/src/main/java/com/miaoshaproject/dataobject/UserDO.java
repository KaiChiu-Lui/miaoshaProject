package com.miaoshaproject.dataobject;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class UserDO {

    private Integer id;

    private String name;

    private Byte gender;

    private Integer age;

    private String telphone;

    private String regisitMode;

    private Integer thirdPartyId;
}