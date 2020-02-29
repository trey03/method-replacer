package com.itrey.spring.beans.factory.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author chenfeng
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceReplacer {

    /**
     * bean interface class
     */
    //Class<?> targetType();

    /**
     * target bean name
     */
    String[] targets();

    /**
     * repla
     */
    int order() default 0;

}
