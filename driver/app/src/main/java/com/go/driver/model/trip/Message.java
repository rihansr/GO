package com.go.driver.model.trip;

import java.sql.Timestamp;

public class Message implements Comparable {

    private int         id;
    private String      senderId;
    private String      photo;
    private String      gender;
    private String      name;
    private String      message;
    private Timestamp   sentAt;

    public Message() {}

    public Message(int id, String senderId, String photo, String gender, String name, String message, Long sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.photo = photo;
        this.gender = gender;
        this.name = name;
        this.message = message;
        this.sentAt = sentAt == null ? null : new Timestamp(sentAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public int compareTo(Object obj) {
        long past =((Message) obj).getSentAt();
        return (int) (this.sentAt.getTime() - past);
    }
}
