package com.example.demo.dto;

public class LoginResponse {
    private String accessToken;
    private String firstName;
    private String lastName;
    private String email;

    public LoginResponse(String accessToken, String firstName, String lastName, String email) {
        this.accessToken = accessToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
