package com.miaoshaproject.error;

/**
 * @author KaiChui
 * @date 2023-05-15  -21:33
 */
public interface CommonError {
    public int getErrCode();

    public String getErrMsg();

    public CommonError setErrMsg(String errMsg);
}
