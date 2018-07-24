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

### Spring MVC 的 InternalResourceViewResolver 配置

在简单的spring MVC中，最后一步就是返回视图的名字，具体返回哪个视图是由具体的逻辑来选择的，这些视图由多个resolver来处理。These beans have to implement the ViewResolver interface for DispatcherServlet to auto-detect them. Spring MVC comes with several ViewResolver implementations. In this example, we will look at such a view resolver template i.e. InternalResourceViewResolver.

In most of the applications, views are mapped to a template’s name and location directly. InternalResourceViewResolver.To register InternalResourceViewResolver, you can declare a bean of this type in the web application context.

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
    <property name="viewClass" value="org.springframework.web.servlet.view.JstlView" />
    <property name="prefix" value="/WEB-INF/jsp/" />
    <property name="suffix" value=".jsp" />
</bean>
```

By default, InternalResourceViewResolver resolves view names into view objects of type JstlView if the JSTL library.然后可以指定视图的前缀和后缀，确定视图的类型和所在位置。

### Spring MVC ResourceBundleViewResolver Configuration Example

默认情况下ResourceBundleViewResolver从`views.properties`文件中获取参数，但我们可以通过配置basename属性来覆盖参数。

SpringMVC用于处理视图最重要的两个接口是ViewResolver和View。ViewResolver的主要作用是把一个逻辑上的视图名称解析为一个真正的视图，SpringMVC中用于把View对象呈现给客户端的是View对象本身，而ViewResolver只是把逻辑视图名称解析为对象的View对象。View接口的主要作用是用于处理视图，然后返回给客户端。

UrlBasedViewResolver：它是对ViewResolver的一种简单实现，而且继承了AbstractCachingViewResolver，主要就是提供的一种拼接URL的方式来解析视图，它可以让我们通过prefix属性指定一个指定的前缀，通过suffix属性指定一个指定的后缀，然后把返回的逻辑视图名称加上指定的前缀和后缀就是指定的视图URL了。如prefix=/WEB-INF/jsps/，suffix=.jsp，返回的视图名称viewName=test/indx，则UrlBasedViewResolver解析出来的视图URL就是/WEB-INF/jsps/test/index.jsp。

URLBasedViewResolver支持返回的视图名称中包含redirect:前缀，这样就可以支持URL在客户端的跳转，如当返回的视图名称是”redirect:test.do”的时候，URLBasedViewResolver发现返回的视图名称包含”redirect:”前缀，于是把返回的视图名称前缀”redirect:”去掉，取后面的test.do组成一个RedirectView，RedirectView中将把请求返回的模型属性组合成查询参数的形式组合到redirect的URL后面，然后调用HttpServletResponse对象的sendRedirect方法进行重定向。同样URLBasedViewResolver还支持forword:前缀，对于视图名称中包含forword:前缀的视图名称将会被封装成一个InternalResourceView对象，然后在服务器端利用RequestDispatcher的forword方式跳转到指定的地址。使用UrlBasedViewResolver的时候必须指定属性viewClass，表示解析成哪种视图，一般使用较多的就是InternalResourceView，利用它来展现jsp，但是当我们使用JSTL的时候我们必须使用JstlView。下面是一段UrlBasedViewResolver的定义，根据该定义，当返回的逻辑视图名称是test的时候，UrlBasedViewResolver将把逻辑视图名称加上定义好的前缀和后缀，即“/WEB-INF/test.jsp”，然后新建一个viewClass属性指定的视图类型予以返回，即返回一个url为“/WEB-INF/test.jsp”的InternalResourceView对象。

InternalResourceViewResolver：它是URLBasedViewResolver的子类，所以URLBasedViewResolver支持的特性它都支持。在实际应用中InternalResourceViewResolver也是使用的最广泛的一个视图解析器。那么InternalResourceViewResolver有什么自己独有的特性呢？单从字面意思来看，我们可以把InternalResourceViewResolver解释为内部资源视图解析器，这就是InternalResourceViewResolver的一个特性。InternalResourceViewResolver会把返回的视图名称都解析为InternalResourceView对象，InternalResourceView会把Controller处理器方法返回的模型属性都存放到对应的request属性中，然后通过RequestDispatcher在服务器端把请求forword重定向到目标URL。比如在InternalResourceViewResolver中定义了prefix=/WEB-INF/，suffix=.jsp，然后请求的Controller处理器方法返回的视图名称为test，那么这个时候InternalResourceViewResolver就会把test解析为一个InternalResourceView对象，先把返回的模型属性都存放到对应的HttpServletRequest属性中，然后利用RequestDispatcher在服务器端把请求forword到/WEB-INF/test.jsp。

```xml
<bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">  
   <property name="prefix" value="/WEB-INF/"/>  
   <property name="suffix" value=".jsp"></property>  
