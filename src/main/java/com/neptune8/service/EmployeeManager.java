package com.neptune8.service;

import com.neptune8.model.EmployeeVO;
import org.springframework.context.annotation.Bean;

import java.util.List;

public interface EmployeeManager {
	public List<EmployeeVO> getAllEmployees();
}
