# AboutSpring
springMVC 学习项目

## 第一阶段

Tomcat的入口点是WEB-INF目录下的web.xml文件，在这个问题中定义servlet和servlet-mapping，springMVC通过拦截所有的请求，跳过了javaEE的servlet-mapping机制，而自己组织路由。

###有关RequestMapping有三种用法：

1. annotations at only method level
    在方法层声明全部的路径
    ```java
        @Controller
        public class EmployeeController
        {
            @RequestMapping("/employee-management/employees")
            public String getAllEmployees(Model model)
            {
                //application code
                return "employeesList";
            }
         }
     ```
2. annotations at class level as well as method levels
    在class上声明通用的部分，在方法上声明不同的部分
    ```java
       @Controller
       @RequestMapping("/employee-management/employees/*")
       public class EmployeeController
       {
           @RequestMapping
           public String getAllEmployees(Model model)
           {
               //application code
               return "employeesList";
           }
        }
    ```
3. annotations using only HTTP request types
    整体是RESTful风格的api，用http方法来区分不同的方法
    ```java
       @Controller
       @RequestMapping("/employee-management/employees")
       public class EmployeeController
       {
           @RequestMapping (method =  RequestMethod.GET)
           public String getAllEmployees(Model model)
           {
               //application code
               return "employeesList";
           }
        }
    ```
 
 ### 有关Validator的用法：
 
 1. 首先继承Validator接口
    ```java
        @Component
        public class EmployeeValidator implements Validator {
        //...
        }
    ```
 2. 在需要使用这个验证器的controller上定义一个private参数，可以使用@AutoWired来自动装配
    ```java
    public class EmployeeController {
        @Autowired
        EmployeeValidator validator;
        //...
    }
    ```
 3. 在具体的方法中调用验证器
 
    ```java
    public class EmployeeController {
        @RequestMapping(value = "addNew", method = RequestMethod.POST)
        public String submitForm(@ModelAttribute("employee") EmployeeVO employeeVO, BindingResult result,
                SessionStatus status, Model model) {
            validator.validate(employeeVO, result);
    
            if (result.hasErrors()) {
                // 将错误打包到视图
                model.addAttribute(result.getAllErrors());
                return "addEmployee";
            }
    
            //Store the employee information in database
            //manager.createNewRecord(employeeVO);
    
            status.setComplete();
            return "redirect:addSuccess";
        }
    }
    ```

### 有关@initBinder

在SpringMVC中，bean中定义了Date，double等类型，如果没有做任何处理的话，日期以及double都无法绑定。使用InitBinder标记，将editor注入到controller，如果遇到需要转换的对象，则调用对应的editor。DepartmentVO.class 是需要转换的对象，DepartmentEditor 是我们自己编写的转换方法。

### 有关 @SessionAttributes

在默认情况下，ModelMap 中的属性作用域是 request 级别是，也就是说，当本次请求结束后，ModelMap 中的属性将销毁。如果希望在多个请求中共享 ModelMap 中的属性，必须将其属性转存到 session 中，这样 ModelMap 的属性才可以被跨请求访问。

这里我们仅将一个 ModelMap 的属性放入 Session 中，其实 @SessionAttributes 允许指定多个属性。你可以通过字符串数组的方式指定多个属性，如 @SessionAttributes({“attr1”,”attr2”})。此外，@SessionAttributes 还可以通过属性类型指定要 session 化的 ModelMap 属性，如 @SessionAttributes(types = User.class)，当然也可以指定多个类，如 @SessionAttributes(types = {User.class,Dept.class})，还可以联合使用属性名和属性类型指定：@SessionAttributes(types = {User.class,Dept.class},value={“attr1”,”attr2”})。

### 有关@ModelAttribute

ModelAttribute修饰的方式将会被先调用，为接下来的视图准备数据。

### JSR-303数据校验

一直存在问题，校验器无法由工厂方法生成，网上查询的问题原因是，版本冲突，推荐使用一下版本：

    ```xml
    <dependency>
        <groupId>javax.validation</groupId>
        <artifactId>validation-api</artifactId>
        <version>1.0.0.GA</version>
    </dependency>
     
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>4.3.1.Final</version>
    </dependency>
    ```

