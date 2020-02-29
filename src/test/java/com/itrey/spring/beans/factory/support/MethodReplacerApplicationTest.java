package com.itrey.spring.beans.factory.support;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MethodReplacerApplicationTest.class)
@SpringBootApplication
public class MethodReplacerApplicationTest {

    @Resource
    private BeanATest beanATest;
    
    @Test
    public void test(){
        // beanB.getName replaced beanA.getName
        String text1 = beanATest.getName("abc");
        Assert.assertEquals("call a1","bean b1",text1);

        String text2 = beanATest.getName("abc","abc2");
        Assert.assertEquals("call a2","bean a2",text2);

    }
}
