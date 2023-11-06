package io.cresco.cpms.statics;

public class CPMSStatics {
    // Archiver
    public static final BagItType DEFAULT_BAGIT_TYPE = BagItType.DotFile;
    public static final BagItHashingAlgorithm DEFAULT_BAGIT_HASHING = BagItHashingAlgorithm.MD5;
    public static final boolean DEFAULT_HIDDEN_FILES = true;
    public static final ArchiveCompression DEFAULT_ARCHIVE_COMPRESSION = ArchiveCompression.GZIP;

    // ObjectStorage
    public static final String UNCOMPRESSED_SIZE_METADATA_TAG_KEY = "uncompressedsize";
    public static final String PART_SIZE_METADATA_TAG_KEY = "partsize";
    public static final int DEFAULT_PART_SIZE = 8;

    // Telemetry
    public static final int HEARTBEAT_INTERVAL_IN_SECONDS = 5;
}
