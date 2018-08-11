package com.zfz.myspringmvc.annotation;

import java.lang.annotation.*;

/**
 * Created by zl on 2018-08-11.
 */
@Documented
@Target(ElementType.FIELD)//类
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
    public String value();
}
