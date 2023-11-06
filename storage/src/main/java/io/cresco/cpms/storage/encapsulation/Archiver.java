package io.cresco.cpms.storage.encapsulation;

import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.exceptions.*;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.reader.BagReader;
import io.cresco.cpms.logging.BasicCPMSLogger;
import io.cresco.cpms.logging.BasicCPMSLoggerBuilder;
import io.cresco.cpms.logging.CPMSLogger;
import io.cresco.cpms.statics.ArchiveCompression;
import io.cresco.cpms.statics.BagItHashingAlgorithm;
import io.cresco.cpms.statics.BagItType;
import io.cresco.cpms.statics.CPMSStatics;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tika.Tika;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

@SuppressWarnings({"unused", "WeakerAccess", "UnusedReturnValue", "SameParameterValue"})
public class Archiver {
    private CPMSLogger logger;
    private final BagItType bagitType;
    private final BagItHashingAlgorithm bagitHashing;
    private final boolean bagitHiddenFiles;
    private final ArchiveCompression archiveCompression;

    public Archiver(ArchiverBuilder builder) {
        this.bagitType = builder.getBagitType();
        this.bagitHashing = builder.getBagitHashingAlgorithm();
        this.bagitHiddenFiles = builder.isBagitHiddenFiles();
        this.archiveCompression = builder.getArchiveCompression();
        setLogger(builder.getLogger());
    }

    private String getCompressionFileExtension(ArchiveCompression compressionType) {
        switch (compressionType) {
            case TAR:
                return ".tar";
            case GZIP:
                return ".tar.gz";
            default:
                return null;
        }
    }

    public String getCompressionFileExtension() {
        return getCompressionFileExtension(archiveCompression);
    }

    public String getFlowCellObjectName(String flowCellID) {
        return String.format("%s%s", flowCellID, getCompressionFileExtension(archiveCompression));
    }

    public String getFlowCellObjectName(String flowCellID, String suffix) {
        return String.format("%s-%s%s", flowCellID, suffix, getCompressionFileExtension(archiveCompression));
    }

    public String getTypedSampleObjectName(String flowCellID, String type, String sampleID) {
        return String.format("%s/%s/%s%s", flowCellID, type, sampleID, getCompressionFileExtension(archiveCompression));
    }

    public String getTypedSampleObjectName(String flowCellID, String type, String sampleID, String suffix) {
        return String.format("%s-%s/%s/%s%s", flowCellID, suffix, type, sampleID, getCompressionFileExtension(archiveCompression));
    }

    public String getSampleObjectName(String flowCellID, String sampleID) {
        return String.format("%s/%s%s", flowCellID, sampleID, getCompressionFileExtension(archiveCompression));
    }

    public String getSampleObjectName(String flowCellID, String sampleID, String suffix) {
        return String.format("%s-%s/%s%s", flowCellID, suffix, sampleID, getCompressionFileExtension(archiveCompression));
    }

    public boolean isArchive(String archive) {
        return isArchive(Paths.get(archive));
    }

    public boolean isArchive(File archive) {
        return isArchive(archive.toPath());
    }

    public boolean isArchive(Path archive) {
        logger.trace("isArchive('{}')", archive);
        return (archive.getFileName().toString().endsWith(".tar") || archive.getFileName().toString().endsWith(".tar.gz"));
    }

    public boolean isBag(String bag) {
        return isBag(Paths.get(bag));
    }

    public boolean isBag(File bag) {
        return isBag(bag.toPath());
    }

