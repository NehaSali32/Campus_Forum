package com.campusforum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "PRN or Roll No is required")
    private String prnRollNo;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String department = "Information Technology";
    private String semester = "Semester 1";
    private String year = "First Year";

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String email, String prnRollNo, String password, String department) {
        this.fullName = fullName;
        this.email = email;
        this.prnRollNo = prnRollNo;
        this.password = password;
        this.department = department;
    }

    public RegisterRequest(String fullName, String email, String prnRollNo, String password, String department, String semester, String year) {
        this.fullName = fullName;
        this.email = email;
        this.prnRollNo = prnRollNo;
        this.password = password;
        this.department = department;
        this.semester = semester;
        this.year = year;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPrnRollNo() {
        return prnRollNo;
    }

    public void setPrnRollNo(String prnRollNo) {
        this.prnRollNo = prnRollNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
