package com.itrey.spring.beans.factory.support;

import java.lang.reflect.Method;
import java.util.Objects;

import lombok.Data;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.MethodReplacer;

/**
 * @author chenfeng
 */
@Data
public class ServiceMethodReplacer implements MethodReplacer {
    private String name;
    private Object replacer;
    private Method targetMethod;
    @Override
    public Object reimplement(Object source, Method method, Object[] args) throws Throwable {

        if (Objects.isNull(replacer)) {
            throw new NoSuchBeanDefinitionException(name);
        }

        if (Objects.isNull(targetMethod)) {
            targetMethod = replacer.getClass().getMethod(method.getName(),method.getParameterTypes());
        }

        if (Objects.isNull(targetMethod)) {
            throw new NoSuchMethodException("Not such method["+method.toGenericString()+"] in "+replacer.getClass());
        }

        return targetMethod.invoke(replacer,args);
    }
}
