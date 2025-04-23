package com.studentmanagement.database;

import java.sql.*;
import java.util.*;
import java.lang.Runtime;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:student_management.db";
    private static final String CREATE_STUDENTS_TABLE =
        "CREATE TABLE IF NOT EXISTS students (" +
        "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "name TEXT NOT NULL, " +
        "course TEXT NOT NULL, " +
        "enrollment_date TEXT DEFAULT CURRENT_DATE" +
        ");";
    private static final String CREATE_ATTENDANCE_TABLE =
        "CREATE TABLE IF NOT EXISTS attendance (" +
        "attendance_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "student_id INTEGER NOT NULL, " +
        "date TEXT NOT NULL, " +
        "status TEXT NOT NULL CHECK(status IN ('Present','Absent')), " +
        "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
        "UNIQUE(student_id, date)" +
        ");";
    private static final String CREATE_ATTENDANCE_DATE_INDEX =
        "CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);";
    private static final String CREATE_ATTENDANCE_STUDENT_INDEX =
        "CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON attendance(student_id);";

    public DatabaseHandler() throws SQLException {
        initializeDatabase();
    }

    private void initializeDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(CREATE_STUDENTS_TABLE);
            stmt.execute(CREATE_ATTENDANCE_TABLE);
            stmt.execute(CREATE_ATTENDANCE_DATE_INDEX);
            stmt.execute(CREATE_ATTENDANCE_STUDENT_INDEX);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        long endTime = System.nanoTime();
        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.out.printf("[PROFILE] executeQuery: time=%.3fs, memDelta=%.2fKB%n", (endTime - startTime)/1e9, (endMem - startMem)/1024.0);
        return rows;
    }

    public int executeUpdate(String sql, Object... params) throws SQLException {
        long startTime = System.nanoTime();
        long startMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setParameters(pstmt, params);
            int affected = pstmt.executeUpdate();
            if (sql.trim().toUpperCase().startsWith("INSERT")) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        long endTime = System.nanoTime();
                        long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        System.out.printf("[PROFILE] executeUpdate (insert): time=%.3fs, memDelta=%.2fKB%n", (endTime - startTime)/1e9, (endMem - startMem)/1024.0);
                        return keys.getInt(1);
                    }
                }
            }
            long endTime = System.nanoTime();
            long endMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            System.out.printf("[PROFILE] executeUpdate: time=%.3fs, memDelta=%.2fKB%n", (endTime - startTime)/1e9, (endMem - startMem)/1024.0);
            return affected;
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }
    }
} 