然而，我用的就是这个版本，问题依旧：

```java
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'employeeController' defined in file [D:\apps\apache-tomcat-8.5.31\webapps\ROOT\WEB-INF\classes\com\neptune8\controller\EmployeeController.class]: Instantiation of bean failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.neptune8.controller.EmployeeController]: Constructor threw exception; nested exception is java.lang.NoClassDefFoundError: Could not initialize class org.hibernate.validator.internal.engine.ConfigurationImpl
```

怎样解决，目前无解，先放一放再去寻找解决方案吧。

### i18n 问题

对于国际化的问题springMVC有对应的解决方案，但是比较复杂，首先想象一下需要几个步骤：

1. 首先要有数据源，就是一种对照表，能够在不同的地区之间进行转换。
2. 然后需要一个拦截器，检查请求中的location之类的参数，对不同的地区赋予不同的数据。
3. 最后一步就是将转换之后的数据体现在view中。


首先定义数据源：

```xml
    <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basename" value="messages" />
    </bean>
```

需要注意的是，还需要在resources目录下定义不同地区的对照表，这样不管在什么地区，对于某个消息来说，都具有了唯一的代码。

然后定义拦截器，和转换器：

```xml
   <bean id="localeResolver" class="org.springframework.web.servlet.i18n.SessionLocaleResolver">
       <property name="defaultLocale" value="en" />
   </bean>

   <bean id="localeChangeInterceptor" class="org.springframework.web.servlet.i18n.LocaleChangeInterceptor">
       <property name="paramName" value="lang" />
   </bean>

   <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping">
       <property name="interceptors">
           <list>
               <ref bean="localeChangeInterceptor" />
           </list>
       </property>
   </bean>
```

对于RequestMappingHandlerMapping来讲，可以定义多个拦截器，实现比如安全认证之类的功能。

最后，我们已经知道了具体的地区，已经去除了对应地区的消息，现在只需要在jsp中体现即可：

注意文件的头部引用：

```jsp
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
```
具体这个spring有什么用，其实我也不知道，我觉得实际使用jsp还是少吧，所以暂且跳过，就知道他能获取到messageSource的数据即可。

### 拦截器 Interceptor

首先实现 HandlerInterceptor 接口，会有三个接口需要实现：

1. preHandle

预处理回调方法，实现处理器的预处理（如检查登陆），第三个参数为响应的处理器，自定义Controller返回值：true表示继续流程（如调用下一个拦截器或处理器）；false表示流程中断（如登录检查失败），不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应；

2. postHandle

后处理回调方法，实现处理器的后处理（但在渲染视图之前），此时我们可以通过modelAndView（模型和视图对象）对模型数据进行处理或对视图进行处理，modelAndView也可能为null。

3. afterCompletion

整个请求处理完毕回调方法，即在视图渲染完毕时回调，如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理，类似于try-catch-finally中的finally，但仅调用处理器执行链中


有时候我们可能只需要实现三个回调方法中的某一个，如果实现HandlerInterceptor接口的话，三个方法必须实现，不管你需不需要，此时spring提供了一个HandlerInterceptorAdapter适配器（种适配器设计模式的实现），允许我们只实现需要的回调方法。

public abstract class HandlerInterceptorAdapter implements AsyncHandlerInterceptor在abstract类中给出接口的默认实现，接下来想要使用拦截器，只需要继承这个抽象类即可，这时候就体现出了java8接口也能定义函数的好处了，给出接口、并给出接口的默认实现，用户只需要实现接口并覆盖自己感兴趣的方法即可。抽象类的实现方法会导致继承的类只能继承这个抽象类，没办法继承其它的类。

定义好处理的类即可在bean文件中声明这个类的存在，并定义它使用的范围：

```xml
    <mvc:interceptors>
        <!--全局拦截器-->
        <bean class="com.neptune8.Interceptor.DemoInterceptor"></bean>

        <!--特定url的拦截器-->
        <mvc:interceptor>
            <mvc:mapping path="/employee-module/getAllEmployees"/>
            <bean class="com.neptune8.Interceptor.MainPageInterceptor"></bean>
        </mvc:interceptor>
    </mvc:interceptors>
```