</bean>  
```

XmlViewResolver：它继承自AbstractCachingViewResolver抽象类，所以它也是支持视图缓存的。XmlViewResolver需要给定一个xml配置文件，该文件将使用和Spring的bean工厂配置文件一样的DTD定义，所以其实该文件就是用来定义视图的bean对象的。在该文件中定义的每一个视图的bean对象都给定一个名字，然后XmlViewResolver将根据Controller处理器方法返回的逻辑视图名称到XmlViewResolver指定的配置文件中寻找对应名称的视图bean用于处理视图。该配置文件默认是/WEB-INF/views.xml文件，如果不使用默认值的时候可以在XmlViewResolver的location属性中指定它的位置。XmlViewResolver还实现了Ordered接口，因此我们可以通过其order属性来指定在ViewResolver链中它所处的位置，order的值越小优先级越高。以下是使用XmlViewResolver的一个示例：

```xml
<bean class="org.springframework.web.servlet.view.XmlViewResolver">  
   <property name="location" value="/WEB-INF/views.xml"/>  
   <property name="order" value="1"/>  
</bean>  
```

在XmlViewResolver对应的配置文件中配置好所需要的视图定义。在下面的代码中我们就配置了一个名为internalResource的InternalResourceView，其url属性为“/index.jsp”。

```xml
<?xml version="1.0" encoding="UTF-8"?>  
<beans xmlns="http://www.springframework.org/schema/beans"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
    xsi:schemaLocation="http://www.springframework.org/schema/beans  
     http://www.springframework.org/schema/beans/spring-beans-3.0.xsd">  
    <bean id="internalResource" class="org.springframework.web.servlet.view.InternalResourceView">  
       <property name="url" value="/index.jsp"/>  
    </bean>  
</beans>  
```

定义一个返回的逻辑视图名称为在XmlViewResolver配置文件中定义的视图名称——internalResource。

```xml
@RequestMapping("/xmlViewResolver")  
public String testXmlViewResolver() {  
   return "internalResource";  
}  
```

这样当我们访问到上面定义好的testXmlViewResolver处理器方法的时候返回的逻辑视图名称为“internalResource”，这时候Spring就会到定义好的views.xml中寻找id或name为“internalResource”的bean对象予以返回，这里Spring找到的是一个url为“/index.jsp”的InternalResourceView对象。

BeanNameViewResolver：这个视图解析器跟XmlViewResolver有点类似，也是通过把返回的逻辑视图名称去匹配定义好的视图bean对象。不同点有二，一是BeanNameViewResolver要求视图bean对象都定义在Spring的application context中，而XmlViewResolver是在指定的配置文件中寻找视图bean对象，二是BeanNameViewResolver不会进行视图缓存。

```xml
<bean class="org.springframework.web.servlet.view.BeanNameViewResolver">  
   <property name="order" value="1"/>  
</bean>  
  
<bean id="test" class="org.springframework.web.servlet.view.InternalResourceView">  
   <property name="url" value="/index.jsp"/>  
