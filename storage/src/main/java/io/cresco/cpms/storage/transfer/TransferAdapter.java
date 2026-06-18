package io.cresco.cpms.storage.transfer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TransferAdapter {
    /**
     * Determines if a path exists in this provider
     *
     * @param transferPath The path to check
     * @return Whether the path exists
     */
    public boolean doesPathExist(TransferPath transferPath);

    /**
     * List the files in a path in this provider
     *
     * @param transferPath The path to list the contents of
     * @return The contents of the path or an empty list
     */
    public List<String> listFilesInPath(TransferPath transferPath);

    /**
     * Uploads a local file to the indicated container
     *
     * @param uploadPath    Path of local file to upload
     * @param transferPath  Remote path for upload
     * @return Whether the file was successfully uploaded
     * @throws IOException if uploadPath doesn't exist locally or container doesn't exist remotely
     */
    public boolean uploadFile(Path uploadPath, TransferPath transferPath) throws IOException;


    /**
     * Downloads a remote file from the supplied location
     *
     * @param transferPath  Remote path for upload
     * @param destinationFolder The folder in which to download the remote object
     * @return The final Path object of the downloaded file
     * @throws IOException if the object doesn't exist remotely or local download fails
     */
    public Path downloadFile(TransferPath transferPath, Path destinationFolder) throws IOException;
}