    public boolean isBag(Path bag) {
        logger.trace("isBag('{}')", bag);
        if (!Files.exists(bag) || !Files.isDirectory(bag))
            return false;
        if (Files.exists(bag.resolve(".bagit")))
            return true;
        Path data = bag.resolve("data");
        if (!Files.exists(data) || !Files.isDirectory(data))
            return false;
        boolean hasBagitTxt = Files.exists(bag.resolve("bagit.txt"));
        boolean hasBagitInfo = Files.exists(bag.resolve("bag-info.txt"));
        boolean manifestSHA512 = Files.exists(bag.resolve("manifest-sha512.txt"));
        boolean tagmanifestSHA512 = Files.exists(bag.resolve("tag-manifest-sha512.txt"));
        boolean hasSHA512 = manifestSHA512 && tagmanifestSHA512;
        boolean manifestSHA256 = Files.exists(bag.resolve("manifest-sha256.txt"));
        boolean tagmanifestSHA256 = Files.exists(bag.resolve("tagmanifest-sha256.txt"));
        boolean hasSHA256 = manifestSHA256 && tagmanifestSHA256;
        boolean manifestSHA1 = Files.exists(bag.resolve("manifest-sha1.txt"));
        boolean tagmanifestSHA1 = Files.exists(bag.resolve("tagmanifest-sha1.txt"));
        boolean hasSHA1 = manifestSHA1 && tagmanifestSHA1;
        boolean manifestMD5 = Files.exists(bag.resolve("manifest-md5.txt"));
        boolean tagmanifestMD5 = Files.exists(bag.resolve("tagmanifest-md5.txt"));
        boolean hasMD5 = manifestMD5 && tagmanifestMD5;
        return (hasBagitTxt && hasBagitInfo && (hasSHA512 || hasSHA256 || hasSHA1 || hasMD5));
    }

    public boolean isPartialBag(Path bag) {
        logger.trace("isPartialBag('{}')", bag);
        if (!Files.exists(bag) || !Files.isDirectory(bag))
            return false;
        if (Files.exists(bag.resolve(".bagit")))
            return true;
        Path data = bag.resolve("data");
        if (!Files.exists(data) || !Files.isDirectory(data))
            return false;
        try {
            Set<Path> dirs = Files.list(bag).filter(Files::isDirectory).collect(Collectors.toSet());
            if (dirs.size() > 1 || !dirs.contains(data))
                return false;
        } catch (IOException e) {
            logger.error("Failed to get folders in [{}]", bag);
            return false;
        }
        return Files.exists(bag.resolve("bagit.txt"));
    }

    /**
     * Build BagIt bag from directory
     * @param folder Directory to bag up
     * @return The resulting bag path or null in case of exception
     */
    public Path bagItUp(Path folder) {
        logger.trace("bagItUp('{}')", folder.toAbsolutePath());
        if (isBag(folder) || isPartialBag(folder))
            debagify(folder);
        List<SupportedAlgorithm> algorithms = new ArrayList<>();
        if (bagitHashing == BagItHashingAlgorithm.MD5)
            algorithms.add(StandardSupportedAlgorithms.MD5);
        if (bagitHashing == BagItHashingAlgorithm.SHA1)
            algorithms.add(StandardSupportedAlgorithms.SHA1);
        if (bagitHashing == BagItHashingAlgorithm.SHA256)
            algorithms.add(StandardSupportedAlgorithms.SHA256);
        if (bagitHashing == BagItHashingAlgorithm.SHA512)
            algorithms.add(StandardSupportedAlgorithms.SHA512);
        switch (bagitType) {
            case DotFile:
                try {
                    logger.trace("Creating .bagit archive in [{}]", folder);
                    BagCreator.createDotBagit(folder, algorithms, bagitHiddenFiles);
                } catch (IOException e) {
                    logger.error("bagItUp : File error encountered while creating BagIt bag [{}]: {}", folder.toAbsolutePath(), e.getMessage());
                    return null;
                } catch (NoSuchAlgorithmException e) {
                    logger.error("bagItUp : Unsupported algorithm selected.");
                    return null;
                }
                break;
            case Standard:
                try {
                    logger.trace("Creating standard BagIt archive in [{}]", folder);
                    BagCreator.bagInPlace(folder, algorithms, bagitHiddenFiles);
                } catch (IOException e) {
                    logger.error("bagItUp : File error encountered while creating BagIt bag [{}]: {}", folder.toAbsolutePath(), e.getMessage());
                    return null;
                } catch (NoSuchAlgorithmException e) {
                    logger.error("bagItUp : Unsupported algorithm selected.");
                    return null;
                }
                break;
        }
        return folder;
    }

    /**
     * Build BagIt bag from directory
     * @param folder Directory to bag up
     * @return The resulting verified bag path, null otherwise
     */
    public Path bagItUpAndVerify(Path folder) {
        logger.trace("bagItUpAndVerify('{}')", folder.toAbsolutePath());
        if (bagItUp(folder) != null && verifyBag(folder))
            return folder;
        return null;
    }

