package com.example.todo.models;

public class Workspace {
    private long updated_at, created_at;
    private Integer id, server_id, sync_status, status;
    private String name;

    // Getters
    public Integer getId() {
        return id;
    }

    public Integer getServer_id() {
        return server_id;
    }

    public Integer getSync_status() {
        return sync_status;
    }

    public Integer getStatus() {
        return status;
    }

    public long getCreated_at() {
        return created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setServer_id(Integer server_id) {
        this.server_id = server_id;
    }

    public void setSync_status(Integer sync_status) {
        this.sync_status = sync_status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setCreated_at(long created_at) {
        this.created_at = created_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public void setName(String name) {
        this.name = name;
    }
}
