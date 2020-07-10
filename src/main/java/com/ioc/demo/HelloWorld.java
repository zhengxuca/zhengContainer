package com.ioc.demo;

import com.zheng.annotations.Autowired;
import com.zheng.annotations.Component;
import com.zheng.annotations.Value;

import java.util.Scanner;

@Component
public class HelloWorld implements Runnable {

    @Autowired
    Calculator calculator;

    @Value("name")
    String name;

    @Value("value")
    int value;

    @Override
    public void run() {

        System.out.println("Hello world! " + name);
        System.out.println("Your value is: " + value);
        String input = null;
        Scanner scan = new Scanner(System.in);
        while ((input = scan.nextLine()).equals("exit") == false) {
            String[] line = input.split("[ ]");
            if (line.length == 2) {
                try {
                    System.out.println("The sum is: " + calculator.sum(Integer.parseInt(line[0]), Integer.parseInt(line[1])));
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
}
