package com.avianage.corejava.handson.annotations;


import com.avianage.corejava.handson.classes.Employee;

public class EmployeeService {

    @AuditLog(action = "CREATE", module = "Employee")
    public void addEmployee(Employee emp) {
        System.out.println("Adding: " + emp.getName());
    }

    @AuditLog(action = "UPDATE", module = "Employee")
    public void updateSalary(int id, double salary) {
        System.out.println("Updating salary for ID: " + id);
    }

    @AuditLog(action = "DELETE", module = "Employee", enabled = false)
    public void removeEmployee(int id) {
        System.out.println("Removing employee ID: " + id);
    }

    public void getEmployee(int id) {
        System.out.println("Fetching employee ID: " + id);   // no annotation
    }
}
