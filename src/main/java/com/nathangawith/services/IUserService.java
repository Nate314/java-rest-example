package com.nathangawith.services;

public interface IUserService {
    boolean existsByUsername(String username) throws Exception;
    void register(String username, String password) throws Exception;
    boolean validateCredentials(String username, String password) throws Exception;
}
