"""manages attendance data"""

from typing import List, Optional, Dict, Any, Tuple
from datetime import datetime, date
import calendar
import time
import tracemalloc
import functools

from database.db_handler import DatabaseHandler
from models.attendance import Attendance, AttendanceStatus
from config import DATE_FORMAT


def profile(func):
    """Decorator to measure time and memory usage of service methods"""
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start_mem, _ = tracemalloc.get_traced_memory()
        start_time = time.perf_counter()
        result = func(*args, **kwargs)
        end_time = time.perf_counter()
        end_mem, peak = tracemalloc.get_traced_memory()
        print(f"[PROFILE] {func.__name__}: runtime {(end_time - start_time):.4f}s; mem {(end_mem - start_mem)/1024:.2f}KiB; peak {peak/1024:.2f}KiB")
        return result
    return wrapper


class AttendanceService:
    """attendance operations"""

    @profile
    def __init__(self, db_handler: DatabaseHandler):
        self.db_handler = db_handler

    @profile
    def record_attendance(self, attendance: Attendance) -> Attendance:
        query = """
        INSERT INTO attendance (student_id, date, status)
        VALUES (?, ?, ?)
        ON CONFLICT(student_id, date) DO UPDATE SET status = excluded.status;
        """
        status_value = attendance.status.value if isinstance(
            attendance.status, AttendanceStatus) else attendance.status
        params = (attendance.student_id, attendance.date, status_value)

        attendance_id = self.db_handler.execute_write_query(query, params)
        attendance.attendance_id = attendance_id

        return self.get_attendance_by_id(attendance_id) or attendance

    @profile
    def get_attendance_by_id(self, attendance_id: int) -> Optional[Attendance]:
        query = "SELECT * FROM attendance WHERE attendance_id = ?;"
        result = self.db_handler.execute_query(query, (attendance_id,))

        if not result:
            return None

        return Attendance.from_db_dict(result[0])

    @profile
    def get_student_attendance(self, student_id: int) -> List[Attendance]:
        query = """
        SELECT * FROM attendance
        WHERE student_id = ?
        ORDER BY date DESC;
        """
        results = self.db_handler.execute_query(query, (student_id,))

        return [Attendance.from_db_dict(row) for row in results]

    @profile
    def get_attendance_by_date(self, date_str: str) -> List[Attendance]:
        query = """
        SELECT * FROM attendance
        WHERE date = ?
        ORDER BY student_id;
        """
        results = self.db_handler.execute_query(query, (date_str,))

        return [Attendance.from_db_dict(row) for row in results]

    @profile
    def get_attendance_by_date_range(self, start_date: str, end_date: str) -> List[Attendance]:
        query = """
        SELECT * FROM attendance
        WHERE date BETWEEN ? AND ?
        ORDER BY date, student_id;
        """
        results = self.db_handler.execute_query(query, (start_date, end_date))

        return [Attendance.from_db_dict(row) for row in results]

    @profile
    def delete_attendance(self, attendance_id: int) -> bool:
        query = "DELETE FROM attendance WHERE attendance_id = ?;"
        affected_rows = self.db_handler.execute_write_query(
            query, (attendance_id,))

        return affected_rows > 0

    @profile
    def get_student_attendance_summary(self, student_id: int) -> Dict[str, Any]:
        query = """
        SELECT
            COUNT(*) as total_days,
            SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_days,
            SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) as absent_days
        FROM attendance
        WHERE student_id = ?;
        """
        result = self.db_handler.execute_query(query, (student_id,))[0]

        total_days = result['total_days'] or 0
        present_days = result['present_days'] or 0
        absent_days = result['absent_days'] or 0

        attendance_percentage = (
            present_days / total_days * 100) if total_days > 0 else 0

        return {
            'student_id': student_id,
            'total_days': total_days,
            'present_days': present_days,
            'absent_days': absent_days,
            'attendance_percentage': round(attendance_percentage, 2)
        }

    @profile
    def get_monthly_attendance_report(self, year: int, month: int) -> Dict[str, Any]:
        _, num_days = calendar.monthrange(year, month)
        start_date = f"{year}-{month:02d}-01"
        end_date = f"{year}-{month:02d}-{num_days:02d}"

        attendance_by_date = {}

        query = """
        SELECT date, 
               SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as present_count,
               SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) as absent_count,
               COUNT(*) as total_count
        FROM attendance
        WHERE date BETWEEN ? AND ?
        GROUP BY date
        ORDER BY date;
        """

        results = self.db_handler.execute_query(query, (start_date, end_date))

        for row in results:
            attendance_by_date[row['date']] = {
                'present': row['present_count'],
                'absent': row['absent_count'],
                'total': row['total_count'],
                'present_percentage': (row['present_count'] / row['total_count'] * 100) if row['total_count'] > 0 else 0
            }

        query = """
        SELECT
            COUNT(DISTINCT student_id) as total_students,
            COUNT(*) as total_records,
            SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) as total_present,
            SUM(CASE WHEN status = 'Absent' THEN 1 ELSE 0 END) as total_absent
        FROM attendance
        WHERE date BETWEEN ? AND ?;
        """

        overall = self.db_handler.execute_query(
            query, (start_date, end_date))[0]

        total_records = overall['total_records'] or 0
        total_present = overall['total_present'] or 0

        overall_percentage = (
            total_present / total_records * 100) if total_records > 0 else 0

        return {
            'year': year,
            'month': month,
            'month_name': calendar.month_name[month],
            'days': attendance_by_date,
            'total_students': overall['total_students'] or 0,
            'total_records': total_records,
            'total_present': total_present,
            'total_absent': overall['total_absent'] or 0,
            'overall_attendance_percentage': round(overall_percentage, 2)
        }
