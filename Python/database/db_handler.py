"""sqlite database handler"""

import sqlite3
import os
from contextlib import contextmanager
from typing import List, Dict, Any, Generator, Tuple, Optional

from config import DB_PATH
from database.schema import TABLES, INDEXES


class DatabaseHandler:
    """database connection manager"""

    def __init__(self, db_path: str = DB_PATH):
        self.db_path = db_path
        self._initialize_db()

    def _initialize_db(self) -> None:
        with self.get_connection() as conn:
            cursor = conn.cursor()

            for table_sql in TABLES:
                cursor.execute(table_sql)

            for index_sql in INDEXES:
                cursor.execute(index_sql)

            conn.commit()

    @contextmanager
    def get_connection(self) -> Generator[sqlite3.Connection, None, None]:
        conn = sqlite3.connect(self.db_path)
        conn.execute("PRAGMA foreign_keys = ON")
        conn.row_factory = sqlite3.Row

        try:
            yield conn
        finally:
            conn.close()

    def execute_query(self, query: str, params: Tuple = ()) -> List[Dict[str, Any]]:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            return [dict(row) for row in cursor.fetchall()]

    def execute_write_query(self, query: str, params: Tuple = ()) -> int:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(query, params)
            conn.commit()

            if query.strip().upper().startswith("INSERT"):
                return cursor.lastrowid
            return cursor.rowcount

    def insert_many(self, query: str, param_list: List[Tuple]) -> int:
        with self.get_connection() as conn:
            cursor = conn.cursor()
            cursor.executemany(query, param_list)
            conn.commit()
            return cursor.rowcount