</bean>  
```
 
ResourceBundleViewResolver：它和XmlViewResolver一样，也是继承自AbstractCachingViewResolver，但是它缓存的不是视图，这个会在后面有说到。和XmlViewResolver一样它也需要有一个配置文件来定义逻辑视图名称和真正的View对象的对应关系，不同的是ResourceBundleViewResolver的配置文件是一个属性文件，而且必须是放在classpath路径下面的，默认情况下这个配置文件是在classpath根目录下的views.properties文件，如果不使用默认值的话，则可以通过属性baseName或baseNames来指定。baseName只是指定一个基名称，Spring会在指定的classpath根目录下寻找以指定的baseName开始的属性文件进行View解析，如指定的baseName是base，那么base.properties、baseabc.properties等等以base开始的属性文件都会被Spring当做ResourceBundleViewResolver解析视图的资源文件。ResourceBundleViewResolver使用的属性配置文件的内容类似于这样：

```xml
resourceBundle.(class)=org.springframework.web.servlet.view.InternalResourceView  
resourceBundle.url=/index.jsp  
test.(class)=org.springframework.web.servlet.view.InternalResourceView  
test.url=/test.jsp  
```

在这个配置文件中我们定义了两个InternalResourceView对象，一个的名称是resourceBundle，对应URL是/index.jsp，另一个名称是test，对应的URL是/test.jsp。从这个定义来看我们可以知道resourceBundle是对应的视图名称，使用resourceBundle.(class)来指定它对应的视图类型，resourceBundle.url指定这个视图的url属性。会思考的读者看到这里可能会有这样一个问题：为什么resourceBundle的class属性要用小括号包起来，而它的url属性就不需要呢？这就需要从ResourceBundleViewResolver进行视图解析的方法来说了。

ResourceBundleViewResolver还是通过bean工厂来获得对应视图名称的视图bean对象来解析视图的。那么这些bean从哪里来呢？就是从我们定义的properties属性文件中来。在ResourceBundleViewResolver第一次进行视图解析的时候会先new一个BeanFactory对象，然后把properties文件中定义好的属性按照它自身的规则生成一个个的bean对象注册到该BeanFactory中，之后会把该BeanFactory对象保存起来，所以ResourceBundleViewResolver缓存的是BeanFactory，而不是直接的缓存从BeanFactory中取出的视图bean。然后会从bean工厂中取出名称为逻辑视图名称的视图bean进行返回。接下来就讲讲Spring通过properties文件生成bean的规则。它会把properties文件中定义的属性名称按最后一个点“.”进行分割，把点前面的内容当做是bean名称，点后面的内容当做是bean的属性。这其中有几个特别的属性，Spring把它们用小括号包起来了，这些特殊的属性一般是对应的attribute，但不是bean对象所有的attribute都可以这样用。其中(class)是一个，除了(class)之外，还有(scope)、(parent)、(abstract)、(lazy-init)。而除了这些特殊的属性之外的其他属性，Spring会把它们当做bean对象的一般属性进行处理，就是bean对象对应的property。所以根据上面的属性配置文件将生成如下两个bean对象：

```xml
<bean id="resourceBundle" class="org.springframework.web.servlet.view.InternalResourceView">  
   <property name="url" value="/index.jsp"/>  
</bean>  
  
<bean id="test" class="org.springframework.web.servlet.view.InternalResourceView">  
   <property name="url" value="/test.jsp"/>  
</bean>  
```


从ResourceBundleViewResolver使用的配置文件我们可以看出，它和XmlViewResolver一样可以解析多种不同类型的View，因为它们的View是通过配置的方式指定的，这也就意味着我们可以指定A视图是InternalResourceView，B视图是JstlView。

来看下面这个一个例子，我在SpringMVC的配置文件中定义了一个ResourceBundleViewResolver对象，指定其baseName为views，然后order为1。

```xml
<bean class="org.springframework.web.servlet.view.ResourceBundleViewResolver">  
   <property name="basename" value="views"/>  
   <property name="order" value="1"/>  
</bean>  
```

我在classpath的根目录下有两个属性文件，一个是views.properties，一个是views_abc.properties，它们的内容分别如下：

```xml
resourceBundle.(class)=org.springframework.web.servlet.view.InternalResourceView  
resourceBundle.url=/index.jsp  
test.(class)=org.springframework.web.servlet.view.InternalResourceView  
test.url=/test.jsp  
```

```xml
abc.(class)=org.springframework.web.servlet.view.InternalResourceView  
abc.url=/abc.jsp  
```

```java
@Controller  
@RequestMapping("/mytest")  
public class MyController {  
    @RequestMapping("resourceBundle")  
    public String resourceBundle() {  
       return "resourceBundle";  
    }  
  
