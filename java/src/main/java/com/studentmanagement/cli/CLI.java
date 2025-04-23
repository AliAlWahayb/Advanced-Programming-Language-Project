package com.studentmanagement.cli;

import com.studentmanagement.models.Student;
import com.studentmanagement.models.Attendance;
import com.studentmanagement.models.AttendanceStatus;
import com.studentmanagement.services.StudentService;
import com.studentmanagement.services.AttendanceService;
import com.studentmanagement.services.ReportService;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CLI {
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final ReportService reportService;
    private final Scanner scanner;
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CLI() throws Exception {
        this.studentService = new StudentService(new com.studentmanagement.database.DatabaseHandler());
        this.attendanceService = new AttendanceService(new com.studentmanagement.database.DatabaseHandler());
        this.reportService = new ReportService(new com.studentmanagement.database.DatabaseHandler(), studentService, attendanceService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        printHeader();
        showMainMenu();
    }

    private void printHeader() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Student Management & Attendance System".formatted().indent(0));
        System.out.println("=".repeat(80));
        System.out.println("Navigate using the menu numbers");
        System.out.println("-".repeat(80));
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Student Management");
            System.out.println("2. Attendance Management");
            System.out.println("3. Reports");
            System.out.println("9. Exit");
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> showStudentMenu();
                    case "2" -> showAttendanceMenu();
                    case "3" -> showReportsMenu();
                    case "9" -> { exitProgram(); return; }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showStudentMenu() throws Exception {
        while (true) {
            System.out.println("\n=== Student Management ===");
            System.out.println("1. Add Student");
            System.out.println("2. View Student");
            System.out.println("3. List All Students");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Search Students");
            System.out.println("7. Advanced Search");
            System.out.println("8. Bulk Operations");
            System.out.println("9. Back to Main Menu");
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("9")) return;
            switch (choice) {
                case "1" -> addStudent();
                case "2" -> viewStudent();
                case "3" -> listStudents();
                case "4" -> updateStudent();
                case "5" -> deleteStudent();
                case "6" -> searchStudents();
                case "7" -> advancedSearch();
                case "8" -> bulkOperations();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showAttendanceMenu() throws Exception {
        while (true) {
            System.out.println("\n=== Attendance Management ===");
            System.out.println("1. Record Attendance for a Student");
            System.out.println("2. Record Attendance for Multiple Students");
            System.out.println("3. View Attendance");
            System.out.println("9. Back to Main Menu");
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("9")) return;
            switch (choice) {
                case "1" -> recordAttendance();
                case "2" -> recordAttendanceBatch();
                case "3" -> viewAttendance();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showReportsMenu() throws Exception {
        while (true) {
            System.out.println("\n=== Reports ===");
            System.out.println("1. Student Attendance Report");
            System.out.println("2. Daily Attendance Report");
            System.out.println("3. Course Attendance Report");
            System.out.println("4. Monthly Attendance Report");
            System.out.println("5. Export Report");
            System.out.println("9. Back to Main Menu");
            System.out.print("\nEnter your choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("9")) return;
            switch (choice) {
                case "1" -> studentAttendanceReport();
                case "2" -> dailyAttendanceReport();
                case "3" -> courseAttendanceReport();
                case "4" -> monthlyAttendanceReport();
                case "5" -> exportReport();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ========== Student Commands ==========
    private void addStudent() throws Exception {
        System.out.println("\n--- Add New Student ---");
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter course (e.g. CS101): ");
        String course = scanner.nextLine().trim();
        if (!Student.validateCourse(course)) {
            System.out.println("Invalid course format."); return;
        }
        Student student = new Student(name, course);
        Student result = studentService.addStudent(student);
        System.out.println("Student added: " + result);
        pause();
    }

    private void viewStudent() throws Exception {
        System.out.println("\n--- View Student Details ---");
        System.out.print("Enter student ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Student s = studentService.getStudentById(id);
        if (s != null) System.out.println(s + ", enrolled: " + s.getEnrollmentDate());
        else System.out.println("No student found with ID " + id);
        pause();
    }

    private void listStudents() throws Exception {
        System.out.println("\n--- Student List ---");
        List<Student> list = studentService.getAllStudents();
        if (list.isEmpty()) { System.out.println("No students found."); pause(); return; }
        System.out.printf("Total: %d students\n", list.size());
        System.out.printf("%-5s %-30s %-10s\n", "ID", "Name", "Course");
        for (Student s : list) {
            System.out.printf("%-5d %-30s %-10s\n", s.getStudentId(), s.getName(), s.getCourse());
        }
        pause();
    }

    private void updateStudent() throws Exception {
        System.out.println("\n--- Update Student ---");
        System.out.print("Enter student ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Student existing = studentService.getStudentById(id);
        if (existing == null) {
            System.out.println("No student found with ID " + id);
            pause();
            return;
        }
        System.out.print("Enter new name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim(); if (name.isEmpty()) name = existing.getName();
        System.out.print("Enter new course [" + existing.getCourse() + "]: ");
        String course = scanner.nextLine().trim(); if (course.isEmpty()) course = existing.getCourse();
        if (!Student.validateCourse(course)) {
            System.out.println("Invalid course format.");
            pause();
            return;
        }
        existing.setName(name);
        existing.setCourse(course);
        Student updated = studentService.updateStudent(existing);
        System.out.println(updated != null ? "Student updated: " + updated : "Failed to update student.");
        pause();
    }

    private void deleteStudent() throws Exception {
        System.out.println("\n--- Delete Student ---");
        System.out.print("Enter student ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Student existing = studentService.getStudentById(id);
        if (existing == null) {
            System.out.println("No student found with ID " + id);
            pause();
            return;
        }
        System.out.print("Confirm delete (y/n): ");
        String resp = scanner.nextLine().trim().toLowerCase();
        if (resp.equals("y") || resp.equals("yes")) {
            boolean deleted = studentService.deleteStudent(id);
            System.out.println(deleted ? "Student deleted." : "Failed to delete.");
        } else {
            System.out.println("Deletion cancelled.");
        }
        pause();
    }

    private void searchStudents() throws Exception {
        System.out.println("\n--- Search Students ---");
        System.out.print("Enter search term: ");
        String term = scanner.nextLine().trim();
        List<Student> results = studentService.searchStudents(term);
        if (results.isEmpty()) System.out.println("No students found.");
        else {
            System.out.printf("%-5s %-30s %-10s\n", "ID", "Name", "Course");
            for (Student s : results) {
                System.out.printf("%-5d %-30s %-10s\n", s.getStudentId(), s.getName(), s.getCourse());
            }
        }
        pause();
    }

    private void advancedSearch() throws Exception {
        System.out.println("\n--- Advanced Student Search ---");
        Map<String, Object> criteria = new HashMap<>();
        System.out.print("Student ID (blank to skip): ");
        String sid = scanner.nextLine().trim(); if (!sid.isEmpty()) criteria.put("student_id", Integer.parseInt(sid));
        System.out.print("Name (partial): ");
        String name = scanner.nextLine().trim(); if (!name.isEmpty()) criteria.put("name", name);
        System.out.print("Course (partial): ");
        String course = scanner.nextLine().trim(); if (!course.isEmpty()) criteria.put("course", course);
        System.out.print("Enrollment date from (YYYY-MM-DD): ");
        String from = scanner.nextLine().trim(); if (!from.isEmpty()) criteria.put("enrollment_date_from", from);
        System.out.print("Enrollment date to (YYYY-MM-DD): ");
        String to = scanner.nextLine().trim(); if (!to.isEmpty()) criteria.put("enrollment_date_to", to);
        List<Student> results = studentService.advancedSearch(criteria);
        if (results.isEmpty()) System.out.println("No students found.");
        else {
            System.out.printf("%-5s %-30s %-10s %-12s\n", "ID", "Name", "Course", "EnrollDate");
            for (Student s : results) {
                System.out.printf("%-5d %-30s %-10s %-12s\n", s.getStudentId(), s.getName(), s.getCourse(), s.getEnrollmentDate());
            }
        }
        pause();
    }

    private void bulkOperations() throws Exception {
        System.out.println("\n--- Bulk Operations ---");
        System.out.println("Feature not yet implemented.");
        pause();
    }

    // ========== Attendance Commands ==========
    private void recordAttendance() throws Exception { /* ... */ }
    private void recordAttendanceBatch() throws Exception { /* ... */ }
    private void viewAttendance() throws Exception { /* ... */ }

    // ========== Report Commands ==========
    private void studentAttendanceReport() throws Exception { /* ... */ }
    private void dailyAttendanceReport() throws Exception { /* ... */ }
    private void courseAttendanceReport() throws Exception { /* ... */ }
    private void monthlyAttendanceReport() throws Exception { /* ... */ }
    private void exportReport() throws Exception { /* ... */ }

    private void showHelp() {
        System.out.println("\n--- Help ---");
        System.out.println("Commands mirror the menu options." +
                           "Use the number keys to navigate.");
        pause();
    }

    private void exitProgram() {
        System.out.println("\nExiting Student Management & Attendance System...");
        System.exit(0);
    }

    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
} 