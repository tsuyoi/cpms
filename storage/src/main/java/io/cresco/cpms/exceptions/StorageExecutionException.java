package io.cresco.cpms.exceptions;

import java.io.Serializable;

public class StorageExecutionException extends Throwable {
    StorageExecutionException(String message) {
        super(message);
    }
}
