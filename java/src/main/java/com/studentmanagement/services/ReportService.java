package com.studentmanagement.services;

import com.studentmanagement.database.DatabaseHandler;
import com.studentmanagement.models.Student;
import com.studentmanagement.models.Attendance;
import com.studentmanagement.services.AttendanceService;
import com.opencsv.CSVWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReportService {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportService(DatabaseHandler dbHandler, StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }

    public Map<String, Object> generateStudentAttendanceReport(int studentId) throws SQLException {
        Student student = studentService.getStudentById(studentId);
        if (student == null) throw new IllegalArgumentException("Student with ID " + studentId + " not found");
        List<Attendance> records = attendanceService.getStudentAttendance(studentId);
        Map<String, Object> summary = attendanceService.getStudentAttendanceSummary(studentId);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("student", student.toMap());
        report.put("attendance_summary", summary);
        List<Map<String, Object>> recList = new ArrayList<>();
        for (Attendance att : records) recList.add(att.toMap());
        report.put("attendance_records", recList);
        report.put("generated_at", LocalDateTime.now().format(dateTimeFormatter));
        return report;
    }

    public Map<String, Object> generateDailyAttendanceReport(String date) throws SQLException {
        List<Attendance> records = attendanceService.getAttendanceByDate(date);
        List<Student> students = studentService.getAllStudents();
        Map<Integer, String> recordMap = new HashMap<>();
        for (Attendance att : records) recordMap.put(att.getStudentId(), att.getStatus().getValue());

        List<Map<String, Object>> entries = new ArrayList<>();
        int presentCount = 0, absentCount = 0;
        for (Student s : students) {
            String status = recordMap.getOrDefault(s.getStudentId(), "Not Recorded");
            if ("Present".equals(status)) presentCount++;
            else if ("Absent".equals(status)) absentCount++;
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("student_id", s.getStudentId());
            entry.put("name", s.getName());
            entry.put("course", s.getCourse());
            entry.put("status", status);
            entries.add(entry);
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("date", date);
        report.put("present_count", presentCount);
        report.put("absent_count", absentCount);
        report.put("not_recorded", students.size() - records.size());
        report.put("total_students", students.size());
        double perc = students.isEmpty() ? 0.0 : (presentCount * 100.0 / students.size());
        report.put("attendance_percentage", Math.round(perc * 100.0) / 100.0);
        report.put("entries", entries);
        report.put("generated_at", LocalDateTime.now().format(dateTimeFormatter));
        return report;
    }

    public Map<String, Object> generateCourseAttendanceReport(String course) throws SQLException {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("course", course);
        List<Student> students = studentService.advancedSearch(criteria);
        if (students.isEmpty()) throw new IllegalArgumentException("No students found for course '" + course + "'");

        List<Map<String, Object>> studentReports = new ArrayList<>();
        int totalPresent = 0, totalDays = 0;
        for (Student s : students) {
            Map<String, Object> summary = attendanceService.getStudentAttendanceSummary(s.getStudentId());
            totalPresent += ((Number) summary.get("present_days")).intValue();
            totalDays += ((Number) summary.get("total_days")).intValue();
            Map<String, Object> rep = new LinkedHashMap<>();
            rep.put("student", s.toMap());
            rep.put("attendance_summary", summary);
            studentReports.add(rep);
        }
        double overallPerc = totalDays > 0 ? (totalPresent * 100.0 / totalDays) : 0.0;
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("course", course);
        report.put("student_count", students.size());
        report.put("overall_attendance_percentage", Math.round(overallPerc * 100.0) / 100.0);
        report.put("student_reports", studentReports);
        report.put("generated_at", LocalDateTime.now().format(dateTimeFormatter));
        return report;
    }

    public Map<String, Object> generateMonthlyAttendanceReport(int year, int month) throws SQLException {
        return attendanceService.getMonthlyAttendanceReport(year, month);
    }

    public String exportReportToCSV(Map<String, Object> reportData, String filename) throws IOException {
        if (!filename.endsWith(".csv")) filename += ".csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            if (reportData.containsKey("attendance_records")) {
                writer.writeNext(new String[]{"Date", "Status"});
                List<Map<String, Object>> records = (List<Map<String, Object>>) reportData.get("attendance_records");
                for (Map<String, Object> rec : records) {
                    writer.writeNext(new String[]{
                        rec.get("date").toString(),
                        rec.get("status").toString()
                    });
                }
            } else if (reportData.containsKey("entries")) {
                writer.writeNext(new String[]{"ID", "Name", "Course", "Status"});
                List<Map<String, Object>> entries = (List<Map<String, Object>>) reportData.get("entries");
                for (Map<String, Object> e : entries) {
                    writer.writeNext(new String[]{
                        e.get("student_id").toString(),
                        e.get("name").toString(),
                        e.get("course").toString(),
                        e.get("status").toString()
                    });
                }
            } else if (reportData.containsKey("student_reports")) {
                writer.writeNext(new String[]{"ID","Name","Course","TotalDays","PresentDays","AbsentDays","Attendance%"});
                List<Map<String, Object>> reps = (List<Map<String, Object>>) reportData.get("student_reports");
                for (Map<String, Object> rep : reps) {
                    Map<String, Object> stud = (Map<String, Object>) rep.get("student");
                    Map<String, Object> sum = (Map<String, Object>) rep.get("attendance_summary");
                    writer.writeNext(new String[]{
                        stud.get("student_id").toString(),
                        stud.get("name").toString(),
                        stud.get("course").toString(),
                        sum.get("total_days").toString(),
                        sum.get("present_days").toString(),
                        sum.get("absent_days").toString(),
                        sum.get("attendance_percentage").toString()
                    });
                }
            } else if (reportData.containsKey("days")) {
                writer.writeNext(new String[]{"Date","Present","Absent","Total","Percentage"});
                Map<String, Map<String, Object>> days = (Map<String, Map<String, Object>>) reportData.get("days");
                for (Map.Entry<String, Map<String, Object>> entry : days.entrySet()) {
                    Map<String, Object> d = entry.getValue();
                    writer.writeNext(new String[]{
                        entry.getKey(),
                        d.get("present").toString(),
                        d.get("absent").toString(),
                        d.get("total").toString(),
                        d.get("present_percentage").toString()
                    });
                }
            } else {
                throw new IllegalArgumentException("Unsupported report format for CSV export");
            }
        }
        return new File(filename).getAbsolutePath();
    }

    public String exportReportToJSON(Map<String, Object> reportData, String filename) throws IOException {
        if (!filename.endsWith(".json")) filename += ".json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), reportData);
        return new File(filename).getAbsolutePath();
    }

    public String exportReportToPDF(Map<String, Object> reportData, String filename) throws IOException, DocumentException {
        if (!filename.endsWith(".pdf")) filename += ".pdf";
        Document document = new Document(PageSize.LETTER);
        PdfWriter.getInstance(document, new FileOutputStream(filename));
        document.open();
        document.add(new Paragraph("Report generated: " + LocalDateTime.now().format(dateTimeFormatter)));
        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            document.add(new Paragraph(entry.getKey() + ": " + entry.getValue().toString()));
        }
        document.close();
        return new File(filename).getAbsolutePath();
    }
} 