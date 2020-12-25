package com.example.demo;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseMBean {
    public static void main(String[] args) {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String s = name.split("@")[0];
        //打印当前Pid
        System.out.println("pid:"+s);
        while (true) {
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
                break;
            }

            process();
        }
    }

    public static void process() {
        System.out.println("process");
    }
}