package io.cresco.cpms.logging;

import java.util.Date;

public class CPMSTaskOutput {
    private final Date ts;
    private final String task;
    private final String output;

    public CPMSTaskOutput(String task, String output) {
        this.ts = new Date();
        this.task = task;
        this.output = output;
    }

    public Date getTs() {
        return ts;
    }

    public String getTask() {
        return task;
    }

    public String getOutput() {
        return output;
    }
}
