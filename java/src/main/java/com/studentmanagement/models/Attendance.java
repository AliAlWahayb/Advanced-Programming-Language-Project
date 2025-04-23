package com.studentmanagement.models;

import java.util.Map;
import java.util.HashMap;

public class Attendance {
    private Integer attendanceId;
    private Integer studentId;
    private String date;
    private AttendanceStatus status;

    public Attendance(Integer studentId, String date, AttendanceStatus status) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

    public Attendance(Integer attendanceId, Integer studentId, String date, AttendanceStatus status) {
        this.attendanceId = attendanceId;
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

    public Integer getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Integer attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public static Attendance fromMap(Map<String, Object> row) {
        Integer id = ((Number) row.get("attendance_id")).intValue();
        Integer studentId = ((Number) row.get("student_id")).intValue();
        String date = (String) row.get("date");
        String statusStr = (String) row.get("status");
        AttendanceStatus status = AttendanceStatus.fromValue(statusStr);
        return new Attendance(id, studentId, date, status);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("attendance_id", attendanceId);
        map.put("student_id", studentId);
        map.put("date", date);
        map.put("status", status.getValue());
        return map;
    }
} 