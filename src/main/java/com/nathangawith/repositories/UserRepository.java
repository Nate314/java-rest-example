package com.nathangawith.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.springframework.stereotype.Component;

import com.nathangawith.database.Database;

@Component("user_repository")
public class UserRepository implements IUserRepository {

    public boolean existsByUsername(String username) throws Exception {
        try (Connection con = new Database().getConnection();
             PreparedStatement stmt = con.prepareStatement("select username from users where username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertUser(String username, String passwordHash) throws Exception {
        try (Connection con = new Database().getConnection();
             PreparedStatement stmt = con.prepareStatement("insert into users (username, password_hash) values (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.executeUpdate();
        }
    }

    public String getPasswordHash(String username) throws Exception {
        try (Connection con = new Database().getConnection();
             PreparedStatement stmt = con.prepareStatement("select password_hash from users where username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return rs.getString("password_hash");
            }
        }
    }
}
