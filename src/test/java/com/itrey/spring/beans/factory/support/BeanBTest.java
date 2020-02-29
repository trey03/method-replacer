package com.itrey.spring.beans.factory.support;

import org.springframework.stereotype.Component;

@Component
public class BeanBTest extends BeanATest{

    @ServiceReplacer(targets = {"beanATest"})
    public String getName(String text){
        return "bean b1";
    }

    @Override
    public String getName(String text1, String text2) {
        return  "bean b2";
    }
}
