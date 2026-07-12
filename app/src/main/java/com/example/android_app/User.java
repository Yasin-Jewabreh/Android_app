package com.example.android_app;

public class User {
    private String id;
    private String name;
    private String email;
    private String passwort;

    public User(String id, String name, String email, String passwort) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwort = passwort;
    }
    public User() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswort() {
        return passwort;
    }
}
