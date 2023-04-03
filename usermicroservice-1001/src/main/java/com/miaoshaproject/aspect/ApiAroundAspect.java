package com.miaoshaproject.aspect;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.CommonException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ApiAroundAspect {

    private static final Logger logger = LoggerFactory.getLogger(ApiAroundAspect.class);

    @Around("execution(* com.miaoshaproject.controller..*(..))")
    public void aroundControllerAspect(ProceedingJoinPoint joinPoint) throws Exception,Throwable{
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error(className + "." + methodName + "(): " + Arrays.toString(args) + " throws exception", e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        logger.info(className + "." + methodName + "(): " + Arrays.toString(args) + " returns " + result + " (time: " + (endTime - startTime) + "ms)" + "success");
    }

    @Around("execution(* com.miaoshaproject.service.impl..*(..))")
    public void aroundServiceAspect(ProceedingJoinPoint joinPoint) throws Exception,Throwable{
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error(className + "." + methodName + "(): " + Arrays.toString(args) + " throws exception", e);
            throw e;
        }

        long endTime = System.currentTimeMillis();
        logger.info(className + "." + methodName + "(): " + Arrays.toString(args) + " returns " + result + " (time: " + (endTime - startTime) + "ms)" + "success");
    }
}
