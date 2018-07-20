# AboutSpring
springMVC 学习项目

## 第一阶段

Tomcat的入口点是WEB-INF目录下的web.xml文件，在这个问题中定义servlet和servlet-mapping，springMVC通过拦截所有的请求，跳过了javaEE的servlet-mapping机制，而自己组织路由。

有关RequestMapping有三种用法：

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
 
 