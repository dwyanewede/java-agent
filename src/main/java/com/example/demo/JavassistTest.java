package com.example.demo;

import javassist.*;
import java.io.IOException;


public class JavassistTest {
    public static void main(String[] args) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, IOException {
//        Base base = new Base();
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("com.example.demo.Base");
        CtMethod m = cc.getDeclaredMethod("process");
        m.insertBefore("{ System.err.println(\"start\"); }");
        m.insertAfter("{ System.err.println(\"end\"); }");
        Class c = cc.toClass();
        cc.writeFile("D:\\localworkerspace\\agent\\src\\main\\java\\com\\example\\demo");
        Base h = (Base)c.newInstance();
        h.process();
    }
}