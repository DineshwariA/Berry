package com.berry.api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BerryDatabase {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/berry";
        String username = "";
        String password = "";
        return DriverManager.getConnection(url, username, password);
    }
}

