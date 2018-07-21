package com.neptune8.validator;

import com.neptune8.model.EmployeeVO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class EmployeeValidator implements Validator {
	@Override
	public boolean supports(Class<?> aClass) {
		/**
		 * 有两个Class类型的类象，一个是调用isAssignableFrom方法的类对象（后称对象a），以及方法中作为参数的这个类对象（称之为对象b），
		 * 这两个对象如果满足以下条件则返回true，否则返回false：
		 *
		 *     a对象所对应类信息是b对象所对应的类信息的父类或者是父接口，简单理解即a是b的父类或接口
		 *     a对象所对应类信息与b对象所对应的类信息相同，简单理解即a和b为同一个类或同一个接口
		 */
		return EmployeeVO.class.isAssignableFrom(aClass);
	}

	@Override
	public void validate(Object o, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "error.firstName", "First name is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "error.lastName", "Last name is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "error.email", "Email name is required.");
	}
}
