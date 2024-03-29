package io.cresco.cpms.database.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Entity
@Table( name = "task_run" )
public class TaskRun extends GenericRunModel {
    @NotNull
    @ManyToOne
    @JoinColumn( name = "pipeline_run_id", nullable = false )
    private PipelineRun pipelineRun;

    @NotNull
    @ManyToOne
    @JoinColumn( name = "task_id", nullable = false )
    private Task task;

    @ManyToOne
    @JoinColumn( name = "runner_id" )
    private Runner runner;

    public TaskRun() {
        super();
    }

    public TaskRun(PipelineRun pipelineRun, Task task) {
        this();
        this.pipelineRun = pipelineRun;
        this.task = task;
    }

    public TaskRun(PipelineRun pipelineRun, Task task, Runner runner) {
        this.runner = runner;
    }

    public PipelineRun getPipelineRun() {
        return pipelineRun;
    }

    public Task getTask() {
        return task;
    }

    public Runner getRunner() {
        return runner;
    }
}
