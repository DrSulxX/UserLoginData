package ottosulaoja.drsulxx.service;

import ottosulaoja.drsulxx.model.usermanagement.User;

public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String familyName;
    private String email;
    private boolean emailSent;

    public UserResponse(User user, boolean emailSent) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.familyName = user.getFamilyName();
        this.email = user.getEmail();
        this.emailSent = emailSent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }
}