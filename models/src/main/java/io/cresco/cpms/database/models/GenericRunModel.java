package io.cresco.cpms.database.models;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@SuppressWarnings("unused")
@MappedSuperclass
public class GenericRunModel extends GenericModel {
    @NotNull
    @CreationTimestamp
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "created", updatable = false, nullable = false )
    private Date created;

    @NotNull
    @Enumerated( EnumType.ORDINAL )
    @Column( name = "status", nullable = false )
    private RunStatus status;

    public GenericRunModel() {
        super();
        this.status = RunStatus.PENDING;
    }

    public GenericRunModel(RunStatus runStatus) {
        this();
        this.status = runStatus;
    }

    public Date getCreated() {
        return created;
    }

    public RunStatus getStatus() {
        return status;
    }
    public void setStatus(RunStatus status) {
        this.status = status;
    }
}
