# AspectJDemo
[面向AOP编程入门](http://blog.csdn.net/sw5131899/article/details/53885957) 

那么我们先来说说什么是AOP。说到AOP也许不太了解，那么OOP一定是知道的。Object-Oriented Progreming.面向对象编程。学Java基础的时候肯定都都会学面向对象思想和三大特性。这里就不详细介绍了。AOP是Aspect-Oriented Progreming的缩写，在OOP设计中有个单一职责原则，在很多时候都不会有问题，但是当很多模块都需要同一个功能的时候，这个时候还用OOP就会很麻烦。那么AOP在Android中的应用就应运而生。
先从搭环境开始吧，待会会把aspectJ的jar包放在Git上，下载1.8.5.jar包之后，安装在电脑上。直接点安装即可。
![](https://github.com/SingleShu/AspectJDemo/raw/master/Logo/a.png)  
![](https://github.com/SingleShu/AspectJDemo/raw/master/Logo/b.png)  
下一步下一步就好。这里我只介绍Android Studio的。Eclipse也是可以使用的。Studio就好配置了，在Gradle中进行配置。

贴一下我的gradle的配置。
```Java
    apply plugin: 'com.android.application'  
    import org.aspectj.bridge.IMessage  
    import org.aspectj.bridge.MessageHandler  
    import org.aspectj.tools.ajc.Main  
    buildscript {  
        repositories {  
            mavenCentral()  
        }  
        dependencies {  
            classpath 'org.aspectj:aspectjtools:1.8.9'  
            classpath 'org.aspectj:aspectjweaver:1.8.9'  
        }  
    }  
    repositories {  
        mavenCentral()  
    }  
    final def log = project.logger  
    final def variants = project.android.applicationVariants  
    variants.all { variant ->  
        if (!variant.buildType.isDebuggable()) {  
            log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")  
            return;  
        }  
      
        JavaCompile javaCompile = variant.javaCompile  
        javaCompile.doLast {  
            String[] args = ["-showWeaveInfo",  
                             "-1.8",  
                             "-inpath", javaCompile.destinationDir.toString(),  
                             "-aspectpath", javaCompile.classpath.asPath,  
                             "-d", javaCompile.destinationDir.toString(),  
                             "-classpath", javaCompile.classpath.asPath,  
                             "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]  
            log.debug "ajc args: " + Arrays.toString(args)  
      
            MessageHandler handler = new MessageHandler(true);  
            new Main().run(args, handler);  
            for (IMessage message : handler.getMessages(null, true)) {  
                switch (message.getKind()) {  
                    case IMessage.ABORT:  
                    case IMessage.ERROR:  
                    case IMessage.FAIL:  
                        log.error message.message, message.thrown  
                        break;  
                    case IMessage.WARNING:  
                        log.warn message.message, message.thrown  
                        break;  
                    case IMessage.INFO:  
                        log.info message.message, message.thrown  
                        break;  
                    case IMessage.DEBUG:  
                        log.debug message.message, message.thrown  
                        break;  
                }  
            }  
        }  
    }  
      
    android {  
        compileSdkVersion 24  
        buildToolsVersion "24.0.2"  
        defaultConfig {  
            applicationId "com.example.administrator.aspectjdemo"  
            minSdkVersion 19  
            targetSdkVersion 24  
            versionCode 1  
            versionName "1.0"  
            testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"  
        }  
        buildTypes {  
            release {  
                minifyEnabled false  
                proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'  
            }  
        }  
    }  
      
      
      
    dependencies {  
        compile fileTree(include: ['*.jar'], dir: 'libs')  
        androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {  
            exclude group: 'com.android.support', module: 'support-annotations'  
        })  
        compile 'com.android.support:appcompat-v7:24.2.1'  
        testCompile 'junit:junit:4.12'  
        compile files('libs/aspectjrt.jar')  
    }  
```
至于为什么这么配置，AspectJ是对java的扩展，而且是完全兼容java的。但是编译时得用Aspect专门的编译器，这里的配置就是使用Aspect的编译器。这里还在libs导入了一个jar包。创建依赖。
![](https://github.com/SingleShu/AspectJDemo/raw/master/Logo/c.png) 
好啦，所有准备工作已完成。那么拿着梭子就是干，别想那么多了。上车吧！

AOP很多是拿来做用户行为统计和性能检测的。那我这里写一个手机权限检测的使用方法。

首先创建一个类，用来处理触发切面的回调。
```Java
    package com.example.administrator.aspectjdemo;  
      
    import android.app.Activity;  
    import android.content.Context;  
    import android.util.Log;  
      
    import org.aspectj.lang.ProceedingJoinPoint;  
    import org.aspectj.lang.annotation.Around;  
    import org.aspectj.lang.annotation.Aspect;  
    import org.aspectj.lang.annotation.Pointcut;  
    import org.aspectj.lang.reflect.MethodSignature;  
      
    /** 
     * Created by Administrator on 2016/12/26. 
     */  
    @Aspect  
    public class AspectJTest {  
        private static final String TAG = "tag00";  
      
        @Pointcut("execution(@com.example.administrator.aspectjdemo.AspectJAnnotation  * *(..))")  
        public void executionAspectJ() {  
      
        }  
      
        @Around("executionAspectJ()")  
        public Object aroundAspectJ(ProceedingJoinPoint joinPoint) throws Throwable {  
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();  
            Log.i(TAG, "aroundAspectJ(ProceedingJoinPoint joinPoint)");  
            AspectJAnnotation aspectJAnnotation = methodSignature.getMethod().getAnnotation(AspectJAnnotation.class);  
            String permission = aspectJAnnotation.value();  
            Context  context = (Context) joinPoint.getThis();  
            Object o = null;  
            if (PermissionManager.getInnerInstance().checkPermission(context,permission)) {  
                o = joinPoint.proceed();  
                Log.i(TAG, "有权限");  
            } else {  
                Log.i(TAG, "没有权限，不给用");  
            }  
            return o;  
        }  
      
    }  
```

这里要使用Aspect的编译器编译必须给类打上标注，@Aspect。

还有这里的Pointcut注解，就是切点，即触发该类的条件。里面的字符串都有哪些呢。让我们来看看，
![](https://github.com/SingleShu/AspectJDemo/raw/master/Logo/d.png) 
在我的例子中，我使用了execution，也就是以方法执行时为切点，触发Aspect类。而execution里面的字符串是触发条件，也是具体的切点。我来解释一下参数的构成。“execution(@com.example.administrator.aspectjdemo.AspectJAnnotation  * *(..)”这个条件是所有加了AspectJAnnotation注解的方法或属性都会是切点，范围比较广。

“**”表示是任意包名

“..”表示任意类型任意多个参数

“com.example.administrator.aspectjdemo.AspectJAnnotation”这是我的项目包名下需要指定类的绝对路径

那知道了这些基本上就能定义自己想要的切点了。比如我只想在MainActivity的onCreate()方法执行时作为切点。那么字符串应该是“* com.example.administrator.aspectjdemo.MainActivity.onCreate(..)”，那么这个切定就很明确，只有一个。不同的切点可以用与或来连接。看清楚哦，前面有个*号。因为onCreate方法时编译时执行，所以在回调时传入的参数必须是父类JoinPoint。上面除了execution还有其他很多切点类型。比如call方法回调时，get和set分别是获取和执行时。等等，就不举例了。

再来看看@Around,Around是指JPoint执行前或执行后备触发，而around就替代了原JPoint。除了Around还有其他几种方式。
![](https://github.com/SingleShu/AspectJDemo/raw/master/Logo/e.png) 
这里的说明和示例大家看看就大概明白什么意思了，具体怎么使用还是亲自去实践才行的。

回到我们的例子上来，创建完Aspect类之后，还需要一个注解类来做笔，哪里需要做切点，那么哪里就用注解标注一下，这样方便快捷。解决了OOP的单一原则问题。

```Java
package com.example.administrator.aspectjdemo;  
  
  
import java.lang.annotation.ElementType;  
import java.lang.annotation.Retention;  
import java.lang.annotation.RetentionPolicy;  
import java.lang.annotation.Target;  
  
/** 
 * Created by Administrator on 2016/12/26. 
 */  
@Target(ElementType.METHOD)  
@Retention(RetentionPolicy.RUNTIME)  
public @interface AspectJAnnotation {  
    String value();  
} 
```
注解其实很简单的，里面的类型并不多。想要了解的童鞋我下篇文章做个学习笔记，大家一起学习一下。

在哪使用呢。方法名和属性名上都可以。

```Java
    package com.example.administrator.aspectjdemo;  
      
    import android.Manifest;  
    import android.support.v7.app.AppCompatActivity;  
    import android.os.Bundle;  
    import android.util.Log;  
      
    public class MainActivity extends AppCompatActivity {  
      
        @Override  
        protected void onCreate(Bundle savedInstanceState) {  
            super.onCreate(savedInstanceState);  
            setContentView(R.layout.activity_main);  
            test();  
        }  
      
        @AspectJAnnotation(value = Manifest.permission.CAMERA)  
        public void test(){  
            Log.i("tag00","检查权限");  
        }  
    }  
```
text()方法执行时就是一个切点。在执行test时，会回调上面的Aspect类的executionAspectJ()方法。然后会依次执行@Before,@After。至于After和Around谁先谁后，这个我不太清楚，如果两个都写的话会报错。所以不知先后。有兴趣的可以自己试试。那么我们在看看aroundAspectJ(ProceedingJoinPoint joinPoint)这个方法里的逻辑。 

```Java
@Around("executionAspectJ()")  
    public Object aroundAspectJ(ProceedingJoinPoint joinPoint) throws Throwable {  
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();  
        Log.i(TAG, "aroundAspectJ(ProceedingJoinPoint joinPoint)");  
        AspectJAnnotation aspectJAnnotation = methodSignature.getMethod().getAnnotation(AspectJAnnotation.class);  
        String permission = aspectJAnnotation.value();  
        Context  context = (Context) joinPoint.getThis();  
        Object o = null;  
        if (PermissionManager.getInnerInstance().checkPermission(context,permission)) {  
            o = joinPoint.proceed();  
            Log.i(TAG, "有权限");  
        } else {  
            Log.i(TAG, "没有权限，不给用");  
        }  
        return o;  
    } 
```
如果使用的是以方法相关为切点，那么使用MethodSignature来接收joinPoint的Signature。如果是属性或其他的，那么可以使用Signature类来接收。之后可以使用Signature来获取注解类。通过注解可以拿到需要检测的权限名称。但是检测权限又需要上下文，那么通过jointPoint.getThis()获取使用该注解的Activity的上下文，至于Fragment的我还试过。有兴趣的自己试试。那么我们再来看看PermissionManager这个类。

```Java
    package com.example.administrator.aspectjdemo;  
      
    import android.content.Context;  
    import android.content.pm.PackageManager;  
    import android.support.v4.content.ContextCompat;  
    import android.util.Log;  
      
    /** 
     * Created by Administrator on 2016/12/26. 
     */  
      
    public class PermissionManager {  
        private static volatile PermissionManager permissionManager;  
      
        public PermissionManager(){}  
      
        //DCL单例模式  
        public static PermissionManager getInstance(){  
            if (permissionManager == null){  
                synchronized (PermissionManager.class){  
                    if (permissionManager == null){  
                        permissionManager = new PermissionManager();  
                    }  
                }  
            }  
            return permissionManager;  
        }  
      
        private static class InnerInsatance{  
            public static final PermissionManager instance = new PermissionManager();  
        }  
      
        //内部类单例模式  
        public static PermissionManager getInnerInstance(){  
            synchronized (PermissionManager.class){  
                return InnerInsatance.instance;  
            }  
        }  
      
        public boolean checkPermission(Context context,String permission){  
            Log.i("tag00","检查的权限："+permission);  
            if (ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED){  
                return true;  
            }  
    //        if (permission.equals("android.permission.CAMERA")){  
    //            return true;  
    //        }  
            return false;  
        }  
    }  
```
在这个类我试验了一下两种单例模式的性能差别，刚都说了AOP可以用来检测性能嘛。不过没在高并发的环境下其实两种都差不多。ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED这句代码是检测权限。Android提供的类，可以直接使用。那么到这里思路差不多清晰了。以后想要做用户行为统计，或是性能检测，亦或是权限或者用户权限管理，AOP都是你的不二之选。

怎么样？捋一遍你学到了吗？觉得有用star一下，谢谢。



