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