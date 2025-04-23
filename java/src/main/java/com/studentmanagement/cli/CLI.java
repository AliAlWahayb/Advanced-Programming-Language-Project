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

// Profile support
@FunctionalInterface
interface Action { void run() throws Exception; }

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
                case "1" -> profile("addStudent", this::addStudent);
                case "2" -> profile("viewStudent", this::viewStudent);
                case "3" -> profile("listStudents", this::listStudents);
                case "4" -> profile("updateStudent", this::updateStudent);
                case "5" -> profile("deleteStudent", this::deleteStudent);
                case "6" -> profile("searchStudents", this::searchStudents);
                case "7" -> profile("advancedSearch", this::advancedSearch);
                case "8" -> profile("bulkOperations", this::bulkOperations);
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
                case "1" -> profile("recordAttendance", this::recordAttendance);
                case "2" -> profile("recordAttendanceBatch", this::recordAttendanceBatch);
                case "3" -> profile("viewAttendance", this::viewAttendance);
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
                case "1" -> profile("studentAttendanceReport", this::studentAttendanceReport);
                case "2" -> profile("dailyAttendanceReport", this::dailyAttendanceReport);
                case "3" -> profile("courseAttendanceReport", this::courseAttendanceReport);
                case "4" -> profile("monthlyAttendanceReport", this::monthlyAttendanceReport);
                case "5" -> profile("exportReport", this::exportReport);
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
        System.out.println("1. Select students from list");
        System.out.println("2. Select students by course");
        System.out.println("3. Select students by advanced search");
        System.out.print("Enter choice (1-3): ");
        String choice = scanner.nextLine().trim();
        List<Integer> studentIds = new ArrayList<>();
        if (choice.equals("1")) {
            List<Student> all = studentService.getAllStudents();
            all.forEach(s -> System.out.printf("%d: %s%n", s.getStudentId(), s.getName()));
            System.out.print("Enter IDs separated by comma: ");
            String in = scanner.nextLine().trim();
            for (String part : in.split(",")) {
                studentIds.add(Integer.parseInt(part.trim()));
            }
        } else if (choice.equals("2")) {
            System.out.print("Enter course: ");
            String course = scanner.nextLine().trim();
            List<Student> list = studentService.advancedSearch(Map.of("course", course));
            for (Student s : list) {
                studentIds.add(s.getStudentId());
            }
            System.out.printf("Selected %d students\n", studentIds.size());
        } else if (choice.equals("3")) {
            advancedSearch(); // advancedSearch lists and optionally bulk
            return;
        } else {
            System.out.println("Invalid choice."); pause(); return;
        }
        if (studentIds.isEmpty()) {
            System.out.println("No students selected."); pause(); return;
        }
        System.out.println("Operations: 1=Update course, 2=Delete, 3=Record attendance");
        System.out.print("Enter operation (1-3): ");
        String op = scanner.nextLine().trim();
        switch (op) {
            case "1":
                System.out.print("Enter new course: ");
                String newCourse = scanner.nextLine().trim();
                int updated = studentService.updateStudentsCourse(studentIds, newCourse);
                System.out.printf("Updated course for %d students\n", updated);
                break;
            case "2":
                int deleted = studentService.deleteStudents(studentIds);
                System.out.printf("Deleted %d students\n", deleted);
                break;
            case "3":
                System.out.print("Enter date (YYYY-MM-DD) [default: today]: ");
                String date = scanner.nextLine().trim(); if (date.isEmpty()) date = LocalDate.now().format(DATE_FORMAT);
                System.out.println("Select status: 1=Present, 2=Absent"); System.out.print("Choice: ");
                String stCh = scanner.nextLine().trim();
                AttendanceStatus stat = stCh.equals("1") ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
                for (Integer id : studentIds) {
                    attendanceService.recordAttendance(new Attendance(id, date, stat));
                }
                System.out.printf("Recorded attendance for %d students\n", studentIds.size());
                break;
            default:
                System.out.println("Invalid operation.");
        }
        pause();
    }

    // ========== Attendance Commands ==========
    private void recordAttendance() throws Exception {
        System.out.println("\n--- Record Attendance ---");
        System.out.print("Enter student ID: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        Student s = studentService.getStudentById(id);
        if (s == null) { System.out.println("No student found with ID " + id); pause(); return; }
        System.out.print("Enter date (YYYY-MM-DD) [default: today]: ");
        String date = scanner.nextLine().trim(); if (date.isEmpty()) date = LocalDate.now().format(DATE_FORMAT);
        System.out.println("1. Present\n2. Absent"); System.out.print("Choice: ");
        String ch = scanner.nextLine().trim();
        AttendanceStatus st = ch.equals("1") ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
        attendanceService.recordAttendance(new Attendance(id, date, st));
        System.out.println("Recorded " + st.getValue() + " for " + s.getName() + " on " + date);
        pause();
    }

    private void recordAttendanceBatch() throws Exception {
        System.out.println("\n--- Batch Attendance Recording ---");
        System.out.print("Enter date (YYYY-MM-DD) [default: today]: ");
        String date = scanner.nextLine().trim(); if (date.isEmpty()) date = LocalDate.now().format(DATE_FORMAT);
        List<Student> students = studentService.getAllStudents();
        if (students.isEmpty()) { System.out.println("No students found"); pause(); return; }
        for (Student s : students) {
            System.out.print(s.getName() + " (" + s.getStudentId() + "): [P/A/-]: ");
            String in = scanner.nextLine().trim().toUpperCase(); if (in.equals("-")) continue;
            if (in.equals("CANCEL")) { System.out.println("Cancelled"); pause(); return; }
            AttendanceStatus st = in.equals("P") ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT;
            attendanceService.recordAttendance(new Attendance(s.getStudentId(), date, st));
        }
        System.out.println("Batch recording complete for " + date);
        pause();
    }

    private void viewAttendance() throws Exception {
        System.out.println("\n--- View Attendance ---");
        System.out.println("1. By student\n2. By date"); System.out.print("Choice: ");
        String c = scanner.nextLine().trim();
        if (c.equals("1")) {
            System.out.print("Enter student ID: "); int id = Integer.parseInt(scanner.nextLine().trim());
            List<Attendance> recs = attendanceService.getStudentAttendance(id);
            if (recs.isEmpty()) { System.out.println("No records"); pause(); return; }
            System.out.printf("%-12s %-10s\n", "Date", "Status");
            recs.forEach(a -> System.out.printf("%-12s %-10s\n", a.getDate(), a.getStatus().getValue()));
        } else {
            System.out.print("Enter date (YYYY-MM-DD): "); String date = scanner.nextLine().trim();
            List<Attendance> recs = attendanceService.getAttendanceByDate(date);
            if (recs.isEmpty()) { System.out.println("No records"); pause(); return; }
            System.out.printf("%-5s %-30s %-10s\n", "ID", "Name", "Status");
            for (Attendance a : recs) {
                Student s = studentService.getStudentById(a.getStudentId());
                System.out.printf("%-5d %-30s %-10s\n", a.getStudentId(), s != null ? s.getName() : "?", a.getStatus().getValue());
            }
        }
        pause();
    }

    // ========== Report Commands ==========
    private void studentAttendanceReport() throws Exception {
        System.out.println("\n--- Student Attendance Report ---");
        System.out.print("Enter student ID: "); int id = Integer.parseInt(scanner.nextLine().trim());
        Map<String,Object> rep = reportService.generateStudentAttendanceReport(id);
        System.out.println(rep);
        System.out.print("Export to PDF? (y/n): "); if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Filename: "); String fn = scanner.nextLine().trim();
            String path = reportService.exportReportToPDF(rep, fn);
            System.out.println("Exported: " + path);
        }
        pause();
    }

    private void dailyAttendanceReport() throws Exception {
        System.out.println("\n--- Daily Attendance Report ---");
        System.out.print("Enter date (YYYY-MM-DD): "); String date = scanner.nextLine().trim();
        Map<String,Object> rep = reportService.generateDailyAttendanceReport(date);
        System.out.println(rep);
        pause();
    }

    private void courseAttendanceReport() throws Exception {
        System.out.println("\n--- Course Attendance Report ---");
        System.out.print("Enter course: "); String course = scanner.nextLine().trim();
        Map<String,Object> rep = reportService.generateCourseAttendanceReport(course);
        System.out.println(rep);
        pause();
    }

    private void monthlyAttendanceReport() throws Exception {
        System.out.println("\n--- Monthly Attendance Report ---");
        System.out.print("Enter year: "); int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Enter month (1-12): "); int month = Integer.parseInt(scanner.nextLine().trim());
        Map<String,Object> rep = reportService.generateMonthlyAttendanceReport(year, month);
        System.out.println(rep);
        pause();
    }

    private void exportReport() throws Exception {
        System.out.println("\n--- Export Report ---");
        System.out.println("1. Student Attendance Report");
        System.out.println("2. Daily Attendance Report");
        System.out.println("3. Course Attendance Report");
        System.out.println("4. Monthly Attendance Report");
        System.out.print("Enter choice (1-4): ");
        String choice = scanner.nextLine().trim();
        Map<String, Object> reportData;
        switch (choice) {
            case "1":
                System.out.print("Student ID: ");
                int sid = Integer.parseInt(scanner.nextLine().trim());
                reportData = reportService.generateStudentAttendanceReport(sid);
                break;
            case "2":
                System.out.print("Date (YYYY-MM-DD): ");
                String date = scanner.nextLine().trim();
                reportData = reportService.generateDailyAttendanceReport(date);
                break;
            case "3":
                System.out.print("Course: ");
                String course = scanner.nextLine().trim();
                reportData = reportService.generateCourseAttendanceReport(course);
                break;
            case "4":
                System.out.print("Year: "); int yr = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Month (1-12): "); int mo = Integer.parseInt(scanner.nextLine().trim());
                reportData = reportService.generateMonthlyAttendanceReport(yr, mo);
                break;
            default:
                System.out.println("Invalid choice."); pause(); return;
        }
        System.out.print("Export format: 1=CSV, 2=JSON, 3=PDF: ");
        String fmt = scanner.nextLine().trim();
        System.out.print("Filename [no extension]: ");
        String fn = scanner.nextLine().trim();
        String path;
        switch (fmt) {
            case "1": path = reportService.exportReportToCSV(reportData, fn); break;
            case "2": path = reportService.exportReportToJSON(reportData, fn); break;
            case "3": default: path = reportService.exportReportToPDF(reportData, fn); break;
        }
        System.out.println("Exported to: " + path);
        pause();
    }

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

    // Profile support
    private void profile(String name, Action action) {
        long start = System.nanoTime();
        Runtime rt = Runtime.getRuntime();
        long beforeMem = rt.totalMemory() - rt.freeMemory();
        try {
            action.run();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        long afterMem = rt.totalMemory() - rt.freeMemory();
        double elapsed = (System.nanoTime() - start) / 1e9;
        System.out.printf("[PROFILE] %s: runtime=%.3fs; memDelta=%.2fKB\n", name, elapsed, (afterMem - beforeMem)/1024.0);
    }
} 