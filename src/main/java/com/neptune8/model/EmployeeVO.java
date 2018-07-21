package com.neptune8.model;

import javax.validation.constraints.Size;
import java.io.Serializable;

public class EmployeeVO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Size(min = 5, max = 20)
	private Integer id;
	private String firstName;
	private String lastName;
	private String email;

	private DepartmentVO department;

	public DepartmentVO getDepartment() {
		return department;
	}

	public void setDepartment(DepartmentVO department) {
		this.department = department;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Override
	public String toString() {
		return "EmployeeVO{" + "id=" + id + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\''
				+ ", email='" + email + '\'' + '}';
	}
}