    @RequestMapping("testResourceBundle")  
    public String testResourceBundle() {  
       return "test";  
    }  
  
    @RequestMapping("abc")  
    public String abc() {  
       return "abc";  
    }
}
```

那么当我们请求/mytest/resourceBundle.do的时候，ResourceBundleViewResolver会首先尝试着来解析该视图，这里Controller处理器方法返回的逻辑视图名称是resourceBundle，ResourceBundleViewResolver按照上面提到的解析方法进行解析，这个时候它发现它是可以解析的，然后就返回了一个url为/index.jsp的InternalResourceView对象。同样，请求/mytest/testResourceBundle.do返回的逻辑视图test和/mytest/abc.do返回的逻辑视图abc它都可以解析。

当我们把basename指定为包的形式，如“com.tiantian.views”，的时候Spring会按照点“.”划分为目录的形式，到classpath相应目录下去寻找basename开始的配置文件，如上面我们指定basename为“com.tiantian.views”，那么spring就会到classpath下的com/tiantian目录下寻找文件名以views开始的properties文件作为解析视图的配置文件。

FreeMarkerViewResolver、VolocityViewResolver：这两个视图解析器都是UrlBasedViewResolver的子类。FreeMarkerViewResolver会把Controller处理方法返回的逻辑视图解析为FreeMarkerView，而VolocityViewResolver会把返回的逻辑视图解析为VolocityView。因为这两个视图解析器类似，所以这里我就只挑FreeMarkerViewResolver来做一个简单的讲解。FreeMarkerViewResolver和VilocityViewResolver都继承了UrlBasedViewResolver。

对于FreeMarkerViewResolver而言，它会按照UrlBasedViewResolver拼接URL的方式进行视图路径的解析。但是使用FreeMarkerViewResolver的时候不需要我们指定其viewClass，因为FreeMarkerViewResolver中已经把viewClass定死为FreeMarkerView了。

我们先在SpringMVC的配置文件里面定义一个FreeMarkerViewResolver视图解析器，并定义其解析视图的order顺序为1。

<bean class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">  
   <property name="prefix" value="fm_"/>  
   <property name="suffix" value=".ftl"/>  
   <property name="order" value="1"/>  
</bean>

那么当我们请求的处理器方法返回一个逻辑视图名称viewName的时候，就会被该视图处理器加上前后缀解析为一个url为“fm_viewName.ftl”的FreeMarkerView对象。对于FreeMarkerView我们需要给定一个FreeMarkerConfig的bean对象来定义FreeMarker的配置信息。FreeMarkerConfig是一个接口，Spring已经为我们提供了一个实现，它就是FreeMarkerConfigurer。我们可以通过在SpringMVC的配置文件里面定义该bean对象来定义FreeMarker的配置信息，该配置信息将会在FreeMarkerView进行渲染的时候使用到。对于FreeMarkerConfigurer而言，我们最简单的配置就是配置一个templateLoaderPath，告诉Spring应该到哪里寻找FreeMarker的模板文件。这个templateLoaderPath也支持使用“classpath:”和“file:”前缀。当FreeMarker的模板文件放在多个不同的路径下面的时候，我们可以使用templateLoaderPaths属性来指定多个路径。在这里我们指定模板文件是放在“/WEB-INF/freemarker/template”下面的。

```xml
<bean class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">  
   <property name="templateLoaderPath" value="/WEB-INF/freemarker/template"/>  
</bean>  
```

```java
@Controller  
@RequestMapping("/mytest")  
public class MyController {  
  
