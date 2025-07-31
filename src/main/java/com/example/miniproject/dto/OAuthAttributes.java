package com.example.miniproject.dto;

import lombok.Getter;

@Getter
public class OAuthAttributes {

    private final String email;
    private final String name;

    public OAuthAttributes(String email, String name) {
        this.email = email;
        this.name = name;
    }
}
