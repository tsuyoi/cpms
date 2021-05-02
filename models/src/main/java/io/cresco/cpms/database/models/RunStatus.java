package io.cresco.cpms.database.models;

@SuppressWarnings("unused")
public enum RunStatus {
    PENDING(0), SUBMITTED(1), FINISHED(2), CANCELLED(3), FAILED(4);
    private final int status;
    RunStatus(int status) {
        this.status = status;
    }
    public int getValue() {
        return status;
    }
    @Override
    public String toString() { return this.name(); }
}
