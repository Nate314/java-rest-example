package com.nathangawith.services;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nathangawith.repositories.IUserRepository;

@Component("user_service")
public class UserService implements IUserService {

    /**
     * Hash checked when the username is unknown, so that unknown-user and
     * wrong-password logins cost the same bcrypt work (no timing oracle).
     */
    private static final String DUMMY_HASH = BCrypt.hashpw("dummy-password-for-timing", BCrypt.gensalt());

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
            BCrypt.checkpw(password, DUMMY_HASH);
            return false;
        }
        return BCrypt.checkpw(password, hash);
    }
}
