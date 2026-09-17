package com.nathangawith.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.springframework.stereotype.Component;

import com.nathangawith.database.Database;

@Component("operation_repository")
public class OperationRepository implements IOperationRepository {

    public void logAddition(String username, int a, int b, int result) throws Exception {
        try (Connection con = new Database().getConnection();
             PreparedStatement stmt = con.prepareStatement(
                 "insert into math_operations (username, operand_a, operand_b, result) values (?, ?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setInt(2, a);
            stmt.setInt(3, b);
            stmt.setInt(4, result);
            stmt.executeUpdate();
        }
    }
}
