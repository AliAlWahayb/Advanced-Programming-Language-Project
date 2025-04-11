"""reports for attendance"""

import csv
import os
from datetime import datetime
from typing import List, Dict, Any, Optional
import calendar
import json

from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch

from database.db_handler import DatabaseHandler
from services.student_service import StudentService
from services.attendance_service import AttendanceService
from models.student import Student


class ReportService:
    """makes reports"""

    def __init__(
        self,
        db_handler: DatabaseHandler,
        student_service: StudentService = None,
        attendance_service: AttendanceService = None
    ):
        self.db_handler = db_handler
        self.student_service = student_service or StudentService(db_handler)
        self.attendance_service = attendance_service or AttendanceService(
            db_handler)

    def generate_student_attendance_report(self, student_id: int) -> Dict[str, Any]:
        student = self.student_service.get_student_by_id(student_id)
        if not student:
            raise ValueError(f"Student with ID {student_id} not found")

        attendance_records = self.attendance_service.get_student_attendance(
            student_id)
        attendance_summary = self.attendance_service.get_student_attendance_summary(
            student_id)

        return {
            'student': student.to_dict(),
            'attendance_summary': attendance_summary,
            'attendance_records': [record.to_dict() for record in attendance_records],
            'generated_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        }

    def generate_daily_attendance_report(self, date_str: str) -> Dict[str, Any]:
        attendance_records = self.attendance_service.get_attendance_by_date(
            date_str)
        all_students = self.student_service.get_all_students()

        attendance_map = {
            record.student_id: record.status for record in attendance_records}

        report_entries = []
        for student in all_students:
            status = attendance_map.get(student.student_id, 'Not Recorded')
            status_value = status.value if hasattr(status, 'value') else status

            report_entries.append({
                'student_id': student.student_id,
                'name': student.name,
                'course': student.course,
                'status': status_value
            })

        present_count = sum(
            1 for record in attendance_records if record.status.value == 'Present')
        absent_count = sum(
            1 for record in attendance_records if record.status.value == 'Absent')

        return {
            'date': date_str,
            'present_count': present_count,
            'absent_count': absent_count,
            'not_recorded': len(all_students) - len(attendance_records),
            'total_students': len(all_students),
            'attendance_percentage': round(present_count / len(all_students) * 100, 2) if all_students else 0,
            'entries': report_entries,
            'generated_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        }

    def generate_course_attendance_report(self, course: str) -> Dict[str, Any]:
        query = "SELECT * FROM students WHERE course = ? ORDER BY name;"
        students = [Student.from_db_dict(row)
                    for row in self.db_handler.execute_query(query, (course,))]

        if not students:
            raise ValueError(f"No students found for course '{course}'")

        student_reports = []
        for student in students:
            summary = self.attendance_service.get_student_attendance_summary(
                student.student_id)
            student_reports.append({
                'student': student.to_dict(),
                'attendance_summary': summary
            })

        total_present = sum(report['attendance_summary']
                            ['present_days'] for report in student_reports)
        total_days = sum(report['attendance_summary']['total_days']
                         for report in student_reports)

        return {
            'course': course,
            'student_count': len(students),
            'overall_attendance_percentage': round(total_present / total_days * 100, 2) if total_days > 0 else 0,
            'student_reports': student_reports,
            'generated_at': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        }

    def generate_monthly_attendance_report(self, year: int, month: int) -> Dict[str, Any]:
        return self.attendance_service.get_monthly_attendance_report(year, month)

    def export_report_to_csv(self, report_data: Dict[str, Any], filename: str) -> str:
        if not filename.endswith('.csv'):
            filename += '.csv'

        if 'entries' in report_data:  # Daily attendance report
            with open(filename, 'w', newline='') as csvfile:
                fieldnames = ['student_id', 'name', 'course', 'status']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

                writer.writeheader()
                for entry in report_data['entries']:
                    writer.writerow({
                        'student_id': entry['student_id'],
                        'name': entry['name'],
                        'course': entry['course'],
                        'status': entry['status']
                    })

        elif 'student_reports' in report_data:  # Course attendance report
            with open(filename, 'w', newline='') as csvfile:
                fieldnames = ['student_id', 'name', 'course', 'total_days',
                              'present_days', 'absent_days', 'attendance_percentage']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

                writer.writeheader()
                for report in report_data['student_reports']:
                    student = report['student']
                    summary = report['attendance_summary']

                    writer.writerow({
                        'student_id': student['student_id'],
                        'name': student['name'],
                        'course': student['course'],
                        'total_days': summary['total_days'],
                        'present_days': summary['present_days'],
                        'absent_days': summary['absent_days'],
                        'attendance_percentage': summary['attendance_percentage']
                    })

        elif 'attendance_records' in report_data:  # Student attendance report
            with open(filename, 'w', newline='') as csvfile:
                fieldnames = ['date', 'status']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

                writer.writeheader()
                for record in report_data['attendance_records']:
                    writer.writerow({
                        'date': record['date'],
                        'status': record['status']
                    })

        else:
            raise ValueError("Unsupported report format for CSV export")

        return os.path.abspath(filename)

    def export_report_to_json(self, report_data: Dict[str, Any], filename: str) -> str:
        if not filename.endswith('.json'):
            filename += '.json'

        with open(filename, 'w') as jsonfile:
            json.dump(report_data, jsonfile, indent=4)

        return os.path.abspath(filename)

    def export_report_to_pdf(self, report_data: Dict[str, Any], filename: str) -> str:
        if not filename.endswith('.pdf'):
            filename += '.pdf'

        doc = SimpleDocTemplate(filename, pagesize=letter)
        styles = getSampleStyleSheet()
        elements = []

        title_style = ParagraphStyle(
            'Title',
            parent=styles['Heading1'],
            alignment=1,
            spaceAfter=0.3*inch
        )

        header_style = ParagraphStyle(
            'Header',
            parent=styles['Heading2'],
            spaceAfter=0.2*inch
        )

        if 'entries' in report_data:  # Daily attendance report
            elements.append(Paragraph(
                f"Daily Attendance Report - {report_data['date']}", title_style))
            elements.append(Paragraph(
                f"Generated: {report_data['generated_at']}", styles["Italic"]))
            elements.append(Spacer(1, 0.2*inch))

            elements.append(Paragraph("Summary", header_style))
            summary_data = [
                ["Total Students", str(report_data['total_students'])],
                ["Present", str(report_data['present_count'])],
                ["Absent", str(report_data['absent_count'])],
                ["Not Recorded", str(report_data['not_recorded'])],
                ["Attendance %", f"{report_data['attendance_percentage']}%"]
            ]
            summary_table = Table(summary_data, colWidths=[2*inch, 1*inch])
            summary_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(summary_table)
            elements.append(Spacer(1, 0.3*inch))

            elements.append(Paragraph("Attendance Details", header_style))
            entries_data = [["ID", "Name", "Course", "Status"]]
            for entry in report_data['entries']:
                entries_data.append([
                    str(entry['student_id']),
                    entry['name'],
                    entry['course'],
                    entry['status']
                ])
            entries_table = Table(entries_data, colWidths=[
                                  0.5*inch, 2.5*inch, 1.5*inch, 1*inch])
            entries_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, 0), 'CENTER'),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(entries_table)

        elif 'student_reports' in report_data:  # Course attendance report
            elements.append(Paragraph(
                f"Course Attendance Report - {report_data['course']}", title_style))
            elements.append(Paragraph(
                f"Generated: {report_data['generated_at']}", styles["Italic"]))
            elements.append(Spacer(1, 0.2*inch))

            elements.append(Paragraph("Summary", header_style))
            summary_data = [
                ["Number of Students", str(report_data['student_count'])],
                ["Overall Attendance %",
                    f"{report_data['overall_attendance_percentage']}%"]
            ]
            summary_table = Table(summary_data, colWidths=[2*inch, 1*inch])
            summary_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(summary_table)
            elements.append(Spacer(1, 0.3*inch))

            elements.append(Paragraph("Student Details", header_style))
            students_data = [
                ["ID", "Name", "Present", "Absent", "Percentage"]]
            for report in report_data['student_reports']:
                student = report['student']
                summary = report['attendance_summary']
                students_data.append([
                    str(student['student_id']),
                    student['name'],
                    str(summary['present_days']),
                    str(summary['absent_days']),
                    f"{summary['attendance_percentage']}%"
                ])
            students_table = Table(students_data, colWidths=[
                                   0.5*inch, 2.5*inch, 1*inch, 1*inch, 1*inch])
            students_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, 0), 'CENTER'),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(students_table)

        elif 'attendance_records' in report_data:  # Student attendance report
            student = report_data['student']
            summary = report_data['attendance_summary']
            student_name = student['name']

            elements.append(Paragraph(
                f"Student Attendance Report - {student_name}", title_style))
            elements.append(Paragraph(
                f"ID: {student['student_id']}  |  Course: {student['course']}", styles["Normal"]))
            elements.append(Paragraph(
                f"Generated: {report_data['generated_at']}", styles["Italic"]))
            elements.append(Spacer(1, 0.2*inch))

            elements.append(Paragraph("Summary", header_style))
            summary_data = [
                ["Total Days", str(summary['total_days'])],
                ["Present", str(summary['present_days'])],
                ["Absent", str(summary['absent_days'])],
                ["Attendance %", f"{summary['attendance_percentage']}%"]
            ]
            summary_table = Table(summary_data, colWidths=[2*inch, 1*inch])
            summary_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(summary_table)
            elements.append(Spacer(1, 0.3*inch))

            elements.append(Paragraph("Attendance Records", header_style))
            records_data = [["Date", "Status"]]
            for record in report_data['attendance_records']:
                records_data.append([
                    record['date'],
                    record['status']
                ])
            records_table = Table(records_data, colWidths=[1.5*inch, 1.5*inch])
            records_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, 0), 'CENTER'),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(records_table)

        elif 'days' in report_data:  # Monthly attendance report
            month_name = report_data['month_name']
            year = report_data['year']

            elements.append(Paragraph(
                f"Monthly Attendance Report - {month_name} {year}", title_style))
            elements.append(Paragraph(
                f"Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}", styles["Italic"]))
            elements.append(Spacer(1, 0.2*inch))

            elements.append(Paragraph("Summary", header_style))
            summary_data = [
                ["Total Students", str(report_data['total_students'])],
                ["Total Records", str(report_data['total_records'])],
                ["Overall Attendance %",
                    f"{report_data['overall_attendance_percentage']}%"]
            ]
            summary_table = Table(summary_data, colWidths=[2*inch, 1*inch])
            summary_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (0, -1), colors.lightgrey),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(summary_table)
            elements.append(Spacer(1, 0.3*inch))

            elements.append(Paragraph("Daily Breakdown", header_style))
            days_data = [["Date", "Present", "Absent", "Total", "Percentage"]]

            # Sort days by date
            sorted_dates = sorted(report_data['days'].keys())

            for date_str in sorted_dates:
                day_data = report_data['days'][date_str]
                days_data.append([
                    date_str,
                    str(day_data['present']),
                    str(day_data['absent']),
                    str(day_data['total']),
                    f"{round(day_data['present_percentage'], 2)}%"
                ])
            days_table = Table(days_data, colWidths=[
                               1.1*inch, 1*inch, 1*inch, 1*inch, 1.4*inch])
            days_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, 0), 'CENTER'),
                ('GRID', (0, 0), (-1, -1), 1, colors.black),
                ('PADDING', (0, 0), (-1, -1), 6)
            ]))
            elements.append(days_table)

        else:
            raise ValueError("Unsupported report format for PDF export")

        doc.build(elements)
        return os.path.abspath(filename)
