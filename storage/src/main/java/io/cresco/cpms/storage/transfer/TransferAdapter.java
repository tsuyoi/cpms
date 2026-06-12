package io.cresco.cpms.storage.transfer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TransferAdapter {
    /**
     * Lists the top level containers
     *
     * @return A String list of the top level container
     */
    public List<String> listTopLevelContainers();

    /**
     *  Determines whether a top level container with this name exists
     *
     * @param containerName The name of the top level container object
     * @return A boolean indicating the existence of a container with a matching name
     */
    public boolean doesContainerExist(String containerName);

    /**
     * List the storage objects associated with a top level container name
     *
     * @param containerName The name of the top level container object
     * @return A String list of the objects
     */
    public List<String> listObjectsInContainer(String containerName);

    /**
     * List the storage objects associated with a top level container name with a matching name prefix
     *
     * @param containerName The name of the top level container object
     * @param prefix        A String prefix used to match container objects
     * @return A String list of matching objects
     */
    public List<String> listObjectsInContainer(String containerName, String prefix);

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
     * Uploads a local file to the indicated container
     *
     * @param container         Name of container in which to upload file
     * @param key               Key to use inside container
     * @param destinationFolder The folder in which to download the remote object
     * @return Whether the object was successfully downloaded
     * @throws IOException if the object doesn't exist remotely or local download fails
     */
    public boolean downloadObject(String container, String key, Path destinationFolder) throws IOException;
}
