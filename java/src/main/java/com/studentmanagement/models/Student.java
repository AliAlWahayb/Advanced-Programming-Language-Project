package com.studentmanagement.models;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Student {
    private Integer studentId;
    private String name;
    private String course;
    private LocalDate enrollmentDate;

    public Student(String name, String course) {
        this.name = name;
        this.course = course;
        this.enrollmentDate = LocalDate.now();
    }

    public Student(Integer studentId, String name, String course, LocalDate enrollmentDate) {
        this.studentId = studentId;
        this.name = name;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public static Student fromMap(Map<String, Object> row) {
        Integer id = ((Number) row.get("student_id")).intValue();
        String name = (String) row.get("name");
        String course = (String) row.get("course");
        LocalDate date = LocalDate.parse((String) row.get("enrollment_date"));
        return new Student(id, name, course, date);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("student_id", studentId);
        map.put("name", name);
        map.put("course", course);
        map.put("enrollment_date", enrollmentDate.toString());
        return map;
    }

    public static boolean validateCourse(String course) {
        return course.matches("^[A-Za-z]{2}\\d{1,3}$");
    }

    @Override
    public String toString() {
        return String.format("Student(id=%d, name=%s, course=%s)", studentId, name, course);
    }
} 