    @RequestMapping("freemarker")  
    public ModelAndView freemarker() {  
       ModelAndView mav = new ModelAndView();  
       mav.addObject("hello", "andy");  
       mav.setViewName("freemarker");  
       return mav;  
    }
}  
```

由上面的定义我们可以看到这个Controller的处理器方法freemarker返回的逻辑视图名称是“freemarker”。那么如果我们需要把该freemarker视图交给FreeMarkerViewResolver来解析的话，我们就需要根据上面的定义，在模板路径下定义视图对应的模板，即在“/WEB-INF/freemarker/template”目录下建立fm_freemarker.ftl模板文件。

经过上面的定义当我们访问/mytest/freemarker.do的时候就会返回一个逻辑视图名称为“freemarker”的ModelAndView对象，根据定义好的视图解析的顺序，首先进行视图解析的是FreeMarkerViewResolver，这个时候FreeMarkerViewResolver会试着解析该视图，根据它自身的定义，它会先解析到该视图的URL为fm_freemarker.ftl，然后它会看是否能够实例化该视图对象，即在定义好的模板路径下是否有该模板存在，如果有则返回该模板对应的FreeMarkerView。

### 视图解析器链

在SpringMVC中可以同时定义多个ViewResolver视图解析器，然后它们会组成一个ViewResolver链。当Controller处理器方法返回一个逻辑视图名称后，ViewResolver链将根据其中ViewResolver的优先级来进行处理。所有的ViewResolver都实现了Ordered接口，在Spring中实现了这个接口的类都是可以排序的。在ViewResolver中是通过order属性来指定顺序的，默认都是最大值。所以我们可以通过指定ViewResolver的order属性来实现ViewResolver的优先级，order属性是Integer类型，order越小，对应的ViewResolver将有越高的解析视图的权利，所以第一个进行解析的将是ViewResolver链中order值最小的那个。当一个ViewResolver在进行视图解析后返回的View对象是null的话就表示该ViewResolver不能解析该视图，这个时候如果还存在其他order值比它大的ViewResolver就会调用剩余的ViewResolver中的order值最小的那个来解析该视图，依此类推。当ViewResolver在进行视图解析后返回的是一个非空的View对象的时候，就表示该ViewResolver能够解析该视图，那么视图解析这一步就完成了，后续的ViewResolver将不会再用来解析该视图。当定义的所有ViewResolver都不能解析该视图的时候，Spring就会抛出一个异常。

### springmvc多视图解释配置详解

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="
    http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
    http://www.springframework.org/schema/context
    http://www.springframework.org/schema/context/spring-context-3.0.xsd">
    <context:component-scan
        base-package="com.spring.action" />
    <!--  
        org.springframework.web.servlet.view.ResourceBundleViewResolver
        用于多个视图集成时,ResourceBundleViewResolver是通过解析资源文件来解析请求输出文件的。
        <property name="basename" value="views"></property>,即表示在/WEB-INF/classes路径下有一个
        views.properties文件,本例中views.properties的内容为
        welcome.(class)=org.springframework.web.servlet.view.velocity.VelocityView
        welcome.url=welcome.vm
        freemarker.(class)=org.springframework.web.servlet.view.freemarker.FreeMarkerView
        freemarker.url=freemarker.ftl
    -->
    <bean class="org.springframework.web.servlet.view.ResourceBundleViewResolver">
        <property name="basename" value="views"></property>
        <!-- 
            <property name="order" value="0"></property>
        -->
    </bean>
    
    <!-- jsp视图解析器 -->
    <bean id="jspViewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
        <property name="prefix" value="/"/>
        <property name="suffix" value=".jsp"/>
    </bean>        
    
    <!-- velocity视图解析器 -->
    <bean id="velocityViewResolver" class="org.springframework.web.servlet.view.velocity.VelocityViewResolver">
        <property name="cache" value="true"/>
        <property name="prefix" value="/"/>
        <property name="suffix" value=".vm"/>
    </bean>
    
    <!-- velocity环境配置 -->
    <bean id="velocityConfig" class="org.springframework.web.servlet.view.velocity.VelocityConfigurer">
        <!-- velocity配置文件路径 -->
        <property name="configLocation" value="/WEB-INF/velocity.properties"/>
        <!-- velocity模板路径 -->
        <property name="resourceLoaderPath" value="/WEB-INF/velocity/"/>
    </bean>
    
    <!-- FreeMarker环境配置 -->
    <bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
        <!-- freemarker模板位置 -->
        <property name="templateLoaderPath" value="/WEB-INF/freemarker/"/>
    </bean>
    
    <!-- FreeMarker视图解析 -->
    <bean id="freeMarkerViewResolver" class="org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver">
        <property name="cache" value="true"/>
        <property name="prefix" value="/"/>
        <property name="suffix" value=".ftl"/>
    </bean>
</beans>
```