package com.neptune8.service;

import com.neptune8.dao.EmployeeDAO;
import com.neptune8.model.EmployeeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeManagerImpl implements EmployeeManager {

	@Autowired
	EmployeeDAO dao;

	@Override
	public List<EmployeeVO> getAllEmployees() {
		return dao.getAllEmployees();
	}
}