    /**
     * Reads a directory to a Bag object
     * @param path Path to the bag directory
     * @return Resulting Bag object
     */
    public Bag readBag(Path path) {
        logger.trace("readBag({})", path.toAbsolutePath());
        BagReader reader = new BagReader();
        try {
            return reader.read(path.normalize());
        } catch (IOException e) {
            logger.error("readBag : Failed to load BagIt bag: {}", path.toAbsolutePath());
            return null;
        } catch (UnparsableVersionException e) {
            logger.error("readBag : Cannot parse this version of BagIt.");
            return null;
        } catch (MaliciousPathException e) {
            logger.error("readBag : Invalid BagIt bag path encountered.");
            return null;
        } catch (UnsupportedAlgorithmException e) {
            logger.error("readBag : BagIt bag requires an unsupported hashing algorithm.");
            return null;
        } catch (InvalidBagitFileFormatException e) {
            logger.error("readBag : The format of this BagIt bag is invalid.");
            return null;
        }
    }

    /**
     * Reads a directory to a Bag object
     * @param path Path to the bag directory
     * @return Resulting Bag object
     */
    public Bag readBag(File path) {
        return readBag(path.toPath());
    }

    /**
     * Reads a directory to a Bag object
     * @param path Path to the bag directory
     * @return Resulting Bag object
     */
    public Bag readBag(String path) {
        return readBag(new File(path));
    }

