package io.cresco.cpms.database.models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Entity
@Table( name = "runner_log" )
public class RunnerLog extends GenericLogModel {
    @NotNull
    @ManyToOne
    @JoinColumn( name = "runner_id", updatable = false, nullable = false )
    private Runner runner;

    public RunnerLog() {
        super();
    }

    public RunnerLog(Runner runner, LogState state, String message) {
        super(state, message);
        this.runner = runner;
    }

    public Runner getRunner() {
        return runner;
    }
}
