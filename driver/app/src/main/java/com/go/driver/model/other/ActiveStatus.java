package com.go.driver.model.other;

import java.io.Serializable;
import java.sql.Timestamp;

public class ActiveStatus implements Serializable {

    private boolean     status;
    private Timestamp   activeAt;

    public ActiveStatus() {}

    public ActiveStatus(boolean status, Long activeAt) {
        this.status = status;
        this.activeAt = activeAt == null ? null : new Timestamp(activeAt);
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public long getActiveAt() {
        return activeAt == null ? 0 : activeAt.getTime();
    }

    public void setActiveAt(Long activeAt) {
        this.activeAt = activeAt == null ? null : new Timestamp(activeAt);
    }
}
