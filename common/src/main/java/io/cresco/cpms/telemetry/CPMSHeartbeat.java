package io.cresco.cpms.telemetry;

import io.cresco.cpms.identity.CPMSWorkerIdentity;

public class CPMSHeartbeat {
    private final CPMSWorkerIdentity workerIdentity;


    public CPMSHeartbeat(CPMSHeartbeatBuilder builder) {
        this.workerIdentity = builder.getWorkerIdentity();
    }

    public CPMSWorkerIdentity getWorkerIdentity() {
        return workerIdentity;
    }

    @Override
    public String toString() {
        return String.format("Heartbeat: [%s]", getWorkerIdentity());
    }
}
