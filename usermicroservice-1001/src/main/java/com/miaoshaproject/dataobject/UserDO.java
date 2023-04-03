package com.miaoshaproject.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDO {

    private Integer id;

    private String name;

    private Byte gender;

    private Integer age;

    private String telphone;

    private String regisitMode;

    private Integer thirdPartyId;
}