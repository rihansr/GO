package com.go.rider.model.notification;

import java.io.Serializable;
import java.sql.Timestamp;

public class Notification implements Serializable {

    private String      id;
    private Integer     userType;
    private String      photo;
    private String      title;
    private String      message;
    private Timestamp   sentAt;
    private Timestamp   expireAt;

    public Notification() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getSentAt() {
        return sentAt == null ? 0 : sentAt.getTime();
    }

    public void setSentAt(Long sentAt) {
        this.sentAt = sentAt == null ? null : new Timestamp(sentAt);
    }

    public long getExpireAt() {
        return expireAt == null ? 0 : expireAt.getTime();
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt == null ? null : new Timestamp(expireAt);
    }
}

