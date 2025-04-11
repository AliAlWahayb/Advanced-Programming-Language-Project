"""handles database stuff for students"""

from typing import List, Optional, Dict, Any, Tuple

from database.db_handler import DatabaseHandler
from models.student import Student


class StudentService:
    """does database operations for students"""

    def __init__(self, db_handler: DatabaseHandler):
        """setup with database connection"""
        self.db_handler = db_handler

    def add_student(self, student: Student) -> Student:
        """add new student to database"""
        # check course format
        if not Student.validate_course(student.course):
            raise ValueError(
                f"Bad course format: {student.course}. Should be like CS101.")

        # check duplicates
        if self.is_duplicate_name(student.name):
            raise ValueError(
                f"Name '{student.name}' already exists. Try adding initials.")

        query = """
        INSERT INTO students (name, course)
        VALUES (?, ?);
        """
        params = (student.name, student.course)

        student_id = self.db_handler.execute_write_query(query, params)
        student.student_id = student_id

        return self.get_student_by_id(student_id)

    def get_student_by_id(self, student_id: int) -> Optional[Student]:
        """get one student by ID"""
        query = "SELECT * FROM students WHERE student_id = ?;"
        result = self.db_handler.execute_query(query, (student_id,))

        if not result:
            return None

        return Student.from_db_dict(result[0])

    def get_all_students(self) -> List[Student]:
        """get all students"""
        query = "SELECT * FROM students ORDER BY student_id;"
        results = self.db_handler.execute_query(query)

        return [Student.from_db_dict(row) for row in results]

    def update_student(self, student: Student) -> Optional[Student]:
        """update student info"""
        if student.student_id is None:
            raise ValueError("Can't update without ID")

        # check course
        if not Student.validate_course(student.course):
            raise ValueError(
                f"Bad course format: {student.course}. Should be like CS101.")

        # check duplicate name
        if self.is_duplicate_name(student.name, student.student_id):
            raise ValueError(
                f"Name '{student.name}' already exists. Try adding initials.")

        query = """
        UPDATE students
        SET name = ?, course = ?
        WHERE student_id = ?;
        """
        params = (student.name, student.course, student.student_id)

        affected_rows = self.db_handler.execute_write_query(query, params)
        if affected_rows == 0:
            return None

        return self.get_student_by_id(student.student_id)

    def update_students_course(self, student_ids: List[int], new_course: str) -> int:
        """update course for multiple students"""
        # check course format
        if not Student.validate_course(new_course):
            raise ValueError(
                f"Bad course format: {new_course}. Should be like CS101.")

        # convert ids to sql format
        id_placeholder = ','.join('?' for _ in student_ids)

        query = f"""
        UPDATE students
        SET course = ?
        WHERE student_id IN ({id_placeholder});
        """

        params = (new_course,) + tuple(student_ids)

        return self.db_handler.execute_write_query(query, params)

    def delete_student(self, student_id: int) -> bool:
        """delete a student"""
        query = "DELETE FROM students WHERE student_id = ?;"
        affected_rows = self.db_handler.execute_write_query(
            query, (student_id,))

        return affected_rows > 0

    def delete_students(self, student_ids: List[int]) -> int:
        """delete multiple students"""
        id_placeholder = ','.join('?' for _ in student_ids)

        query = f"""
        DELETE FROM students
        WHERE student_id IN ({id_placeholder});
        """

        return self.db_handler.execute_write_query(query, tuple(student_ids))

    def search_students(self, search_term: str) -> List[Student]:
        """search by name or course"""
        query = """
        SELECT * FROM students
        WHERE name LIKE ? OR course LIKE ?
        ORDER BY name;
        """
        search_pattern = f"%{search_term}%"
        params = (search_pattern, search_pattern)

        results = self.db_handler.execute_query(query, params)

        return [Student.from_db_dict(row) for row in results]

    def advanced_search(self, criteria: Dict[str, Any]) -> List[Student]:
        """search with filters"""
        params = []
        conditions = []

        # build query
        if 'student_id' in criteria and criteria['student_id']:
            conditions.append("student_id = ?")
            params.append(criteria['student_id'])

        if 'name' in criteria and criteria['name']:
            conditions.append("name LIKE ?")
            params.append(f"%{criteria['name']}%")

        if 'course' in criteria and criteria['course']:
            conditions.append("course LIKE ?")
            params.append(f"%{criteria['course']}%")

        if 'enrollment_date_from' in criteria and criteria['enrollment_date_from']:
            conditions.append("enrollment_date >= ?")
            params.append(criteria['enrollment_date_from'])

        if 'enrollment_date_to' in criteria and criteria['enrollment_date_to']:
            conditions.append("enrollment_date <= ?")
            params.append(criteria['enrollment_date_to'])

        # if no conditions, get everything
        if not conditions:
            return self.get_all_students()

        query = f"""
        SELECT * FROM students
        WHERE {' AND '.join(conditions)}
        ORDER BY name;
        """

        results = self.db_handler.execute_query(query, tuple(params))

        return [Student.from_db_dict(row) for row in results]

    def is_duplicate_name(self, name: str, exclude_id: Optional[int] = None) -> bool:
        """check if name already exists"""
        if exclude_id:
            query = "SELECT COUNT(*) as count FROM students WHERE name = ? AND student_id != ?;"
            result = self.db_handler.execute_query(query, (name, exclude_id))
        else:
            query = "SELECT COUNT(*) as count FROM students WHERE name = ?;"
            result = self.db_handler.execute_query(query, (name,))

        return result[0]['count'] > 0
