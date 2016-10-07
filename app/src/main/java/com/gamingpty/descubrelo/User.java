package com.gamingpty.descubrelo;

import java.io.Serializable;

public class User implements Serializable {
    String id, authToken, providerId, avatar, name, email;

    public User() {
    }

    public User(String id, String authToken, String providerId, String avatar, String name, String email) {
        this.id = id;
        this.authToken = authToken;
        this.providerId = providerId;
        this.avatar = avatar;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}