package io.cresco.cpms.storage.transfer;

public class TransferPath {
    private final String container;
    private final String path;

    public TransferPath(String path) {
        this.container = null;
        this.path = path;
    }

    public TransferPath(String container, String path) {
        this.container = container;
        this.path = path;
    }

    public String getContainer() {
        return container;
    }
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return String.format("""
                        Transfer Path (container: %s, path %s)""",
                getContainer(), getPath()
        );
    }
}
