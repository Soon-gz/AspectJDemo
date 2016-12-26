package com.example.administrator.aspectjdemo;

import android.app.Activity;
import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Created by Administrator on 2016/12/26.
 */
@Aspect
public class AspectJTest {
    private static final String TAG = "tag00";

    @Pointcut("execution(@com.example.administrator.aspectjdemo.AspectJAnnotation  * *(..))")
    public void executionAspectJ() {

    }

    @Around("executionAspectJ()")
    public Object aroundAspectJ(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Log.i(TAG, "aroundAspectJ(ProceedingJoinPoint joinPoint)");
        AspectJAnnotation aspectJAnnotation = methodSignature.getMethod().getAnnotation(AspectJAnnotation.class);
        String permission = aspectJAnnotation.value();
        Object o = null;
        if (PermissionManager.getInnerInstance().checkPermission(permission)) {
            o = joinPoint.proceed();
            Log.i(TAG, "有权限");
        } else {
            Log.i(TAG, "没有权限，不给用");
        }
        return o;
    }

}
