package com.zfz.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * Created by zl on 2018-08-11.
 */
@Documented
@Target({ElementType.METHOD,ElementType.TYPE})//类
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    public String value();
}
