package com.nathangawith.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nathangawith.repositories.IMathRepository;

@Component("math_service")
public class MathService implements IMathService {

    private IMathRepository repository;

    @Autowired
    public MathService(
        @Qualifier("math_repository")
        IMathRepository repository
    ) {
        this.repository = repository;
    }

    public Integer getAddition(int a, int b) {
        return this.repository.getAddition(a, b);
    }
}
