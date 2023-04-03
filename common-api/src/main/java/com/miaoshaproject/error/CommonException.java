package com.miaoshaproject.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommonException implements CommonError{

    public String errMsg;

    public Integer errCode;

    public CommonException(String errMsg){
        this.errMsg = errMsg;
    }

    public int getErrCode(){
        return this.errCode;
    }

    public String getErrMsg(){
        return this.errMsg;
    }

    public CommonError setErrMsg(String errMsg){
        this.errMsg = errMsg;
        return this;
    }

}
