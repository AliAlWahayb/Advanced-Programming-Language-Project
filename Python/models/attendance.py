"""attendance records"""

from dataclasses import dataclass
from typing import Optional
from enum import Enum
from datetime import date

from config import DATE_FORMAT


class AttendanceStatus(str, Enum):
    """attendance status"""
    PRESENT = 'Present'
    ABSENT = 'Absent'


@dataclass
class Attendance:
    """attendance record"""
    student_id: int
    date: str
    status: AttendanceStatus
    attendance_id: Optional[int] = None

    def __str__(self) -> str:
        return f"Attendance(id={self.attendance_id}, student_id={self.student_id}, " \
               f"date={self.date}, status={self.status})"

    @classmethod
    def from_db_dict(cls, db_dict: dict) -> 'Attendance':
        return cls(
            attendance_id=db_dict.get('attendance_id'),
            student_id=db_dict.get('student_id'),
            date=db_dict.get('date'),
            status=AttendanceStatus(db_dict.get('status'))
        )

    def to_dict(self) -> dict:
        return {
            'attendance_id': self.attendance_id,
            'student_id': self.student_id,
            'date': self.date,
            'status': self.status.value if isinstance(self.status, AttendanceStatus) else self.status
        }
