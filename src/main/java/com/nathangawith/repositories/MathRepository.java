package com.nathangawith.repositories;

import org.springframework.stereotype.Component;

@Component("math_repository")
public class MathRepository implements IMathRepository {

    public Integer getAddition(int a, int b) {
        return a + b;
    }
}
