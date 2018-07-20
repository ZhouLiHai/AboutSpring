package com.neptune8.controller;

import com.neptune8.dao.EmployeeDAO;
import com.neptune8.service.EmployeeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/employee-module")
public class EmployeeController {

	@Autowired
	EmployeeManager manager;

	@RequestMapping(value = "getAllEmployees", method = RequestMethod.GET)
	public String getAllEmployees(HttpServletRequest request, Model model) {

		// 在这里有下一个点，将来可以将session迁移到redis中，实现共享，应用察觉不出redis的存在。
		// System.out.println(request.getSession().getAttribute("id"));

		model.addAttribute("employees", manager.getAllEmployees());
		return "employeesListDisplay";
	}

}
