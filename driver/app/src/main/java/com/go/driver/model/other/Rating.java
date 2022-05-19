package com.go.driver.model.other;

import java.io.Serializable;
import java.sql.Timestamp;

public class Rating implements Serializable {

    private String      userId;
    private Float       rating;
    private String      notes;
    private String      complement;
    private Timestamp   ratedAt;

    public Rating() {}

    public Rating(String userId, Float rating, Long ratedAt) {
        this.userId = userId;
        this.rating = rating;
        this.ratedAt = ratedAt == null ? null : new Timestamp(ratedAt);
    }

    public Rating(String userId, Float rating, String notes, Long ratedAt) {
        this.userId = userId;
        this.rating = rating;
        this.notes = notes;
        this.ratedAt = ratedAt == null ? null : new Timestamp(ratedAt);
    }

    public Rating(String userId, Float rating, String notes, String complement, Long ratedAt) {
        this.userId = userId;
        this.rating = rating;
        this.notes = notes;
        this.complement = complement;
        this.ratedAt = ratedAt == null ? null : new Timestamp(ratedAt);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getComplement() {
        return complement;
    }

    public void setComplement(String complement) {
        this.complement = complement;
    }

    public long getRatedAt() {
        return ratedAt == null ? 0 : ratedAt.getTime();
    }

    public void setRatedAt(Long ratedAt) {
        this.ratedAt = ratedAt == null ? null : new Timestamp(ratedAt);
    }
}
