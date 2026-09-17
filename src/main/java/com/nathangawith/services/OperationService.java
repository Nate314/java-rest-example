package com.nathangawith.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nathangawith.repositories.IOperationRepository;

@Component("operation_service")
public class OperationService implements IOperationService {

    private IOperationRepository repository;

    @Autowired
    public OperationService(
        @Qualifier("operation_repository")
        IOperationRepository repository
    ) {
        this.repository = repository;
    }

    public void logAddition(String username, int a, int b, int result) throws Exception {
        this.repository.logAddition(username, a, b, result);
    }
}
