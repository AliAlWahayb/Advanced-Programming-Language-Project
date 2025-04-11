"""database tables and indexes"""

# Students table
CREATE_STUDENTS_TABLE = """
CREATE TABLE IF NOT EXISTS students (
    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    course TEXT NOT NULL,
    enrollment_date TEXT DEFAULT CURRENT_DATE
);
"""

# Attendance table
CREATE_ATTENDANCE_TABLE = """
CREATE TABLE IF NOT EXISTS attendance (
    attendance_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    status TEXT NOT NULL CHECK(status IN ('Present', 'Absent')),
    FOREIGN KEY (student_id) REFERENCES students (student_id) ON DELETE CASCADE,
    UNIQUE(student_id, date)
);
"""

# Tables list
TABLES = [CREATE_STUDENTS_TABLE, CREATE_ATTENDANCE_TABLE]

# Indexes
CREATE_ATTENDANCE_DATE_INDEX = """
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);
"""

CREATE_ATTENDANCE_STUDENT_INDEX = """
CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON attendance(student_id);
"""

# Index list
INDEXES = [CREATE_ATTENDANCE_DATE_INDEX, CREATE_ATTENDANCE_STUDENT_INDEX]
