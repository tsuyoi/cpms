package io.cresco.cpms.storage.transfer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TransferAdapter {
    /**
     * Determines if a path exists in this provider
     *
     * @param path The path to check
     * @return Whether the path exists
     */
    public boolean doesPathExist(String path);

    /**
     * List the files in a path in this provider
     *
     * @param path The path to list the contents of
     * @return The contents of the path or an empty list
     */
    public List<String> listFilesInPath(String path);

    /**
     * Uploads a local file to the indicated container
     *
     * @param uploadPath    Path of local file to upload
     * @param container     Name of container in which to upload file
     * @param key           Key to use inside container
     * @return Whether the file was successfully uploaded
     * @throws IOException if uploadPath doesn't exist locally or container doesn't exist remotely
     */
    public boolean uploadFile(Path uploadPath, String container, String key) throws IOException;


    /**
     * Downloads a remote file from the supplied location
     *
     * @param container         Name of container in which to upload file
     * @param key               Key to use inside container
     * @param destinationFolder The folder in which to download the remote object
     * @return The final Path object of the downloaded file
     * @throws IOException if the object doesn't exist remotely or local download fails
     */
    public Path downloadFile(String container, String key, Path destinationFolder) throws IOException;
}
