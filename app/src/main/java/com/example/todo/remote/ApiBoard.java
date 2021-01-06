package com.example.todo.remote;

public class ApiBoard {
    private Integer id, user_id, status;
    private String name;
    private long updated_at, created_at;

    // Getters
    public Integer getId() {
        return id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public Integer getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public long getCreated_at() {
        return created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }
}
