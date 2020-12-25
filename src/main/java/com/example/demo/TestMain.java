package com.example.demo;

/**
 * @ClassName TestMain
 * @Description: TODO
 * @Author sxs
 * @Date 2020/12/23 18:36
 */
public class TestMain {

    public static void main(String[] args) {
        String preLog = "{ System.err.println(" +  123 ;
        String concat = preLog.concat("); }");

        System.err.println(concat);
    }
}
