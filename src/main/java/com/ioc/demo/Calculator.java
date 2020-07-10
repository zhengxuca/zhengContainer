package com.ioc.demo;

import com.zheng.annotations.Component;

@Component
public class Calculator {

    public int sum(int a, int b) {
        return a + b;
    }
}
