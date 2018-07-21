package com.neptune8.controller;

import com.neptune8.convertor.DepartmentEditor;
import com.neptune8.model.DepartmentVO;
import com.neptune8.model.EmployeeVO;
import com.neptune8.service.EmployeeManager;
import com.neptune8.validator.EmployeeValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.validation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/employee-module/")
@SessionAttributes("employee")
public class EmployeeController {

	@Autowired
	private EmployeeManager manager;

	@Autowired
	private EmployeeValidator validator;

	private Validator validator_303;

	// 需要使用工厂方法生成
	public EmployeeController() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator_303 = validatorFactory.getValidator();
	}

	@RequestMapping(value = "addNew", method = RequestMethod.GET)
	public String setupForm(Model model) {
		EmployeeVO employeeVO = new EmployeeVO();
		model.addAttribute("employee", employeeVO);
		return "addEmployee";
	}

	@RequestMapping(value = "getAllEmployees", method = RequestMethod.GET)
	public String getAllEmployees(HttpServletRequest request, Model model) {

		// 在这里有下一个点，将来可以将session迁移到redis中，实现共享，应用察觉不出redis的存在。
		// System.out.println(request.getSession().getAttribute("id"));

		model.addAttribute("employees", manager.getAllEmployees());
		return "employeesListDisplay";
	}

	/**
	 * 在SpringMVC中，bean中定义了Date，double等类型，如果没有做任何处理的话，日期以及double都无法绑定。
	 * 使用InitBinder标记，将editor注入到controller，如果遇到需要转换的对象，则调用对应的editor。
	 * DepartmentVO.class 是需要转换的对象，DepartmentEditor 是我们自己编写的转换方法。
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(DepartmentVO.class, new DepartmentEditor());
	}

	@RequestMapping(value = "addNew", method = RequestMethod.POST)
	public String submitForm(@ModelAttribute("employee") EmployeeVO employeeVO, BindingResult result,
			SessionStatus status, Model model) {


		//		validator.validate(employeeVO, result);

		Set<ConstraintViolation<EmployeeVO>> violations = validator_303.validate(employeeVO);

		for (ConstraintViolation<EmployeeVO> violation : violations) {
			String propertyPath = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			// Add JSR-303 errors to BindingResult
			// This allows Spring to display them in view via a FieldError
			result.addError(new FieldError("employee", propertyPath, "Invalid " + propertyPath + "(" + message + ")"));
		}

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

	@RequestMapping(value = "addSuccess", method = RequestMethod.GET)
	public String addSuccess() {
		return "addSuccess";
	}

	// TODO: ModelAttribute貌似是直接能在模板引擎中使用的数据，不用model.add。
	@ModelAttribute("allDepartments")
	public List<DepartmentVO> populateDepartments() {
		ArrayList<DepartmentVO> departments = new ArrayList<DepartmentVO>();
		departments.add(new DepartmentVO(-1, "Select Department"));
		departments.add(new DepartmentVO(1, "Human Resource"));
		departments.add(new DepartmentVO(2, "Finance"));
		departments.add(new DepartmentVO(3, "Information Technology"));
		return departments;
	}

}
