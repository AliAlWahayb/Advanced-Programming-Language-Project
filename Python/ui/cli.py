"""command line interface for the program"""

import os
import sys
import datetime
from typing import List, Dict, Any, Optional, Callable
import calendar

from database.db_handler import DatabaseHandler
from models.student import Student
from models.attendance import Attendance, AttendanceStatus
from services.student_service import StudentService
from services.attendance_service import AttendanceService
from services.report_service import ReportService
from config import DATE_FORMAT


class CLI:
    """command-line menu interface"""

    def __init__(self):
        """setup connections to database"""
        self.db_handler = DatabaseHandler()
        self.student_service = StudentService(self.db_handler)
        self.attendance_service = AttendanceService(self.db_handler)
        self.report_service = ReportService(
            self.db_handler,
            self.student_service,
            self.attendance_service
        )

        # commands dictionary
        self.commands = {
            # Student management
            'add_student': self.add_student,
            'view_student': self.view_student,
            'list_students': self.list_students,
            'update_student': self.update_student,
            'delete_student': self.delete_student,
            'search_students': self.search_students,
            'advanced_search': self.advanced_search,
            'bulk_operations': self.bulk_operations,

            # Attendance management
            'record_attendance': self.record_attendance,
            'view_attendance': self.view_attendance,
            'record_attendance_batch': self.record_attendance_batch,

            # Reports
            'student_report': self.student_attendance_report,
            'daily_report': self.daily_attendance_report,
            'course_report': self.course_attendance_report,
            'monthly_report': self.monthly_attendance_report,
            'export_report': self.export_report,

            # System
            'help': self.show_help,
            'exit': self.exit_program
        }

        # main menu options
        self.main_menu = [
            {"id": 1, "name": "Student Management",
                "submenu": self.show_student_menu},
            {"id": 2, "name": "Attendance Management",
                "submenu": self.show_attendance_menu},
            {"id": 3, "name": "Reports", "submenu": self.show_reports_menu},
            {"id": 9, "name": "Exit", "command": "exit"}
        ]

        # student menu
        self.student_menu = [
            {"id": 1, "name": "Add Student", "command": "add_student"},
            {"id": 2, "name": "View Student", "command": "view_student"},
            {"id": 3, "name": "List All Students", "command": "list_students"},
            {"id": 4, "name": "Update Student", "command": "update_student"},
            {"id": 5, "name": "Delete Student", "command": "delete_student"},
            {"id": 6, "name": "Search Students", "command": "search_students"},
            {"id": 7, "name": "Advanced Search", "command": "advanced_search"},
            {"id": 8, "name": "Bulk Operations", "command": "bulk_operations"},
            {"id": 9, "name": "Back to Main Menu", "back": True}
        ]

        # attendance menu
        self.attendance_menu = [
            {"id": 1, "name": "Record Attendance for a Student",
                "command": "record_attendance"},
            {"id": 2, "name": "Record Attendance for Multiple Students",
                "command": "record_attendance_batch"},
            {"id": 3, "name": "View Attendance", "command": "view_attendance"},
            {"id": 9, "name": "Back to Main Menu", "back": True}
        ]

        # reports menu
        self.reports_menu = [
            {"id": 1, "name": "Student Attendance Report",
                "command": "student_report"},
            {"id": 2, "name": "Daily Attendance Report", "command": "daily_report"},
            {"id": 3, "name": "Course Attendance Report", "command": "course_report"},
            {"id": 4, "name": "Monthly Attendance Report",
                "command": "monthly_report"},
            {"id": 5, "name": "Export Report", "command": "export_report"},
            {"id": 9, "name": "Back to Main Menu", "back": True}
        ]

    def start(self):
        """start the program"""
        self._print_header()
        self.show_main_menu()

    def show_main_menu(self):
        """show main menu options"""
        while True:
            print("\n=== Main Menu ===")
            for option in self.main_menu:
                print(f"{option['id']}. {option['name']}")

            choice = input("\nEnter your choice: ").strip()

            try:
                choice = int(choice)
                selected = next(
                    (opt for opt in self.main_menu if opt['id'] == choice), None)

                if selected:
                    if "command" in selected:
                        self.commands[selected["command"]]()
                    elif "submenu" in selected:
                        selected["submenu"]()
                else:
                    print("Invalid choice. Please try again.")
            except ValueError:
                print("Please enter a number.")
            except KeyboardInterrupt:
                print("\nExiting program...")
                sys.exit(0)
            except Exception as e:
                print(f"Error: {str(e)}")

    def show_student_menu(self):
        """show student menu"""
        while True:
            print("\n=== Student Management ===")
            for option in self.student_menu:
                print(f"{option['id']}. {option['name']}")

            choice = input("\nEnter your choice: ").strip()

            try:
                choice = int(choice)
                selected = next(
                    (opt for opt in self.student_menu if opt['id'] == choice), None)

                if selected:
                    if "back" in selected and selected["back"]:
                        return
                    elif "command" in selected:
                        self.commands[selected["command"]]()
                else:
                    print("Invalid choice. Please try again.")
            except ValueError:
                print("Please enter a number.")

    def show_attendance_menu(self):
        """show attendance menu"""
        while True:
            print("\n=== Attendance Management ===")
            for option in self.attendance_menu:
                print(f"{option['id']}. {option['name']}")

            choice = input("\nEnter your choice: ").strip()

            try:
                choice = int(choice)
                selected = next(
                    (opt for opt in self.attendance_menu if opt['id'] == choice), None)

                if selected:
                    if "back" in selected and selected["back"]:
                        return
                    elif "command" in selected:
                        self.commands[selected["command"]]()
                else:
                    print("Invalid choice. Please try again.")
            except ValueError:
                print("Please enter a number.")

    def show_reports_menu(self):
        """show reports menu"""
        while True:
            print("\n=== Reports ===")
            for option in self.reports_menu:
                print(f"{option['id']}. {option['name']}")

            choice = input("\nEnter your choice: ").strip()

            try:
                choice = int(choice)
                selected = next(
                    (opt for opt in self.reports_menu if opt['id'] == choice), None)

                if selected:
                    if "back" in selected and selected["back"]:
                        return
                    elif "command" in selected:
                        self.commands[selected["command"]]()
                else:
                    print("Invalid choice. Please try again.")
            except ValueError:
                print("Please enter a number.")

    def _print_header(self):
        """print program title"""
        print("\n" + "="*80)
        print("Student Management & Attendance System".center(80))
        print("="*80)
        print("Navigate using the menu numbers".center(80))
        print("-"*80)

    def _get_input(self, prompt: str, required: bool = True) -> str:
        """get input from user"""
        while True:
            value = input(prompt).strip()
            if not required or value:
                return value
            print("This field is required. Please try again.")

    def _get_date_input(self, prompt: str, default_today: bool = True) -> str:
        """get date from user"""
        if default_today:
            default_date = datetime.date.today().strftime(DATE_FORMAT)
            date_input = input(f"{prompt} [default: {default_date}]: ").strip()
            if not date_input:
                return default_date
        else:
            date_input = input(prompt).strip()

        try:
            datetime.datetime.strptime(date_input, DATE_FORMAT)
            return date_input
        except ValueError:
            print(
                f"Invalid date format. Please use {DATE_FORMAT} format (e.g., 2023-04-15).")
            return self._get_date_input(prompt, default_today)

    def _confirm_action(self, prompt: str) -> bool:
        """ask yes/no question"""
        response = input(f"{prompt} (y/n): ").strip().lower()
        return response in ('y', 'yes')

    # Student stuff

    def add_student(self):
        """add a new student"""
        print("\n--- Add New Student ---")

        # loop for name input
        while True:
            name = self._get_input("Enter student name: ")

            # get course
            while True:
                course = self._get_input(
                    "Enter course (2 letters followed by up to 3 numbers): ")
                if Student.validate_course(course):
                    break
                print(
                    "Invalid course format. Please enter 2 letters followed by up to 3 numbers (e.g., CS101, EN42).")

            student = Student(name=name, course=course)

            try:
                result = self.student_service.add_student(student)

                print(f"\nStudent added successfully!")
                print(f"Student ID: {result.student_id}")
                print(f"Name: {result.name}")
                print(f"Course: {result.course}")

                break  # exit the loop if successful

            except ValueError as e:
                if "already exists" in str(e):
                    print(f"\nError: {str(e)}")
                    if not self._confirm_action("Would you like to try a different name?"):
                        print("Student addition cancelled.")
                        break
                else:
                    print(f"\nError: {str(e)}")
                    break

        input("\nPress Enter to continue...")

    def view_student(self):
        """show student details"""
        print("\n--- View Student Details ---")

        student_id = self._get_input("Enter student ID: ")
        try:
            student_id = int(student_id)
        except ValueError:
            print("Student ID must be a number.")
            input("\nPress Enter to continue...")
            return

        student = self.student_service.get_student_by_id(student_id)

        if student:
            print(f"\nStudent ID: {student.student_id}")
            print(f"Name: {student.name}")
            print(f"Course: {student.course}")
            print(f"Enrollment Date: {student.enrollment_date}")
        else:
            print(f"No student found with ID {student_id}")

        input("\nPress Enter to continue...")

    def list_students(self):
        """show all students"""
        print("\n--- Student List ---")

        students = self.student_service.get_all_students()

        if not students:
            print("No students found in the system.")
            input("\nPress Enter to continue...")
            return

        print(f"Total students: {len(students)}\n")
        print(f"{'ID':<5} {'Name':<30} {'Course':<30}")
        print("-" * 65)

        for student in students:
            print(f"{student.student_id:<5} {student.name:<30} {student.course:<30}")

        input("\nPress Enter to continue...")

    def update_student(self):
        """update student info"""
        print("\n--- Update Student ---")

        student_id = self._get_input("Enter student ID: ")
        try:
            student_id = int(student_id)
        except ValueError:
            print("Student ID must be a number.")
            input("\nPress Enter to continue...")
            return

        existing_student = self.student_service.get_student_by_id(student_id)

        if not existing_student:
            print(f"No student found with ID {student_id}")
            input("\nPress Enter to continue...")
            return

        print(f"\nCurrent details:")
        print(f"Name: {existing_student.name}")
        print(f"Course: {existing_student.course}")

        # loop for validation
        while True:
            print("\nEnter new details (leave blank to keep current value):")
            name = input(f"Name [{existing_student.name}]: ").strip()
            if not name:
                name = existing_student.name

            # get and validate course
            while True:
                course = input(f"Course [{existing_student.course}]: ").strip()

                # keep current if empty
                if not course:
                    course = existing_student.course
                    break

                # validate if new input
                if Student.validate_course(course):
                    break

                print(
                    "Invalid course format. Please enter 2 letters followed by up to 3 numbers (e.g., CS101, EN42).")

            # update student
            updated_student = Student(
                student_id=student_id,
                name=name,
                course=course
            )

            try:
                result = self.student_service.update_student(updated_student)

                if result:
                    print("\nStudent updated successfully!")
                else:
                    print("\nFailed to update student.")

                break  # exit if successful

            except ValueError as e:
                if "already exists" in str(e):
                    print(f"\nError: {str(e)}")
                    if not self._confirm_action("Would you like to try a different name?"):
                        print("Update cancelled.")
                        break
                else:
                    print(f"\nError: {str(e)}")
                    break

        input("\nPress Enter to continue...")

    def delete_student(self):
        """delete a student"""
        print("\n--- Delete Student ---")

        student_id = self._get_input("Enter student ID: ")
        try:
            student_id = int(student_id)
        except ValueError:
            print("Student ID must be a number.")
            input("\nPress Enter to continue...")
            return

        # check if exists
        student = self.student_service.get_student_by_id(student_id)
        if not student:
            print(f"No student found with ID {student_id}")
            input("\nPress Enter to continue...")
            return

        print(f"\nStudent to delete:")
        print(f"ID: {student.student_id}")
        print(f"Name: {student.name}")
        print(f"Course: {student.course}")

        if not self._confirm_action("\nAre you sure you want to delete this student?"):
            print("Deletion cancelled.")
            input("\nPress Enter to continue...")
            return

        result = self.student_service.delete_student(student_id)

        if result:
            print("\nStudent deleted successfully!")
        else:
            print("\nFailed to delete student.")

        input("\nPress Enter to continue...")

    def search_students(self):
        """search students by name or course"""
        print("\n--- Search Students ---")

        search_term = self._get_input("Enter search term (name or course): ")

        students = self.student_service.search_students(search_term)

        if not students:
            print(f"No students found matching '{search_term}'")
            input("\nPress Enter to continue...")
            return

        print(f"\nFound {len(students)} matching students:\n")
        print(f"{'ID':<5} {'Name':<30} {'Course':<30}")
        print("-" * 65)

        for student in students:
            print(f"{student.student_id:<5} {student.name:<30} {student.course:<30}")

        input("\nPress Enter to continue...")

    def advanced_search(self):
        """search with multiple criteria"""
        print("\n--- Advanced Student Search ---")

        criteria = {}

        print("\nEnter search criteria (leave blank to skip):")

        student_id_input = input("Student ID: ").strip()
        if student_id_input:
            try:
                criteria['student_id'] = int(student_id_input)
            except ValueError:
                print("Student ID must be a number. This criterion will be ignored.")

        name = input("Name (partial match): ").strip()
        if name:
            criteria['name'] = name

        course = input("Course (partial match): ").strip()
        if course:
            criteria['course'] = course

        date_from = input("Enrollment date from (YYYY-MM-DD): ").strip()
        if date_from:
            try:
                datetime.datetime.strptime(date_from, '%Y-%m-%d')
                criteria['enrollment_date_from'] = date_from
            except ValueError:
                print("Invalid date format. This criterion will be ignored.")

        date_to = input("Enrollment date to (YYYY-MM-DD): ").strip()
        if date_to:
            try:
                datetime.datetime.strptime(date_to, '%Y-%m-%d')
                criteria['enrollment_date_to'] = date_to
            except ValueError:
                print("Invalid date format. This criterion will be ignored.")

        # search
        students = self.student_service.advanced_search(criteria)

        if not students:
            print("\nNo students found matching the specified criteria.")
            input("\nPress Enter to continue...")
            return

        print(f"\nFound {len(students)} matching students:\n")
        print(f"{'ID':<5} {'Name':<30} {'Course':<30} {'Enrollment Date':<15}")
        print("-" * 80)

        for student in students:
            enrollment_date = student.enrollment_date or 'N/A'
            print(
                f"{student.student_id:<5} {student.name:<30} {student.course:<30} {enrollment_date:<15}")

        # ask for bulk operations
        if self._confirm_action("\nDo you want to perform bulk operations on these students?"):
            self._bulk_operations_on_selection(
                [s.student_id for s in students])

        input("\nPress Enter to continue...")

    def bulk_operations(self):
        """do stuff to multiple students at once"""
        print("\n--- Bulk Operations ---")

        print("1. Select students from a list")
        print("2. Select students by course")
        print("3. Select students by advanced search")

        selection_choice = self._get_input("Enter choice (1-3): ")

        student_ids = []

        if selection_choice == '1':
            student_ids = self._select_students_from_list()
        elif selection_choice == '2':
            course = self._get_input("Enter course name: ")
            students = self.student_service.advanced_search({'course': course})
            student_ids = [s.student_id for s in students]

            if not student_ids:
                print(f"No students found in course '{course}'")
                input("\nPress Enter to continue...")
                return

            print(
                f"\nSelected {len(student_ids)} students from course '{course}'")
        elif selection_choice == '3':
            self.advanced_search()
            return  # advanced_search handles bulk operations
        else:
            print("Invalid choice.")
            input("\nPress Enter to continue...")
            return

        if student_ids:
            self._bulk_operations_on_selection(student_ids)
        else:
            print("No students selected for bulk operations.")
            input("\nPress Enter to continue...")

    def _select_students_from_list(self) -> List[int]:
        """let user pick students from list"""
        students = self.student_service.get_all_students()

        if not students:
            print("No students found in the system.")
            return []

        print(f"\nSelect students by entering their ID (separated by commas):")
        print(f"{'ID':<5} {'Name':<30} {'Course':<30}")
        print("-" * 65)

        for student in students:
            print(f"{student.student_id:<5} {student.name:<30} {student.course:<30}")

        selection = input("\nEnter student IDs (e.g., 1,3,5): ").strip()

        if not selection:
            return []

        # parse ids
        try:
            student_ids = [int(id_str.strip())
                           for id_str in selection.split(',')]
            valid_ids = [s.student_id for s in students]
            valid_selections = [id for id in student_ids if id in valid_ids]

            if len(valid_selections) < len(student_ids):
                print(
                    f"Warning: {len(student_ids) - len(valid_selections)} invalid student IDs were ignored.")

            return valid_selections
        except ValueError:
            print("Invalid input. Please enter numbers separated by commas.")
            return []

    def _bulk_operations_on_selection(self, student_ids: List[int]):
        """perform operations on multiple students"""
        if not student_ids:
            print("No students selected.")
            return

        # show selected students
        print(f"\nSelected {len(student_ids)} students:")
        students = []

        for student_id in student_ids:
            student = self.student_service.get_student_by_id(student_id)
            if student:
                students.append(student)
                print(
                    f"ID: {student.student_id}, Name: {student.name}, Course: {student.course}")

        # show options
        print("\nAvailable operations:")
        print("1. Update course for all selected students")
        print("2. Delete all selected students")
        print("3. Record attendance for all selected students")
        print("4. Cancel")

        op_choice = self._get_input("Enter operation (1-4): ")

        if op_choice == '1':
            # update course
            while True:
                new_course = self._get_input(
                    "Enter new course name (2 letters followed by up to 3 numbers): ")

                if Student.validate_course(new_course):
                    break

                print(
                    "Invalid course format. Please enter 2 letters followed by up to 3 numbers (e.g., CS101, EN42).")

            if self._confirm_action(f"Update course to '{new_course}' for {len(student_ids)} students?"):
                try:
                    updated = self.student_service.update_students_course(
                        student_ids, new_course)
                    print(
                        f"\nSuccessfully updated course for {updated} students.")
                except ValueError as e:
                    print(f"\nError: {str(e)}")

        elif op_choice == '2':
            # delete students
            if self._confirm_action(f"Are you sure you want to delete {len(student_ids)} students? This cannot be undone!"):
                deleted = self.student_service.delete_students(student_ids)
                print(f"\nSuccessfully deleted {deleted} students.")

        elif op_choice == '3':
            # record attendance
            date_str = self._get_date_input("Enter date (YYYY-MM-DD): ")

            print("\nSelect attendance status:")
            print("1. Present")
            print("2. Absent")

            status_choice = self._get_input("Enter choice (1/2): ")

            if status_choice not in ['1', '2']:
                print("Invalid choice.")
                return

            status = AttendanceStatus.PRESENT if status_choice == '1' else AttendanceStatus.ABSENT

            if self._confirm_action(f"Record '{status.value}' for {len(student_ids)} students on {date_str}?"):
                # record attendance
                for student_id in student_ids:
                    attendance = Attendance(
                        student_id=student_id,
                        date=date_str,
                        status=status
                    )
                    self.attendance_service.record_attendance(attendance)

                print(
                    f"\nSuccessfully recorded attendance for {len(student_ids)} students.")

        elif op_choice == '4':
            # cancel
            print("Operation cancelled.")

        else:
            print("Invalid choice.")

        input("\nPress Enter to continue...")

    # Attendance stuff

    def record_attendance(self):
        """record attendance for a student"""
        print("\n--- Record Attendance ---")

        student_id = self._get_input("Enter student ID: ")
        try:
            student_id = int(student_id)
        except ValueError:
            print("Student ID must be a number.")
            input("\nPress Enter to continue...")
            return

        # check if student exists
        student = self.student_service.get_student_by_id(student_id)
        if not student:
            print(f"No student found with ID {student_id}")
            input("\nPress Enter to continue...")
            return

        date_str = self._get_date_input("Enter date (YYYY-MM-DD): ")

        print("\nSelect attendance status:")
        print("1. Present")
        print("2. Absent")

        status_choice = self._get_input("Enter choice (1/2): ")

        if status_choice == '1':
            status = AttendanceStatus.PRESENT
        elif status_choice == '2':
            status = AttendanceStatus.ABSENT
        else:
            print("Invalid choice. Please enter 1 for Present or 2 for Absent.")
            input("\nPress Enter to continue...")
            return

        attendance = Attendance(
            student_id=student_id,
            date=date_str,
            status=status
        )

        result = self.attendance_service.record_attendance(attendance)

        print(f"\nAttendance recorded successfully!")
        print(f"Student: {student.name}")
        print(f"Date: {date_str}")
        print(f"Status: {status.value}")

        input("\nPress Enter to continue...")

    def record_attendance_batch(self):
        """Record attendance for multiple students at once."""
        print("\n--- Batch Attendance Recording ---")

        date_str = self._get_date_input(
            "Enter date for all records (YYYY-MM-DD): ")

        print("\nListing all students:")
        students = self.student_service.get_all_students()

        if not students:
            print("No students found in the system.")
            input("\nPress Enter to continue...")
            return

        print(f"\n{'ID':<5} {'Name':<30} {'Course':<20}")
        print("-" * 55)

        for student in students:
            print(f"{student.student_id:<5} {student.name:<30} {student.course:<20}")

        print("\nFor each student, enter:")
        print("P - Present")
        print("A - Absent")
        print("- - Skip (no record)")
        print("Enter 'cancel' at any time to cancel the batch recording.")

        attendance_records = []

        for student in students:
            while True:
                status_input = input(
                    f"{student.name} ({student.student_id}): ").strip().upper()

                if status_input == 'CANCEL':
                    print("Batch recording cancelled.")
                    input("\nPress Enter to continue...")
                    return

                if status_input in ['P', 'A', '-']:
                    break

                print("Invalid input. Please enter P, A, or -")

            if status_input == '-':
                continue

            status = AttendanceStatus.PRESENT if status_input == 'P' else AttendanceStatus.ABSENT

            attendance_records.append(Attendance(
                student_id=student.student_id,
                date=date_str,
                status=status
            ))

        if not attendance_records:
            print("No attendance records to save.")
            input("\nPress Enter to continue...")
            return

        # Save all records
        for attendance in attendance_records:
            self.attendance_service.record_attendance(attendance)

        print(
            f"\nSuccessfully recorded {len(attendance_records)} attendance records for {date_str}.")

        input("\nPress Enter to continue...")

    def view_attendance(self):
        """View attendance records."""
        print("\n--- View Attendance ---")
        print("1. View by student")
        print("2. View by date")

        choice = self._get_input("Enter choice (1/2): ")

        if choice == '1':
            self._view_student_attendance()
        elif choice == '2':
            self._view_date_attendance()
        else:
            print("Invalid choice.")
            input("\nPress Enter to continue...")

    def _view_student_attendance(self):
        """View attendance records for a specific student."""
        student_id = self._get_input("Enter student ID: ")
        try:
            student_id = int(student_id)
        except ValueError:
            print("Student ID must be a number.")
            input("\nPress Enter to continue...")
            return

        # Check if student exists
        student = self.student_service.get_student_by_id(student_id)
        if not student:
            print(f"No student found with ID {student_id}")
            input("\nPress Enter to continue...")
            return

        records = self.attendance_service.get_student_attendance(student_id)

        if not records:
            print(f"No attendance records found for {student.name}")
            input("\nPress Enter to continue...")
            return

        print(f"\nAttendance records for {student.name}:\n")
        print(f"{'Date':<12} {'Status':<10}")
        print("-" * 22)

        for record in records:
            status_value = record.status.value if hasattr(
                record.status, 'value') else record.status
            print(f"{record.date:<12} {status_value:<10}")

        # Show summary
        summary = self.attendance_service.get_student_attendance_summary(
            student_id)

        print(f"\nSummary:")
        print(f"Total days: {summary['total_days']}")
        print(f"Present: {summary['present_days']}")
        print(f"Absent: {summary['absent_days']}")
        print(f"Attendance percentage: {summary['attendance_percentage']}%")

        input("\nPress Enter to continue...")

    def _view_date_attendance(self):
        """View attendance records for a specific date."""
        date_str = self._get_date_input(
            "Enter date (YYYY-MM-DD): ", default_today=False)

        records = self.attendance_service.get_attendance_by_date(date_str)

        if not records:
            print(f"No attendance records found for {date_str}")
            input("\nPress Enter to continue...")
            return

        # Get student names
        student_ids = [record.student_id for record in records]
        student_map = {}

        for student_id in student_ids:
            student = self.student_service.get_student_by_id(student_id)
            if student:
                student_map[student_id] = student.name

        print(f"\nAttendance records for {date_str}:\n")
        print(f"{'ID':<5} {'Name':<30} {'Status':<10}")
        print("-" * 45)

        for record in records:
            status_value = record.status.value if hasattr(
                record.status, 'value') else record.status
            name = student_map.get(
                record.student_id, f"Unknown ({record.student_id})")
            print(f"{record.student_id:<5} {name:<30} {status_value:<10}")

        present_count = sum(1 for r in records if r.status.value == 'Present')
        absent_count = sum(1 for r in records if r.status.value == 'Absent')

        print(f"\nSummary for {date_str}:")
        print(f"Total records: {len(records)}")
        print(f"Present: {present_count}")
        print(f"Absent: {absent_count}")
        present_percentage = (present_count / len(records)
                              * 100) if records else 0
        print(f"Present percentage: {round(present_percentage, 2)}%")

        input("\nPress Enter to continue...")

    # ======= Report Commands =======

    def student_attendance_report(self):
        """Generate a student attendance report."""
        print("\n--- Student Attendance Report ---")

        student_id = self._get_input("Enter student ID: ")
        try:
            student_id = int(student_id)
        except ValueError:
            print("Student ID must be a number.")
            input("\nPress Enter to continue...")
            return

        try:
            report = self.report_service.generate_student_attendance_report(
                student_id)

            student = report['student']
            summary = report['attendance_summary']
            records = report['attendance_records']

            print(
                f"\nAttendance Report for {student['name']} (ID: {student['student_id']})")
            print(f"Course: {student['course']}")
            print(f"Generated: {report['generated_at']}")
            print("-" * 50)

            print(f"\nSummary:")
            print(f"Total days: {summary['total_days']}")
            print(f"Present: {summary['present_days']}")
            print(f"Absent: {summary['absent_days']}")
            print(
                f"Attendance percentage: {summary['attendance_percentage']}%")

            if records:
                print(f"\nAttendance Records:")
                print(f"{'Date':<12} {'Status':<10}")
                print("-" * 22)

                for record in records:
                    print(f"{record['date']:<12} {record['status']:<10}")

            # Ask if user wants to export the report
            if self._confirm_action("\nDo you want to export this report?"):
                self._export_report(report)

        except ValueError as e:
            print(f"Error: {str(e)}")

        input("\nPress Enter to continue...")

    def daily_attendance_report(self):
        """Generate a daily attendance report."""
        print("\n--- Daily Attendance Report ---")

        date_str = self._get_date_input(
            "Enter date (YYYY-MM-DD): ", default_today=True)

        try:
            report = self.report_service.generate_daily_attendance_report(
                date_str)

            print(f"\nDaily Attendance Report for {report['date']}")
            print(f"Generated: {report['generated_at']}")
            print("-" * 50)

            print(f"\nSummary:")
            print(f"Total students: {report['total_students']}")
            print(f"Present: {report['present_count']}")
            print(f"Absent: {report['absent_count']}")
            print(f"Not recorded: {report['not_recorded']}")
            print(f"Attendance percentage: {report['attendance_percentage']}%")

            if report['entries']:
                print(f"\nAttendance Details:")
                print(f"{'ID':<5} {'Name':<30} {'Course':<20} {'Status':<10}")
                print("-" * 65)

                for entry in report['entries']:
                    print(
                        f"{entry['student_id']:<5} {entry['name']:<30} {entry['course']:<20} {entry['status']:<10}")

            # Ask if user wants to export the report
            if self._confirm_action("\nDo you want to export this report?"):
                self._export_report(report)

        except ValueError as e:
            print(f"Error: {str(e)}")

        input("\nPress Enter to continue...")

    def course_attendance_report(self):
        """Generate a course attendance report."""
        print("\n--- Course Attendance Report ---")

        course = self._get_input("Enter course name: ")

        try:
            report = self.report_service.generate_course_attendance_report(
                course)

            print(f"\nCourse Attendance Report for {report['course']}")
            print(f"Generated: {report['generated_at']}")
            print("-" * 50)

            print(f"\nSummary:")
            print(f"Number of students: {report['student_count']}")
            print(
                f"Overall attendance: {report['overall_attendance_percentage']}%")

            if report['student_reports']:
                print(f"\nStudent Details:")
                print(
                    f"{'ID':<5} {'Name':<30} {'Present':<10} {'Absent':<10} {'Percentage':<10}")
                print("-" * 65)

                for student_report in report['student_reports']:
                    student = student_report['student']
                    summary = student_report['attendance_summary']

                    print(f"{student['student_id']:<5} {student['name']:<30} "
                          f"{summary['present_days']:<10} {summary['absent_days']:<10} "
                          f"{summary['attendance_percentage']}%")

            # Ask if user wants to export the report
            if self._confirm_action("\nDo you want to export this report?"):
                self._export_report(report)

        except ValueError as e:
            print(f"Error: {str(e)}")

        input("\nPress Enter to continue...")

    def monthly_attendance_report(self):
        """Generate a monthly attendance report."""
        print("\n--- Monthly Attendance Report ---")

        # Default to current month
        today = datetime.date.today()
        default_year = today.year
        default_month = today.month

        year_input = input(f"Enter year [default: {default_year}]: ").strip()
        year = int(year_input) if year_input else default_year

        month_input = input(
            f"Enter month (1-12) [default: {default_month}]: ").strip()
        month = int(month_input) if month_input else default_month

        if not (1 <= month <= 12):
            print("Month must be between 1 and 12.")
            input("\nPress Enter to continue...")
            return

        try:
            report = self.report_service.generate_monthly_attendance_report(
                year, month)

            print(
                f"\nMonthly Attendance Report for {report['month_name']} {report['year']}")
            print("-" * 50)

            print(f"\nSummary:")
            print(f"Total students: {report['total_students']}")
            print(f"Total attendance records: {report['total_records']}")
            print(
                f"Overall attendance: {report['overall_attendance_percentage']}%")

            if report['days']:
                print(f"\nDaily Breakdown:")
                print(
                    f"{'Date':<12} {'Present':<10} {'Absent':<10} {'Total':<10} {'Percentage':<10}")
                print("-" * 52)

                # Sort days by date
                sorted_dates = sorted(report['days'].keys())

                for date_str in sorted_dates:
                    day_data = report['days'][date_str]
                    print(f"{date_str:<12} {day_data['present']:<10} {day_data['absent']:<10} "
                          f"{day_data['total']:<10} {round(day_data['present_percentage'], 2)}%")

            # Ask if user wants to export the report
            if self._confirm_action("\nDo you want to export this report?"):
                self._export_report(report)

        except Exception as e:
            print(f"Error: {str(e)}")

        input("\nPress Enter to continue...")

    def export_report(self):
        """Export a previously generated report."""
        print("\n--- Export Report ---")
        print("1. Student Attendance Report")
        print("2. Daily Attendance Report")
        print("3. Course Attendance Report")
        print("4. Monthly Attendance Report")

        choice = self._get_input("Enter choice (1-4): ")

        try:
            if choice == '1':
                student_id = int(self._get_input("Enter student ID: "))
                report = self.report_service.generate_student_attendance_report(
                    student_id)
                self._export_report(report)

            elif choice == '2':
                date_str = self._get_date_input("Enter date (YYYY-MM-DD): ")
                report = self.report_service.generate_daily_attendance_report(
                    date_str)
                self._export_report(report)

            elif choice == '3':
                course = self._get_input("Enter course name: ")
                report = self.report_service.generate_course_attendance_report(
                    course)
                self._export_report(report)

            elif choice == '4':
                year = int(self._get_input("Enter year: "))
                month = int(self._get_input("Enter month (1-12): "))
                report = self.report_service.generate_monthly_attendance_report(
                    year, month)
                self._export_report(report)

            else:
                print("Invalid choice.")

        except ValueError as e:
            print(f"Error: {str(e)}")

        input("\nPress Enter to continue...")

    def _export_report(self, report: Dict[str, Any]):
        """Helper method to export a report."""
        print("\nExport format:")
        print("1. CSV")
        print("2. JSON")
        print("3. PDF")

        format_choice = self._get_input("Choose format (1-3): ")

        if format_choice not in ['1', '2', '3']:
            print("Invalid choice.")
            return

        # Determine filename based on report type
        filename_base = ""

        if 'student' in report and 'student_id' in report['student']:
            # Student report
            filename_base = f"student_{report['student']['student_id']}_attendance"
        elif 'date' in report:
            # Daily report
            filename_base = f"daily_attendance_{report['date']}"
        elif 'course' in report:
            # Course report
            filename_base = f"course_{report['course'].replace(' ', '_').lower()}_attendance"
        elif 'month_name' in report:
            # Monthly report
            filename_base = f"monthly_attendance_{report['year']}_{report['month']:02d}"
        else:
            filename_base = f"attendance_report_{datetime.date.today().strftime('%Y%m%d')}"

        default_filename = filename_base

        if format_choice == '1':
            default_filename += ".csv"
        elif format_choice == '2':
            default_filename += ".json"
        else:
            default_filename += ".pdf"

        filename = input(
            f"Enter filename [default: {default_filename}]: ").strip()
        if not filename:
            filename = default_filename

        try:
            if format_choice == '1':
                file_path = self.report_service.export_report_to_csv(
                    report, filename)
            elif format_choice == '2':
                file_path = self.report_service.export_report_to_json(
                    report, filename)
            else:
                file_path = self.report_service.export_report_to_pdf(
                    report, filename)

            print(f"\nReport exported successfully to: {file_path}")

        except Exception as e:
            print(f"Error exporting report: {str(e)}")

    # ======= System Commands =======

    def show_help(self):
        """Show available commands."""
        print("\n--- Available Commands ---")

        # Group commands by category
        categories = {
            "Student Management": [
                "add_student    - Add a new student",
                "view_student   - View a student's details",
                "list_students  - List all students",
                "update_student - Update a student's information",
                "delete_student - Delete a student",
                "search_students - Search for students by name or course",
                "advanced_search - Search for students using multiple criteria",
                "bulk_operations - Perform operations on multiple students at once"
            ],
            "Attendance Management": [
                "record_attendance       - Record attendance for a student",
                "record_attendance_batch - Record attendance for multiple students at once",
                "view_attendance         - View attendance records"
            ],
            "Reports": [
                "student_report - Generate a student attendance report",
                "daily_report   - Generate a daily attendance report",
                "course_report  - Generate a course attendance report",
                "monthly_report - Generate a monthly attendance report",
                "export_report  - Export a previously generated report"
            ],
            "Export Formats": [
                "CSV  - Export reports as comma-separated values",
                "JSON - Export reports as JSON data",
                "PDF  - Export reports as professionally formatted PDF documents"
            ],
            "System": [
                "help  - Show this help message",
                "exit  - Exit the program"
            ]
        }

        for category, commands in categories.items():
            print(f"\n{category}:")
            for cmd in commands:
                print(f"  {cmd}")

        input("\nPress Enter to continue...")

    def exit_program(self):
        """Exit the program."""
        print("\nExiting Student Management & Attendance System...")
        sys.exit(0)
