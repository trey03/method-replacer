package com.itrey.spring.beans.factory.support;

import java.lang.reflect.Method;

import lombok.Builder;
import lombok.Data;

/**
 * service replacer configuration .
 */
@Data
@Builder
public class ServiceReplacerConfigInfo {
    private String targetBeanName;
    private String beanName;
    private Method method;
    private int order ;
}
