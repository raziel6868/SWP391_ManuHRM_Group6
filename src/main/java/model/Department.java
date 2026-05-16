package model;

public class Department {
    public enum DepartmentType {
        OFFICE, FACTORY
    }

    private Long id;
    private String name;
    private DepartmentType departmentType;
    private Long parentId;
    private Boolean isActive;
}
