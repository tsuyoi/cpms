package io.cresco.cpms.storage.transfer;

import io.cresco.cpms.logging.CPMSLogger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class FileSystemStorage implements TransferAdapter {
    private CPMSLogger logger;

    /**
     * Object Storage Constructer utilizing the Builder paradigm
     * @param builder - Builder object
     */
    public FileSystemStorage(FileSystemStorageBuilder builder) {
        setLogger(builder.getLogger());
    }

    /**
     * Determines if a path exists in this provider
     *
     * @param transferPath The path to check
     * @return Whether the path exists
     */
    @Override
    public boolean doesPathExist(TransferPath transferPath) {
        logger.debug("Does path exist: {}", transferPath.getPath());
        return Files.exists(Paths.get(transferPath.getPath()));
    }

    /**
     * List the files in a path in this provider
     *
     * @param transferPath The path to list the contents of
     * @return The contents of the path or an empty list
     */
    @Override
    public List<String> listFilesInPath(TransferPath transferPath) {
        logger.debug("List files in path: {}", transferPath.getPath());
        if (!doesPathExist(transferPath))
            return List.of();
        try (Stream<Path> paths = Files.list(Paths.get(transferPath.getPath()))) {
            return paths.map(Path::toAbsolutePath).map(Path::toString).toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Uploads a local file to the indicated container
     *
     * @param uploadPath    Path of local file to upload
     * @param transferPath  Remote path for upload
     * @return Whether the file was successfully uploaded
     * @throws IOException if uploadPath doesn't exist locally or container doesn't exist remotely
     */
    @Override
    public boolean uploadFile(Path uploadPath, TransferPath transferPath) throws IOException {
        logger.debug("Upload file in path: {} -> {}", uploadPath,  transferPath);
        String localChecksum;
        try (InputStream in = new FileInputStream(uploadPath.toFile())) {
            localChecksum = DigestUtils.md5Hex(in);
            logger.debug("Local checksum: {}", localChecksum);
        } catch (Exception e) {
            logger.error("Failed to compute the MD5 checksum of file to copy: {}", e);
            return false;
        }
        FileUtils.copyFile(uploadPath.toFile(), new File(transferPath.getPath()));
        if (!Files.exists(Paths.get(transferPath.getPath()))) {
            logger.error("The copied file {} does not exist", transferPath.getPath());
            return false;
        }
        String remoteChecksum;
        try (InputStream in = new FileInputStream(transferPath.getPath())) {
            remoteChecksum = DigestUtils.md5Hex(in);
            logger.debug("Remote checksum: {}", remoteChecksum);
        } catch (Exception e) {
            logger.error("Failed to compute the MD5 checksum of copied file: {}", e);
            return false;
        }
        return remoteChecksum.equals(localChecksum);
    }

    /**
     * Downloads a remote file from the supplied location
     *
     * @param transferPath  Remote path for upload
     * @param destinationFolder The folder in which to download the remote object
     * @return The final Path object of the downloaded file
     * @throws IOException if the object doesn't exist remotely or local download fails
     */
    @Override
    public Path downloadFile(TransferPath transferPath, Path destinationFolder) throws IOException {
        logger.debug("Download file in path: {} -> {}",
                Paths.get(transferPath.getPath()).toAbsolutePath().normalize(),
                destinationFolder.toAbsolutePath().normalize());
        String remoteChecksum;
        try (InputStream in = new FileInputStream(transferPath.getPath())) {
            remoteChecksum = DigestUtils.md5Hex(in);
            logger.debug("Remote checksum: {}", remoteChecksum);
        } catch (Exception e) {
            logger.error("Failed to compute the MD5 checksum of copied file: {}", e);
            return null;
        }
        FileUtils.copyFileToDirectory(new File(transferPath.getPath()), destinationFolder.toFile());
        Path downloadedPath = destinationFolder.resolve(Paths.get(transferPath.getPath()).getFileName().toString());
        logger.debug("Downloaded file: {}", downloadedPath.toAbsolutePath().normalize());
        if (!Files.exists(downloadedPath)) {
            logger.error("The copied file {} does not exist", downloadedPath.toAbsolutePath().normalize());
            return null;
        }
        String localChecksum;
        try (InputStream in = new FileInputStream(downloadedPath.toFile())) {
            localChecksum = DigestUtils.md5Hex(in);
            logger.debug("Local checksum: {}", localChecksum);
        } catch (Exception e) {
            logger.error("Failed to compute the MD5 checksum of file to copy: {}", e);
            return null;
        }
        if (localChecksum.equals(remoteChecksum))
            return downloadedPath;
        return null;
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(FileSystemStorage.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(FileSystemStorage.class);
    }
}
