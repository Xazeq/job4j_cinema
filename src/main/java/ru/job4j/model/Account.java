package ru.job4j.model;

import java.util.Objects;

public class Account {
    private int id;
    private String username;
    private String email;
    private String phone;

    public Account(int id, String username, String email, String phone) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return id == account.id
                && Objects.equals(username, account.username)
                && Objects.equals(email, account.email)
                && Objects.equals(phone, account.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, phone);
    }
}
