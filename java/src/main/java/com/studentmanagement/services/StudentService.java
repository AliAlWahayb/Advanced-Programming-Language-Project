package com.studentmanagement.services;

import com.studentmanagement.database.DatabaseHandler;
import com.studentmanagement.models.Student;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class StudentService {
    private final DatabaseHandler dbHandler;

    public StudentService(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public Student addStudent(Student student) throws SQLException {
        if (!Student.validateCourse(student.getCourse())) {
            throw new IllegalArgumentException("Bad course format: " + student.getCourse());
        }
        if (isDuplicateName(student.getName(), null)) {
            throw new IllegalArgumentException("Name '" + student.getName() + "' already exists.");
        }
        String sql = "INSERT INTO students (name, course) VALUES (?, ?);";
        int id = dbHandler.executeUpdate(sql, student.getName(), student.getCourse());
        return getStudentById(id);
    }

    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, studentId);
        if (rows.isEmpty()) return null;
        return Student.fromMap(rows.get(0));
    }

    public List<Student> getAllStudents() throws SQLException {
        String sql = "SELECT * FROM students ORDER BY student_id;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql);
        return rows.stream().map(Student::fromMap).collect(Collectors.toList());
    }

    public Student updateStudent(Student student) throws SQLException {
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
        if (affected == 0) return null;
        return getStudentById(student.getStudentId());
    }

    public int updateStudentsCourse(List<Integer> studentIds, String newCourse) throws SQLException {
        if (!Student.validateCourse(newCourse)) {
            throw new IllegalArgumentException("Bad course format: " + newCourse);
        }
        if (studentIds == null || studentIds.isEmpty()) return 0;
        String placeholders = studentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("UPDATE students SET course = ? WHERE student_id IN (%s);", placeholders);
        List<Object> params = new ArrayList<>();
        params.add(newCourse);
        params.addAll(studentIds);
        return dbHandler.executeUpdate(sql, params.toArray());
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?;";
        int affected = dbHandler.executeUpdate(sql, studentId);
        return affected > 0;
    }

    public int deleteStudents(List<Integer> studentIds) throws SQLException {
        if (studentIds == null || studentIds.isEmpty()) return 0;
        String placeholders = studentIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("DELETE FROM students WHERE student_id IN (%s);", placeholders);
        return dbHandler.executeUpdate(sql, studentIds.toArray());
    }

    public List<Student> searchStudents(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM students WHERE name LIKE ? OR course LIKE ? ORDER BY name;";
        String pattern = "%" + searchTerm + "%";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, pattern, pattern);
        return rows.stream().map(Student::fromMap).collect(Collectors.toList());
    }

    public List<Student> advancedSearch(Map<String, Object> criteria) throws SQLException {
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
        return rows.stream().map(Student::fromMap).collect(Collectors.toList());
    }

    public boolean isDuplicateName(String name, Integer excludeId) throws SQLException {
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
        return count > 0;
    }
} 