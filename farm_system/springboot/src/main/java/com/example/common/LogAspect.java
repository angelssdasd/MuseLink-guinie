package com.example.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Admin;
import com.example.entity.Artifact;
import com.example.entity.Log;
import com.example.service.LogService;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LogAspect {

    @Resource
    private LogService logService;
    @Around("@annotation(logAOP)")
    public Object doAround(ProceedingJoinPoint joinPoint, LogAOP logAOP) throws Throwable {


        String type = logAOP.title();
        String content = logAOP.content();
        String time = DateUtil.now();
        Integer operator = 0;
        Admin user = JwtTokenUtils.getCurrentUser();
        System.out.println("userId: " + user);
        if(ObjectUtil.isNotNull(user)){
            operator = user.getUserId();
        }
        System.out.println("userId: " + operator);

        Result result = (Result)joinPoint.proceed();

        Object data = result.getData();
        if(data instanceof Admin){
            Admin admin = (Admin) data;
            operator = admin.getUserId();
            System.out.println("login userId: " + operator);
        }

        System.out.println("type: " + type
                + ", content: " + content
                + ", time: " + time
                + ", userId: " + operator);

        Log log = new Log(type, content, time, operator);
        logService.add(log);

        return result;
    }
}
