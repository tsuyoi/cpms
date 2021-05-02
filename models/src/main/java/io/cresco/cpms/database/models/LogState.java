package io.cresco.cpms.database.models;

@SuppressWarnings("unused")
public enum LogState {
    IDLE(0), WORKING(1), ERROR(2), SHUTDOWN(3), MISSING(4);
    public final int state;
    LogState(int state) { this.state = state; }
    public int getValue() { return state; }
    @Override
    public String toString() {
        return this.name();
    }
}

