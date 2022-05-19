package com.go.driver.model.other;

import java.io.Serializable;

public class Preference implements Serializable {

    private boolean available;

    public Preference() {}

    public Preference(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
