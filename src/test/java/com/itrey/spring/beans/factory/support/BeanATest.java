package com.itrey.spring.beans.factory.support;

import org.springframework.stereotype.Component;

@Component
public class BeanATest implements BeanTest {
    public String getName(String txt){
        return "bean a1";
    }

    @Override
    public String getName(String text1, String text2) {
        return "bean a2";
    }
}
