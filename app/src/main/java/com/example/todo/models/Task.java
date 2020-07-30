package com.example.todo.models;

public class Task {
    private long id;
    private int status, created_at, updated_at;
    private String name, text;

    // Getters
    public long getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public int getCreated_at() {
        return created_at;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(int updated_at) {
        this.updated_at = updated_at;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }
}
