package io.cresco.cpms.database.models;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@SuppressWarnings("unused")
@MappedSuperclass
public class GenericLogModel extends GenericModel {
    @NotNull
    @CreationTimestamp
    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "ts", updatable = false, nullable = false )
    private Date ts;

    @NotNull
    @Enumerated( EnumType.ORDINAL )
    @Column( name = "state", updatable = false, nullable = false )
    private LogState state;

    @NotNull
    @Column( name = "message", columnDefinition = "longtext", updatable = false, nullable = false )
    private String message;

    public GenericLogModel() {
        super();
    }

    public GenericLogModel(LogState state, String message) {
        this();
        this.state = state;
        this.message = message;
    }

    public Date getTs() {
        return ts;
    }

    public LogState getState() {
        return state;
    }

    public String getMessage() {
        return message;
    }
}
