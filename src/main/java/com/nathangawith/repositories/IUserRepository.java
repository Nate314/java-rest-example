package com.nathangawith.repositories;

public interface IUserRepository {
    boolean existsByUsername(String username) throws Exception;
    void insertUser(String username, String passwordHash) throws Exception;
    String getPasswordHash(String username) throws Exception;
}
