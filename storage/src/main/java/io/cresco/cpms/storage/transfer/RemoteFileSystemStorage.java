package io.cresco.cpms.storage.transfer;

import com.jcraft.jsch.*;
import io.cresco.cpms.logging.CPMSLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class RemoteFileSystemStorage implements TransferAdapter {
    private CPMSLogger logger;

    public RemoteFileSystemStorage(RemoteFileSystemStorageBuilder builder) {
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
        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            jsch.setKnownHosts("~/.ssh/known_hosts");
            jsch.addIdentity("~/.ssh/id_rsa");
            session = jsch.getSession(transferPath.getContainer());
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;

            c.stat(transferPath.getPath());
            return true;
        } catch (JSchException | SftpException e) {
            return false;
        } finally {
            if (channel != null)
                channel.disconnect();
            if (session != null)
                session.disconnect();
        }
    }

    /**
     * List the files in a path in this provider
     *
     * @param transferPath The path to list the contents of
     * @return The contents of the path or an empty list
     */
    @Override
    public List<String> listFilesInPath(TransferPath transferPath) {
        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            jsch.setKnownHosts("~/.ssh/known_hosts");
            jsch.addIdentity("~/.ssh/id_rsa");
            logger.debug("TransferPath: {}", transferPath);
            session = jsch.getSession(transferPath.getContainer());
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;

            List<String> files = new java.util.ArrayList<>();
            for (ChannelSftp.LsEntry lsEntry : c.ls(transferPath.getPath())) {
                files.add(lsEntry.getFilename());
            }
            return files;
        } catch (JSchException e) {
            logger.cpmsError("Failed to list files on remote filesystem [{}:{}]",
                    transferPath.getContainer(), transferPath.getPath());
            return List.of();
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                logger.cpmsError("Remote filesystem location [{}:{}] does not exist",
                        transferPath.getContainer(), transferPath.getPath());
            else
                logger.cpmsError("Failed to list files on remote filesystem [{}:{}]",
                        transferPath.getContainer(), transferPath.getPath());
            return List.of();
        } finally {
            if (channel != null)
                channel.disconnect();
            if (session != null)
                session.disconnect();
        }
    }

    /**
     * Uploads a local file to the indicated container
     *
     * @param uploadPath   Path of local file to upload
     * @param transferPath Remote path for upload
     * @return Whether the file was successfully uploaded
     * @throws IOException if uploadPath doesn't exist locally or container doesn't exist remotely
     */
    @Override
    public boolean uploadFile(Path uploadPath, TransferPath transferPath) throws IOException {
        Session session = null;
        Channel channel = null;
        try {
            JSch jsch = new JSch();
            jsch.setKnownHosts("~/.ssh/known_hosts");
            jsch.addIdentity("~/.ssh/id_rsa");
            logger.debug("TransferPath: {}", transferPath);
            session = jsch.getSession(transferPath.getContainer());
            session.connect();

            channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;

            logger.debug("Uploading {}", uploadPath);
            logger.debug("Uploading to {}", transferPath.getPath());

            c.put(uploadPath.toString(), transferPath.getPath());
            return true;
        } catch (JSchException e) {
            throw new IOException(String.format("Failed to list files on remote filesystem [%s:%s]",
                    transferPath.getContainer(), transferPath.getPath()));
        } catch (SftpException e) {
            if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                throw new IOException(String.format("Remote filesystem location [%s:%s] does not exist",
                        transferPath.getContainer(), transferPath.getPath()));
            else
                throw new IOException(String.format("Failed to list files on remote filesystem [%s:%s]",
                        transferPath.getContainer(), transferPath.getPath()));
        } finally {
            if (channel != null)
                channel.disconnect();
            if (session != null)
                session.disconnect();
        }
    }

    /**
     * Downloads a remote file from the supplied location
     *
     * @param transferPath      Remote path for upload
     * @param destinationFolder The folder in which to download the remote object
     * @return The final Path object of the downloaded file
     * @throws IOException if the object doesn't exist remotely or local download fails
     */
    @Override
    public Path downloadFile(TransferPath transferPath, Path destinationFolder) throws IOException {
        return null;
    }



    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(RemoteFileSystemStorage.class);
    }

    public void updateLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(RemoteFileSystemStorage.class);
    }
}
