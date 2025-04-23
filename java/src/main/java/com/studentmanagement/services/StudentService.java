package com.studentmanagement.services;

import com.studentmanagement.database.DatabaseHandler;
import com.studentmanagement.models.Student;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.lang.Runtime;

public class StudentService {
    private final DatabaseHandler dbHandler;

    public StudentService(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    // profiling helper
    private void logProfile(String methodName, long startTime, long startMem) {
        long endTime = System.nanoTime();
        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.printf("[PROFILE] StudentService.%s: time=%.3fs; memDelta=%.2fKB%n", methodName, (endTime - startTime)/1e9, (endMem - startMem)/1024.0);
    }

    public Student addStudent(Student student) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (!Student.validateCourse(student.getCourse())) {
            throw new IllegalArgumentException("Bad course format: " + student.getCourse());
        }
        if (isDuplicateName(student.getName(), null)) {
            throw new IllegalArgumentException("Name '" + student.getName() + "' already exists.");
        }
        String sql = "INSERT INTO students (name, course) VALUES (?, ?);";
        int id = dbHandler.executeUpdate(sql, student.getName(), student.getCourse());
        Student result = getStudentById(id);
        logProfile("addStudent", startTime, startMem);
        return result;
    }

    public Student getStudentById(int studentId) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String sql = "SELECT * FROM students WHERE student_id = ?;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, studentId);
        Student result = rows.isEmpty() ? null : Student.fromMap(rows.get(0));
        logProfile("getStudentById", startTime, startMem);
        return result;
    }

    public List<Student> getAllStudents() throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String sql = "SELECT * FROM students ORDER BY student_id;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql);
        List<Student> result = rows.stream().map(Student::fromMap).collect(Collectors.toList());
        logProfile("getAllStudents", startTime, startMem);
        return result;
    }

    public Student updateStudent(Student student) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (student.getStudentId() == null) {
            throw new IllegalArgumentException("Can't update without ID");
        }
        if (!Student.validateCourse(student.getCourse())) {
            throw new IllegalArgumentException("Bad course format: " + student.getCourse());
        }
        if (isDuplicateName(student.getName(), student.getStudentId())) {
            throw new IllegalArgumentException("Name '" + student.getName() + "' already exists.");
        }
        String sql = "UPDATE students SET name = ?, course = ? WHERE student_id = ?;";
        int affected = dbHandler.executeUpdate(sql, student.getName(), student.getCourse(), student.getStudentId());
        Student result = affected == 0 ? null : getStudentById(student.getStudentId());
        logProfile("updateStudent", startTime, startMem);
        return result;
    }

    public int updateStudentsCourse(List<Integer> studentIds, String newCourse) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (!Student.validateCourse(newCourse)) {
            throw new IllegalArgumentException("Bad course format: " + newCourse);
        }
        if (studentIds == null || studentIds.isEmpty()) return 0;
        String placeholders = studentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("UPDATE students SET course = ? WHERE student_id IN (%s);", placeholders);
        List<Object> params = new ArrayList<>();
        params.add(newCourse);
        params.addAll(studentIds);
        int result = dbHandler.executeUpdate(sql, params.toArray());
        logProfile("updateStudentsCourse", startTime, startMem);
        return result;
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String sql = "DELETE FROM students WHERE student_id = ?;";
        int affected = dbHandler.executeUpdate(sql, studentId);
        boolean result = affected > 0;
        logProfile("deleteStudent", startTime, startMem);
        return result;
    }

    public int deleteStudents(List<Integer> studentIds) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        if (studentIds == null || studentIds.isEmpty()) return 0;
        String placeholders = studentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("DELETE FROM students WHERE student_id IN (%s);", placeholders);
        int result = dbHandler.executeUpdate(sql, studentIds.toArray());
        logProfile("deleteStudents", startTime, startMem);
        return result;
    }

    public List<Student> searchStudents(String searchTerm) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String sql = "SELECT * FROM students WHERE name LIKE ? OR course LIKE ? ORDER BY name;";
        String pattern = "%" + searchTerm + "%";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, pattern, pattern);
        List<Student> result = rows.stream().map(Student::fromMap).collect(Collectors.toList());
        logProfile("searchStudents", startTime, startMem);
        return result;
    }

    public List<Student> advancedSearch(Map<String, Object> criteria) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (criteria.containsKey("student_id")) {
            conditions.add("student_id = ?");
            params.add(criteria.get("student_id"));
        }
        if (criteria.containsKey("name")) {
            conditions.add("name LIKE ?");
            params.add("%" + criteria.get("name") + "%");
        }
        if (criteria.containsKey("course")) {
            conditions.add("course LIKE ?");
            params.add("%" + criteria.get("course") + "%");
        }
        if (criteria.containsKey("enrollment_date_from")) {
            conditions.add("enrollment_date >= ?");
            params.add(criteria.get("enrollment_date_from"));
        }
        if (criteria.containsKey("enrollment_date_to")) {
            conditions.add("enrollment_date <= ?");
            params.add(criteria.get("enrollment_date_to"));
        }
        if (conditions.isEmpty()) {
            return getAllStudents();
        }
        String whereClause = String.join(" AND ", conditions);
        String sql = String.format("SELECT * FROM students WHERE %s ORDER BY name;", whereClause);
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, params.toArray());
        List<Student> result = rows.stream().map(Student::fromMap).collect(Collectors.toList());
        logProfile("advancedSearch", startTime, startMem);
        return result;
    }

    public boolean isDuplicateName(String name, Integer excludeId) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        String sql;
        List<Map<String, Object>> result;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) as count FROM students WHERE name = ? AND student_id != ?;";
            result = dbHandler.executeQuery(sql, name, excludeId);
        } else {
            sql = "SELECT COUNT(*) as count FROM students WHERE name = ?;";
            result = dbHandler.executeQuery(sql, name);
        }
        int count = ((Number) result.get(0).get("count")).intValue();
        boolean dup = count > 0;
        logProfile("isDuplicateName", startTime, startMem);
        return dup;
    }
} 