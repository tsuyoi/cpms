package io.cresco.cpms.database.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Entity
@Table( name = "task_run_log" )
public class TaskRunLog extends GenericLogModel {
    @NotNull
    @ManyToOne
    @JoinColumn( name = "task_run_id", updatable = false, nullable = false )
    private TaskRun taskRun;

    public TaskRunLog() {
        super();
    }

    public TaskRunLog(TaskRun taskRun, LogState state, String message) {
        super(state, message);
    }
}
