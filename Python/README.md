# Student Management & Attendance System

A Python-based system for managing student records and tracking attendance.

## Features

- **Student Management**
  - Add, update, delete students
  - View student details
  - Search students by name or course

- **Attendance Management**
  - Record attendance for individual students
  - Batch record attendance for multiple students
  - View attendance by student or date

- **Reporting**
  - Generate student attendance reports
  - Generate daily attendance reports
  - Generate course-based attendance reports
  - Generate monthly attendance reports
  - Export reports to CSV or JSON formats

## System Architecture

### Database Design

- **Students Table**
  - `student_id` (Primary Key)
  - `name`
  - `course`
  - `enrollment_date`

- **Attendance Table**
  - `attendance_id` (Primary Key)
  - `student_id` (Foreign Key)
  - `date`
  - `status` (Present/Absent)

### Components

- **Database Layer**
  - SQLite database with optimized schema
  - Indexing for improved query performance

- **Service Layer**
  - Student management operations
  - Attendance tracking
  - Report generation

- **User Interface**
  - Command-line interface (CLI)

## Installation

1. Clone this repository:
   ```
   git clone https://github.com/your-username/student-attendance-system.git
   cd student-attendance-system
   ```

2. Install dependencies (no external dependencies required, uses standard library only)

## Usage

Run the application:

```
python main.py
```

### Available Commands

#### Student Management
- `add_student` - Add a new student
- `view_student` - View a student's details
- `list_students` - List all students
- `update_student` - Update a student's information
- `delete_student` - Delete a student
- `search_students` - Search for students by name or course

#### Attendance Management
- `record_attendance` - Record attendance for a student
- `record_attendance_batch` - Record attendance for multiple students at once
- `view_attendance` - View attendance records

#### Reports
- `student_report` - Generate a student attendance report
- `daily_report` - Generate a daily attendance report
- `course_report` - Generate a course attendance report
- `monthly_report` - Generate a monthly attendance report
- `export_report` - Export a previously generated report

#### System
- `help` - Show help message with available commands
- `exit` - Exit the program

## Data Export

Reports can be exported in two formats:
- CSV - For easy import into spreadsheet applications
- JSON - For integration with other systems

## Performance Optimization

The system includes several optimizations:
- Database indexing for faster queries
- Efficient batch operations for attendance recording
- Memory-efficient data processing for reports

## Future Enhancements

Potential future improvements:
- Graphical user interface (GUI)
- Web-based interface
- Additional report types
- Data visualization
- Email notifications
- Multi-user support with authentication
- Course schedule integration 