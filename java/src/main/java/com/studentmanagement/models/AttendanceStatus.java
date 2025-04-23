package com.studentmanagement.models;

public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent");

    private final String value;

    AttendanceStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AttendanceStatus fromValue(String status) {
        for (AttendanceStatus s : AttendanceStatus.values()) {
            if (s.value.equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + status);
    }
} 