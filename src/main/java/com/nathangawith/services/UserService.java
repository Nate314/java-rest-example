package com.nathangawith.services;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nathangawith.repositories.IUserRepository;

@Component("user_service")
public class UserService implements IUserService {

    private IUserRepository repository;

    @Autowired
    public UserService(
        @Qualifier("user_repository")
        IUserRepository repository
    ) {
        this.repository = repository;
    }

    public boolean existsByUsername(String username) throws Exception {
        return this.repository.existsByUsername(username);
    }

    public void register(String username, String password) throws Exception {
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        this.repository.insertUser(username, hash);
    }

    public boolean validateCredentials(String username, String password) throws Exception {
        String hash = this.repository.getPasswordHash(username);
        if (hash == null) {
            return false;
        }
        return BCrypt.checkpw(password, hash);
    }
}
