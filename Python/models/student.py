"""Student class"""

from dataclasses import dataclass
from typing import Optional
from datetime import date
import re


@dataclass
class Student:
    """Student class with basic info"""

    name: str
    course: str
    student_id: Optional[int] = None
    enrollment_date: Optional[str] = None

    def __str__(self) -> str:
        """print student info"""
        return f"Student(id={self.student_id}, name={self.name}, course={self.course})"

    @staticmethod
    def validate_course(course: str) -> bool:
        """check if course format is ok (2 letters + numbers)"""
        pattern = r'^[A-Za-z]{2}\d{1,3}$'
        return bool(re.match(pattern, course))

    @classmethod
    def from_db_dict(cls, db_dict: dict) -> 'Student':
        """make student object from database data"""
        return cls(
            student_id=db_dict.get('student_id'),
            name=db_dict.get('name'),
            course=db_dict.get('course'),
            enrollment_date=db_dict.get('enrollment_date')
        )

    def to_dict(self) -> dict:
        """convert to dict for database"""
        return {
            'student_id': self.student_id,
            'name': self.name,
            'course': self.course,
            'enrollment_date': self.enrollment_date
        }
