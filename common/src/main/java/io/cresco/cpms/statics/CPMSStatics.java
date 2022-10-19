package io.cresco.cpms.statics;

public class CPMSStatics {
    // Archiver
    public static final String DEFAULT_BAGIT_TYPE = "standard";
    public static final String DEFAULT_BAGIT_HASHING = "md5";
    public static final boolean DEFAULT_HIDDEN_FILES = true;
    public static final String DEFAULT_ARCHIVE_COMPRESSION = "tar";

    // ObjectStorage
    public static final String UNCOMPRESSED_SIZE_METADATA_TAG_KEY = "uncompressedsize";
    public static final String PART_SIZE_METADATA_TAG_KEY = "partsize";
    public static final int DEFAULT_PART_SIZE = 8;
}
