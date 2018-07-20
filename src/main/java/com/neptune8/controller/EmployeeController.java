package com.neptune8.controller;

import com.neptune8.dao.EmployeeDAO;
import com.neptune8.service.EmployeeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/employee-module")
public class EmployeeController {

	@Autowired
	EmployeeManager manager;

	@RequestMapping(value = "getAllEmployees", method = RequestMethod.GET)
	public String getAllEmployees(Model model) {
		model.addAttribute("employees", manager.getAllEmployees());
		return "employeesListDisplay";
	}

}