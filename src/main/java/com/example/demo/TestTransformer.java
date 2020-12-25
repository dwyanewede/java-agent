package com.example.demo;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;


public class TestTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println("Transforming " + className);
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.get("com.example.demo.BaseMBean");
            CtMethod m = cc.getDeclaredMethod("process");
            // java.lang.RuntimeException: com.example.demo.BaseMBean class is frozen
            // 如果一个CtClass对象通过writeFile（）,toClass（）或者toByteCode（）转换成class文件，
            // 那么javassist会冻结这个CtClass对象。后面就不能修改这个CtClass对象了。
            // 这样是为了警告开发者不要修改已经被JVM加载的class文件，因为JVM不允许重新加载一个类。

            String beforeCode = "{System.err.println(\"start sxs \" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\")));}";
            m.insertBefore(beforeCode);
            String afterCode = "{System.err.println(\"end sxs \" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\")));}";
            m.insertAfter(afterCode);
            return cc.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}