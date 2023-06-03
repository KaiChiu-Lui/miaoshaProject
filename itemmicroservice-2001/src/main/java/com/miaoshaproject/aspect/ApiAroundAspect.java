package com.miaoshaproject.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ApiAroundAspect {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${eureka.instance.instance-id}")
    private String instanceId;

    private static final Logger logger = LoggerFactory.getLogger(ApiAroundAspect.class);

    @Around(value = "execution(* com.miaoshaproject.controller..*.*(..))")
    public Object aroundControllerAspect(ProceedingJoinPoint joinPoint) throws Exception,Throwable{
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error("fail: "+ "applicationName:" + applicationName + " " + "instanceId:"+instanceId + " " + className + "." + methodName + "(): " + args.toString() + " throws exception:"+e);
            throw e;
        }
        long endTime = System.currentTimeMillis();
        logger.info("success: "+ "applicationName:" + applicationName + " " + "instanceId:"+instanceId + " " + className + "." + methodName + "(): " + args.toString() +" returns " + result + " (time: " + (endTime - startTime) + "ms)");
        return result;
    }

    @Around(value = "execution(* com.miaoshaproject.service.impl..*.*(..))")
    public Object aroundServiceAspect(ProceedingJoinPoint joinPoint) throws Exception,Throwable{
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error("fail: "+ "applicationName:" + applicationName + " " + "instanceId:"+instanceId + " " + className + "." + methodName + "(): " + args + " throws exception:"+e);
            throw e;
        }
        long endTime = System.currentTimeMillis();
        if(result==null){

        }
        logger.info("success: "+ "applicationName:" + applicationName + " " + "instanceId:"+instanceId + " " + className + "." + methodName + "(): " + args +" returns " + result + " (time: " + (endTime - startTime) + "ms)");
        return result;
    }
}