    /**
     * Verifies the bag at the given path
     * @param path Path of the bag to verify
     * @return Whether the bag is valid or not
     */
    public boolean verifyBag(Path path) {
        logger.trace("verifyBag({})", path);
        LargeBagVerifier verifier = new LargeBagVerifier();
        Bag bag = readBag(path);
        if (bag == null)
            return false;
        try {
            verifier.isValid(bag, bagitHiddenFiles);
            verifier.close();
            return true;
        } catch (IOException e) {
            logger.error("verifyBag : Failed to read a file in BagIt bag : {}", e.getMessage());
            return false;
        } catch (UnsupportedAlgorithmException e) {
            logger.error("verifyBag : BagIt bag requires an unsupported hashing algorithm.");
            return false;
        } catch (MissingPayloadManifestException e) {
            logger.error("verifyBag : BagIt bag is missing a payload manifest.");
            return false;
        } catch (MissingBagitFileException e) {
            logger.error("verifyBag : BagIt bag is missing a file: {}", e.getMessage());
            return false;
        } catch (MissingPayloadDirectoryException e) {
            logger.error("verifyBag : BagIt bag is missing a payload directory.");
            return false;
        } catch (FileNotInPayloadDirectoryException e) {
            logger.error("verifyBag : BagIt bag is missing a file from its payload directory: {}", e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.error("verifyBag : Verification process was interrupted.");
            return false;
        } catch (MaliciousPathException e) {
            logger.error("verifyBag : Invalid BagIt bag path encountered.");
            return false;
        } catch (CorruptChecksumException e) {
            logger.error("verifyBag : BagIt bag contains a corrupt checksum: {}", e.getMessage());
            return false;
        } catch (VerificationException e) {
            logger.error("verifyBag : BagIt bag encountered an unknown verification issue.");
            return false;
        } catch (InvalidBagitFileFormatException e) {
            logger.error("verifyBag : BagIt bag is in an invalid format.");
            return false;
        }
    }

    /**
     * Verifies the bag at the given path
     * @param path Path of the bag to verify
     * @return Whether the bag is valid or not
     */
    public boolean verifyBag(File path) {
        return verifyBag(path.toPath());
    }

    /**
     * Verifies the bag at the given path
     * @param path Path of the bag to verify
     * @return Whether the bag is valid or not
     */
    public boolean verifyBag(String path) {
        return verifyBag(new File(path));
    }

    /**
     * Cleans up from the BagIt bag creation
     * @param bag The path to the bag to clean up
     */
    public void debagify(Path bag) {
        logger.trace("Call to debagify({})", bag.toAbsolutePath());
        if (Files.isRegularFile(bag))
            return;
        Path bagIt = bag.resolve(".bagit");
        if (Files.exists(bagIt)) {
            try {
                deleteFolder(bagIt);
            } catch (IOException e) {
                logger.error("Failed to delete the .bagit directory for bag: {}", bag.toAbsolutePath());
            }
        }
        try {
            Files.deleteIfExists(bag.resolve("bagit.txt"));
            Files.deleteIfExists(bag.resolve("bag-info.txt"));
            Files.deleteIfExists(bag.resolve("manifest-sha512.txt"));
            Files.deleteIfExists(bag.resolve("manifest-sha256.txt"));
            Files.deleteIfExists(bag.resolve("manifest-sha1.txt"));
            Files.deleteIfExists(bag.resolve("manifest-md5.txt"));
            Files.deleteIfExists(bag.resolve("tagmanifest-sha512.txt"));
            Files.deleteIfExists(bag.resolve("tagmanifest-sha256.txt"));
            Files.deleteIfExists(bag.resolve("tagmanifest-sha1.txt"));
            Files.deleteIfExists(bag.resolve("tagmanifest-md5.txt"));
        } catch (IOException e) {
            logger.error("Failed to clean up BagIt metadata");
        }
        Path data = bag.resolve("data");
        if (Files.exists(data)) {
            try {
                Path tmpDataPath = bag.resolve(UUID.randomUUID().toString());
                Files.move(data, tmpDataPath);
                if (!moveToFolder(tmpDataPath, bag)) {
                    logger.error("Failed to move files out of BagIt data directory");
                    return;
                }
                Files.deleteIfExists(tmpDataPath);
            } catch (IOException e) {
                logger.error("Failed to move files from {} to {}",
                        data.toString().replace("\\", "\\\\"),
                        bag.toString().replace("\\", "\\\\"));
            }
        }
    }

    public void debagify(File bag) {
        debagify(bag.toPath());
    }

    public void debagify(String bag) {
        debagify(Paths.get(bag));
    }

    public Path archive(File... files) {
        logger.trace("archive('{}'...)", (files.length > 0) ? files[0] : "NULL");

        try {
            Path path;
            switch (archiveCompression) {
                case TAR:
                    path = Paths.get(String.format("%s%s", files[0].getAbsolutePath(), ".tar"));
                    pack(path, files);
                    return path;
                case GZIP:
                    path = Paths.get(String.format("%s%s", files[0].getAbsolutePath(), ".tar.gz"));
                    compress(path, files);
                    return path;
                default:
                    logger.error("Supplied archive type [{}] is not currently supported", archiveCompression);
                    return null;
            }
        } catch (IOException e) {
            logger.error("Failed to archive files: {}", e.getMessage());
            logger.trace(ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public void mtPack(Path path, File... files) throws IOException {
        logger.trace("mtPack('{}','{}')", path.toAbsolutePath(), StringUtils.join(files));
        try (TarArchiveOutputStream out = getTarArchiveOutputStream(path)) {
            ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            for (File file : files)
                addToArchiveCompressionViaExecutor(exec, out, file, ".");
        }
    }

    /**
     * Packs a TAR archive with the given file(s)
     * @param path  Filename for output TAR file
     * @param files File(s) to pack into TAR file
     * @throws IOException Upon failure to create output TAR file
     */
    public void pack(Path path, File... files) throws IOException {
        logger.trace("pack('{}', '{}')", path.toAbsolutePath(), StringUtils.join(files));
        try (TarArchiveOutputStream out = getTarArchiveOutputStream(path)){
            for (File file : files){
                addToArchiveCompression(out, file, ".");
            }
        }
    }

    /**
     * Packs and compresses given file(s)/director(y|ies) into a GZIP'd TAR file
     * @param path  Filename for GZIP'd TAR file
     * @param files File(s) to pack into GZIP'd TAR file
     * @throws IOException Upon failure to create output GZIP'd TAR file
     */
    public void compress(Path path, File... files) throws IOException {
        logger.trace("compress('{}', '{}')", path.toAbsolutePath(), StringUtils.join(files));
        try (TarArchiveOutputStream out = getGZIPTarArchiveOutputStream(path)){
            for (File file : files){
                addToArchiveCompression(out, file, ".");
            }
        }
    }

    private TarArchiveOutputStream getTarArchiveOutputStream(Path path) throws IOException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(Files.newOutputStream(path));
        // TAR has an 8 gig file limit by default, this gets around that
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        return taos;
    }

    private TarArchiveOutputStream getGZIPTarArchiveOutputStream(Path path) throws IOException {
        TarArchiveOutputStream taos = new TarArchiveOutputStream(new GzipCompressorOutputStream(Files.newOutputStream(path)));
        // TAR has an 8 gig file limit by default, this gets around that
        taos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        // TAR originally didn't support long file names, so enable the support for it
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        taos.setAddPaxHeadersForNonAsciiNames(true);
        return taos;
    }

    private void addToArchiveCompression(TarArchiveOutputStream out, File file, String dir) throws IOException {
        String entry = dir + File.separator + file.getName();
        if (file.isFile()){
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)){
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            logger.error(file.getName() + " is not supported");
        }
    }

    private void addToArchiveCompressionViaExecutor(ExecutorService exec, TarArchiveOutputStream out, File file, String dir) throws IOException {
        String entry = dir + File.separator + file.getName();
        if (file.isFile()){
            Runnable addFile = () -> {
                try {
                    out.putArchiveEntry(new TarArchiveEntry(file, entry));
                    try (FileInputStream in = new FileInputStream(file)){
                        IOUtils.copy(in, out);
                    }
                    out.closeArchiveEntry();
                } catch (IOException e) {
                    logger.error("Failed to archive file [{}]", file.getAbsolutePath());
                }
            };
            exec.execute(addFile);
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null){
                for (File child : children){
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            logger.error(file.getName() + " is not supported");
        }
    }

    public boolean unarchive(String in, String out) {
        return unarchive(new File(in), new File(out));
    }

    public boolean unarchive(Path in, Path out) {
        return unarchive(in.toFile(), out.toFile());
    }

    public boolean unarchive(File in, File out) {
        logger.trace("unarchive('{}','{}')", in.getAbsolutePath(), out.getAbsolutePath());
        try {
            Tika tika = new Tika();
            String inType = tika.detect(in);
            logger.trace("Detected type (in): {}", inType);
            switch (inType) {
                case "application/x-tar":
                case "application/x-gtar":
                    return unpack(in, out);
                case "application/gzip":
                    return decompress(in, out);
                default:
                    logger.error("[{}] has archive type [{}] which is unsupported currently", in.getAbsolutePath(), inType);
                    return false;
            }
        } catch (IOException ioe) {
            logger.error("Failed to detect type [{}] - [{}:{}]\n{}",
                    in, ioe.getClass().getCanonicalName(), ioe.getMessage(), ExceptionUtils.getStackTrace(ioe));
            return false;
        }
    }

    public boolean unpack(String in, String out) {
        return unpack(new File(in), new File(out));
    }

    public boolean unpack(File in, File out) {
        logger.trace("unpack('{}','{}')", in.getAbsolutePath(), out.getAbsolutePath());
        if (!in.exists() || !in.isFile()) {
            logger.error("Unpacking input [{}] does not exist or is not a file", in.getAbsolutePath());
            return false;
        }
        if (!out.exists())
            if (!out.mkdirs()) {
                logger.error("Failed to create output directory [{}]", out.getAbsolutePath());
                return false;
            }
        else if (!out.isDirectory()) {
            logger.error("Unpacking output [{}] is not a directory", out.getAbsolutePath());
            return false;
        }
        try (TarArchiveInputStream fin = new TarArchiveInputStream(new FileInputStream(in))){
            extractStream(fin, out);
            return true;
        } catch (FileNotFoundException fnfe) {
            logger.error("Failed to unpack [{}], file not found [{}:{}]",
                    in, fnfe.getClass().getCanonicalName(), fnfe.getMessage());
            return false;
        } catch (IOException ioe) {
            logger.error("Failed to unpack [{}] - [{}:{}]\n{}",
                    in, ioe.getClass().getCanonicalName(), ioe.getMessage(), ExceptionUtils.getStackTrace(ioe));
            return false;
        }
    }

    public boolean decompress(String in, String out) {
        return decompress(new File(in), new File(out));
    }

    public boolean decompress(File in, File out) {
        logger.trace("decompress('{}','{}')", in, out.getAbsolutePath());
        if (!in.exists() || !in.isFile()) {
            logger.error("Decompression input [{}] does not exist or is not a file", in.getAbsolutePath());
            return false;
        }
        if (!out.exists() || !out.isDirectory()) {
            logger.error("Decompression output [{}] does not exist or is not a directory", out.getAbsolutePath());
            return false;
        }
        try (TarArchiveInputStream fin = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(in)))){
            extractStream(fin, out);
            return true;
        } catch (FileNotFoundException fnfe) {
            logger.error("Failed to decompress [{}], file not found [{}:{}]",
                    in, fnfe.getClass().getCanonicalName(), fnfe.getMessage());
            return false;
        } catch (IOException ioe) {
            logger.error("Failed to decompress [{}] -  [{}:{}]\n{}",
                    in, ioe.getClass().getCanonicalName(), ioe.getMessage(), ExceptionUtils.getStackTrace(ioe));
            return false;
        }
    }

    public CPMSLogger getLogger() {
        return logger;
    }

    public void setLogger(CPMSLogger logger) {
        this.logger = logger.cloneLogger(Archiver.class);
    }

    public void updateLogger(CPMSLogger logger) {
        // Todo: Rebuild this section
        /*this.logger.setFlowCellID(logger.getFlowCellID());
        this.logger.setSampleID(logger.getSampleID());
        this.logger.setRequestID(logger.getRequestID());
        this.logger.setStage(logger.getStage());
        this.logger.setStep(logger.getStep());*/
    }

    private void extractStream(TarArchiveInputStream fin, File out) throws IOException {
        TarArchiveEntry entry;
        while ((entry = fin.getNextTarEntry()) != null) {
            if (entry.isDirectory()) {
                continue;
            }
            File curfile = new File(out, entry.getName());
            File parent = curfile.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    logger.error("Failed to create parent directory [{}] in extractStream", parent.getAbsolutePath());
                    throw new IOException(String.format("failed to create parent directory [%s] in extractStream",
                            parent.getAbsolutePath()));
                }
            }
            OutputStream fout = new FileOutputStream(curfile);
            IOUtils.copy(fin, fout);
            IOUtils.closeQuietly(fout);
        }
    }

    private boolean moveToFolder(Path srcFolder, Path dstFolder) {
        try {
            if (!Files.exists(srcFolder)) {
                logger.error("Folder to move [{}] does not exist", srcFolder.toString().replace("\\", "\\\\"));
                return false;
            }
            if (!Files.exists(dstFolder)) {
                logger.error("Destination folder [{}] does not exist", dstFolder.toString().replace("\\", "\\\\"));
                return false;
            }
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(srcFolder)) {
                boolean bSuccess = true;
                for (Path path : directoryStream) {
                    if (!movePath(path.toString(), dstFolder.resolve(path.getFileName()).toString()))
                        bSuccess = false;
                }
                return bSuccess;
            } catch (IOException e) {
                logger.error("Failed to move [{}] to folder [{}] : {}",
                        srcFolder.toString().replace("\\", "\\\\"), dstFolder.toString().replace("\\", "\\\\"),
                        ExceptionUtils.getStackTrace(e).replace("\\", "\\\\"));
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to move [{}] to folder [{}] : {}",
                    srcFolder.toString().replace("\\", "\\\\"), dstFolder.toString().replace("\\", "\\\\"),
                    ExceptionUtils.getStackTrace(e).replace("\\", "\\\\"));
            return false;
        }
    }

    private boolean movePath(String srcPathString, String dstPathString) {
        try {
            Path srcPath = Paths.get(srcPathString);
            if (!Files.exists(srcPath)) {
                logger.error("Folder to move [{}] does not exist", srcPathString.replace("\\", "\\\\"));
                return false;
            }
            Path dstPath = Paths.get(dstPathString);
            Files.deleteIfExists(dstPath);
            long started = System.currentTimeMillis();
            Files.move(srcPath, dstPath, ATOMIC_MOVE);
            logger.trace("Moved folder in {}ms", (System.currentTimeMillis() - started));
            return true;
        } catch (IOException e) {
            logger.error("Failed to move [{}] to [{}] : {}",
                    srcPathString.replace("\\", "\\\\"), dstPathString.replace("\\", "\\\\"),
                    ExceptionUtils.getStackTrace(e).replace("\\", "\\\\"));
            return false;
        }
    }

    /**
     * Deletes an entire folder structure
     * @param folder Path of the folder to delete
     * @throws IOException Thrown from sub-routines
     */
    private void deleteFolder(Path folder) throws IOException {
        logger.trace("deleteFolder({})", folder.toAbsolutePath());
        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
