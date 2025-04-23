package com.studentmanagement.services;

import com.studentmanagement.database.DatabaseHandler;
import com.studentmanagement.models.Attendance;
import com.studentmanagement.models.AttendanceStatus;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceService {
    private final DatabaseHandler dbHandler;

    public AttendanceService(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public Attendance recordAttendance(Attendance attendance) throws SQLException {
        String sql = "INSERT INTO attendance (student_id, date, status) VALUES (?, ?, ?) " +
                     "ON CONFLICT(student_id, date) DO UPDATE SET status = excluded.status;";
        int id = dbHandler.executeUpdate(sql,
            attendance.getStudentId(),
            attendance.getDate(),
            attendance.getStatus().getValue()
        );
        attendance.setAttendanceId(id);
        return getAttendanceById(id);
    }

    public Attendance getAttendanceById(int attendanceId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE attendance_id = ?;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, attendanceId);
        if (rows.isEmpty()) return null;
        return Attendance.fromMap(rows.get(0));
    }

    public List<Attendance> getStudentAttendance(int studentId) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE student_id = ? ORDER BY date DESC;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, studentId);
        return rows.stream().map(Attendance::fromMap).collect(Collectors.toList());
    }

    public List<Attendance> getAttendanceByDate(String date) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE date = ? ORDER BY student_id;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, date);
        return rows.stream().map(Attendance::fromMap).collect(Collectors.toList());
    }

    public List<Attendance> getAttendanceByDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM attendance WHERE date BETWEEN ? AND ? ORDER BY date, student_id;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, startDate, endDate);
        return rows.stream().map(Attendance::fromMap).collect(Collectors.toList());
    }

    public boolean deleteAttendance(int attendanceId) throws SQLException {
        String sql = "DELETE FROM attendance WHERE attendance_id = ?;";
        int affected = dbHandler.executeUpdate(sql, attendanceId);
        return affected > 0;
    }

    public Map<String, Object> getStudentAttendanceSummary(int studentId) throws SQLException {
        String sql = "SELECT " +
            "COUNT(*) as total_days, " +
            "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_days, " +
            "SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) as absent_days " +
            "FROM attendance WHERE student_id = ?;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sql, studentId);
        Map<String, Object> result = new HashMap<>();
        if (!rows.isEmpty()) {
            Map<String, Object> r = rows.get(0);
            int total = ((Number) r.get("total_days")).intValue();
            int present = ((Number) r.get("present_days")).intValue();
            int absent = ((Number) r.get("absent_days")).intValue();
            double percentage = total > 0 ? (present * 100.0 / total) : 0.0;
            result.put("student_id", studentId);
            result.put("total_days", total);
            result.put("present_days", present);
            result.put("absent_days", absent);
            result.put("attendance_percentage", Math.round(percentage * 100.0) / 100.0);
        }
        return result;
    }

    public Map<String, Object> getMonthlyAttendanceReport(int year, int month) throws SQLException {
        // Determine date range
        String startDate = String.format("%d-%02d-01", year, month);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int numDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String endDate = String.format("%d-%02d-%02d", year, month, numDays);

        Map<String, Object> report = new HashMap<>();
        // Aggregate by date
        String sqlDates = "SELECT date, " +
            "SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_count, " +
            "SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) as absent_count, " +
            "COUNT(*) as total_count " +
            "FROM attendance WHERE date BETWEEN ? AND ? GROUP BY date ORDER BY date;";
        List<Map<String, Object>> rows = dbHandler.executeQuery(sqlDates, startDate, endDate);
        Map<String, Map<String, Object>> days = new LinkedHashMap<>();
        int overallTotal = 0, overallPresent = 0;
        for (Map<String, Object> r : rows) {
            String date = (String) r.get("date");
            int present = ((Number) r.get("present_count")).intValue();
            int absent = ((Number) r.get("absent_count")).intValue();
            int total = ((Number) r.get("total_count")).intValue();
            double perc = total > 0 ? (present * 100.0 / total) : 0.0;
            Map<String, Object> data = new HashMap<>();
            data.put("present", present);
            data.put("absent", absent);
            data.put("total", total);
            data.put("present_percentage", Math.round(perc * 100.0) / 100.0);
            days.put(date, data);
            overallTotal += total;
            overallPresent += present;
        }
        report.put("year", year);
        report.put("month", month);
        report.put("days", days);
        report.put("total_students", dbHandler.executeQuery("SELECT COUNT(DISTINCT student_id) as count FROM attendance WHERE date BETWEEN ? AND ?;", startDate, endDate).get(0).get("count"));
        report.put("total_records", overallTotal);
        report.put("total_present", overallPresent);
        double overallPerc = overallTotal > 0 ? (overallPresent * 100.0 / overallTotal) : 0.0;
        report.put("overall_attendance_percentage", Math.round(overallPerc * 100.0) / 100.0);
        return report;
    }
} 