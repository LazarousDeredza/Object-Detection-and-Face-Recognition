package com.example.eyesyhopefyp;

public class UserModel {
    String id;
    String  name,phone,email,BlindPersonName;

    public UserModel() {
    }

    public UserModel(String name, String phone, String email, String blindPersonName) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.BlindPersonName = blindPersonName;
    }

    public UserModel(String id, String name, String phone, String email, String blindPersonName) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.BlindPersonName = blindPersonName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBlindPersonName() {
        return BlindPersonName;
    }

    public void setBlindPersonName(String blindPersonName) {
        BlindPersonName = blindPersonName;
    }
}
