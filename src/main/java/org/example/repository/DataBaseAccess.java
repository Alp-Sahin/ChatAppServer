package org.example.repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class DataBaseAccess {
    private Connection connection = DriverManager.getConnection("jdbc:sqlite:Database.db");
    private static DataBaseAccess databaseAccess;

    private DataBaseAccess() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        Statement statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY,username TEXT UNIQUE, hash BLOB UNIQUE)");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS messages(id INTEGER PRIMARY KEY,username TEXT, message TEXT, date INTEGER)");
    }
    public static DataBaseAccess getInstance() {
        if (databaseAccess == null) {
            try {
                databaseAccess = new DataBaseAccess();
            } catch (SQLException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return databaseAccess;
    }
    public boolean searchForPassword(byte[] hash){
        try {
            String query = "SELECT * FROM users WHERE hash=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setBytes(1,hash);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (Arrays.equals(resultSet.getBytes("hash"), hash)) return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Password fetch from database failed: ", e);
        }
    }
    public boolean searchForUsername(String username){
        try {
            String query = "SELECT * FROM users WHERE username=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if(resultSet.getString("username").equals(username)) return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Username fetch from database failed: ", e);
        }
    }
    public void newUser(String username, byte[] password){
        try{
            String query = "INSERT INTO users (id,username, hash) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,checkForUserID());
            statement.setString(2, username);
            statement.setBytes(3, password);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("New user registration failed: ", e);
        }
    }
    public void newMessage(String username, String message, LocalDateTime date){
        ZonedDateTime zonedDate = date.atZone(ZoneId.systemDefault());
        try {
            String query = "INSERT INTO messages (id, username, message, date) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1,checkForMessageID());
            statement.setString(2, username);
            statement.setString(3, message);
            statement.setTimestamp(4, new Timestamp(zonedDate.toInstant().toEpochMilli()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("New message registration failed: ", e);
        }
    }
    public String messageHistory(){
        try {
            String query = "SELECT * FROM messages ORDER BY id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            String message="";
            StringBuilder messageHistory = new StringBuilder();
            while (resultSet.next()) {
                message = resultSet.getString("username")+": "+resultSet.getString("message")+" || "+resultSet.getTimestamp("date")+"\n";
                messageHistory.append(message);
            }
            return messageHistory.toString();
        } catch (SQLException e) {
            throw new RuntimeException("Message history fetch from database failed: ", e);
        }
    }
    public int checkForMessageID(){
        try {
            String query = "SELECT MAX(id) FROM messages";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        }catch (SQLException e) {
            throw new RuntimeException("checkForMessageID failed: ", e);
        }
        return 0;
    }
    public int checkForUserID(){
        try{
            String query = "SELECT MAX(id) FROM users";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        }catch (SQLException e){
            throw new RuntimeException("checkForUserID failed: ", e);
        }
        return 0;
    }

}
