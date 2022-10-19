package io.cresco.cpms.logging;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table( name = "log_message")
public class CPMSLogMessage {
    @Id
    private String id;
    @Column( name = "ts" )
    private Long ts;
    @Column( name = "pipeline_id" )
    private String pipelineID;
    @Column( name = "job_id" )
    private String jobID;
    @Column( name = "task_id" )
    private String taskID;
    @Column( name = "run_id" )
    private String runID;
    @Column( name = "type" )
    private String type;
    @Column( name = "message" )
    private String message;

    public CPMSLogMessage() {

    }

    public CPMSLogMessage(CPMSLogMessageBuilder builder) {
        this.id = builder.getId();
        this.ts = builder.getTs();
        this.pipelineID = builder.getPipelineID();
        this.jobID = builder.getJobID();
        this.taskID = builder.getTaskID();
        this.runID = builder.getRunID();
        this.type = builder.getType().name();
        this.message = builder.getMessage();
    }

    public String getId() {
        return id;
    }

    public Long getTs() {
        return ts;
    }

    public String getPipelineID() {
        return pipelineID;
    }

    public String getJobID() {
        return jobID;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getRunID() {
        return runID;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
