package com.neptune8.Interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 有时候我们可能只需要实现三个回调方法中的某一个，如果实现HandlerInterceptor接口的话，
 * 三个方法必须实现，不管你需不需要，此时spring提供了一个HandlerInterceptorAdapter适配器（种适配器设计模式的实现），
 * 允许我们只实现需要的回调方法。
 *
 * public abstract class HandlerInterceptorAdapter implements AsyncHandlerInterceptor
 * 在abstract类中给出接口的默认实现，接下来想要使用拦截器，只需要继承这个抽象类即可，
 * 这时候就体现出了java8接口也能定义函数的好处了，给出接口、并给出接口的默认实现，
 * 用户只需要实现接口并覆盖自己感兴趣的方法即可。
 *
 * 抽象类的实现方法会导致继承的类只能继承这个抽象类，没办法继承其它的类。
 */

public class DemoInterceptor implements HandlerInterceptor {
	/**
	 * 预处理回调方法，实现处理器的预处理（如检查登陆），第三个参数为响应的处理器，自定义Controller
	 * 返回值：true表示继续流程（如调用下一个拦截器或处理器）；false表示流程中断（如登录检查失败），
	 * 不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应；
	 */
	@Override
	public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o)
			throws Exception {
		System.out.println("Inside pre handler.");
		return true;
	}

	/**
	 * 后处理回调方法，实现处理器的后处理（但在渲染视图之前），
	 * 此时我们可以通过modelAndView（模型和视图对象）对模型数据进行处理或对视图进行处理，modelAndView也可能为null。
	 */
	@Override
	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
			ModelAndView modelAndView) throws Exception {
		System.out.println("Inside post handler.");
	}

	/**
	 * 整个请求处理完毕回调方法，即在视图渲染完毕时回调，
	 * 如性能监控中我们可以在此记录结束时间并输出消耗时间，还可以进行一些资源清理，
	 * 类似于try-catch-finally中的finally，但仅调用处理器执行链中
	 */
	@Override
	public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			Object o, Exception e) throws Exception {
			System.out.println("Inside after handler.");
	}
}
