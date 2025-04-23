package com.studentmanagement;

import com.studentmanagement.database.DatabaseHandler;
import com.studentmanagement.cli.CLI;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize database schema
            new DatabaseHandler();

            // Start the command-line interface
            CLI cli = new CLI();
            cli.start();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 