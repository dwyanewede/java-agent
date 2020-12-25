# java-agent

### JavassistTest 

采用 `javassist`实现在编译阶段字节码的修改。

```java
public class JavassistTest {
    public static void main(String[] args) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, IOException {

        // Exception in thread "main" javassist.CannotCompileException: by java.lang.LinkageError: loader (instance of  sun/misc/Launcher$AppClassLoader): attempted  duplicate class definition for name: "com/sprucetec/Base"

        // Base base = new Base();
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
```



### TestAgent 

制作可执行的`java agent`。

###### 编写`TestAgent.java`类

```java
public class TestAgent {
    public static void agentmain(String args, Instrumentation inst) {
        //指定我们自己定义的Transformer，在其中利用Javassist做字节码替换
        inst.addTransformer(new TestTransformer(), true);
        try {
            //重定义类并载入新的字节码
            inst.retransformClasses(BaseMBean.class);
            System.out.println("Agent Load Done.");
        } catch (Exception e) {
            System.out.println("Agent load failed!");
        }
    }
}
```

###### /[META-INF](https://github.com/dwyanewede/java-agent/tree/master/src/main/resources/META-INF)/MANIFEST.MF

```properties
Manifest-Version: 1.0
Agent-Class: com.example.demo.TestAgent
Can-Redefine-Classes: true
Created-By: shangxs
Can-Redefine-Classes: true
Can-Retransform-Classes: true
Boot-Class-Path: javassist-3.23.1-GA.jar
```



### 制作agent.jar

1. 制作可执行`jar`包
![image-20201225155931670](https://github.com/dwyanewede/java-agent/blob/master/src/main/resources/project.picture/image-20201225155931671.png)

2. 构建`agent.jar`
![image-20201225155931670](https://github.com/dwyanewede/java-agent/blob/master/src/main/resources/project.picture/image-20201225161046092.png)
   

3. build artifacts
![image-20201225155931670](https://github.com/dwyanewede/java-agent/blob/master/src/main/resources/project.picture/image-20201225161201473.png)


4. 执行`jar`包构建
![image-20201225155931670](https://github.com/dwyanewede/java-agent/blob/master/src/main/resources/project.picture/image-20201225162341334.png)

   

### 运行`BaseMBean.java`

```java
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
```



### 获取上述运行`pid` 并且执行`Attacher.java`

```java
public class Attacher {
    public static void main(String[] args) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
        // 传入目标 JVM pid
        VirtualMachine vm = VirtualMachine.attach("69204");
        vm.loadAgent("D:\\localworkerspace\\agent\\out\\artifacts\\agent_jar\\agent.jar");
    }
}
```



### 观察运行结果

`javassist`编译生成的字节码，通过`java.lang.instrument.ClassFileTransformer`动态注入`jvm`成功

打印运行结果：

```properties
start sxs 2020-12-25 16:28:41
end sxs 2020-12-25 16:28:41
process
start sxs 2020-12-25 16:28:44
end sxs 2020-12-25 16:28:44
process
start sxs 2020-12-25 16:28:47
end sxs 2020-12-25 16:28:47
start sxs 2020-12-25 16:28:50
end sxs 2020-12-25 16:28:50
process
```

### 遗留问题

1. 字节码注入报错 `class is frozen`

```java
java.lang.RuntimeException: com.example.demo.BaseMBean class is frozen
	at javassist.CtClassType.checkModify(CtClassType.java:321)
	at javassist.CtBehavior.insertBefore(CtBehavior.java:773)
	at javassist.CtBehavior.insertBefore(CtBehavior.java:766)
	at com.example.demo.TestTransformer.transform(TestTransformer.java:24)
	at sun.instrument.TransformerManager.transform(TransformerManager.java:188)
	at sun.instrument.InstrumentationImpl.transform(InstrumentationImpl.java:428)
	at java.time.format.DateTimeFormatterBuilder.toFormatter(DateTimeFormatterBuilder.java:2060)
	at java.time.format.DateTimeFormatter.<clinit>(DateTimeFormatter.java:710)
	at com.example.demo.BaseMBean.process(BaseMBean.java)
	at com.example.demo.BaseMBean.main(BaseMBean.java:20)
java.lang.RuntimeException: com.example.demo.BaseMBean class is frozen
	at javassist.CtClassType.checkModify(CtClassType.java:321)
	at javassist.CtBehavior.insertBefore(CtBehavior.java:773)
	at javassist.CtBehavior.insertBefore(CtBehavior.java:766)
	at com.example.demo.TestTransformer.transform(TestTransformer.java:24)
	at sun.instrument.TransformerManager.transform(TransformerManager.java:188)
	at sun.instrument.InstrumentationImpl.transform(InstrumentationImpl.java:428)
	at java.util.Locale.getDefault(Locale.java:836)
	at java.time.format.DateTimeFormatterBuilder.toFormatter(DateTimeFormatterBuilder.java:2060)
	at java.time.format.DateTimeFormatter.<clinit>(DateTimeFormatter.java:710)
	at com.example.demo.BaseMBean.process(BaseMBean.java)
	at com.example.demo.BaseMBean.main(BaseMBean.java:20)
```

2. 字节码注入后运行结果顺序不一致

   ```
   -- 预期结果
   start sxs 2020-12-25 16:28:41
   process
   end sxs 2020-12-25 16:28:41
   start sxs 2020-12-25 16:28:44
   process
   end sxs 2020-12-25 16:28:44
   ```

   

