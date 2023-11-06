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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CPMSTaskOutput (");
        if (getTs() != null)
            sb.append(String.format(",TS:%s", getTs()));
        if (getTask() != null)
            sb.append(String.format(",T:%s", getTask()));
        return sb.toString();
    }
}
