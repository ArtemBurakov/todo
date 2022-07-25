package com.example.todo.remote;

public class ApiTask {
    private Integer id, note_id, user_id, status;
    private String name;
    private long created_at, updated_at;

    // Getters
    public Integer getUser_id() {
        return user_id;
    }

    public Integer getNote_id() {
        return note_id;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getId() {
        return id;
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
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public void setNote_id(Integer note_id) {
        this.note_id = note_id;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setId(Integer id) {
        this.id = id;
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
