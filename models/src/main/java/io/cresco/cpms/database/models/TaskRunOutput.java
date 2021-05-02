package io.cresco.cpms.database.models;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@SuppressWarnings("unused")
@Entity
@Table( name = "task_run_output" )
public class TaskRunOutput extends GenericModel {
    @NotNull
    @CreationTimestamp
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "ts", updatable = false, nullable = false )
    private Date ts;

    @NotNull
    @ManyToOne
    @JoinColumn( name = "task_run_id", updatable = false, nullable = false )
    private TaskRun taskRun;

    @NotNull
    @Column( name = "output", columnDefinition = "longtext", updatable = false, nullable = false )
    private String output;

    public TaskRunOutput() {
        super();
    }

    public TaskRunOutput(TaskRun taskRun, String output) {
        this();
        this.taskRun = taskRun;
        this.output = output;
    }

    public Date getTs() {
        return ts;
    }

    public TaskRun getTaskRun() {
        return taskRun;
    }

    public String getOutput() {
        return output;
    }
}
