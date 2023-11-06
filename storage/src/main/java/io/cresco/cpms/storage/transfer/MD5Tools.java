package io.cresco.cpms.storage.transfer;

import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerConfiguration;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static com.amazonaws.services.s3.internal.Constants.MAXIMUM_UPLOAD_PARTS;

public class MD5Tools {
    private static final Logger logger = LoggerFactory.getLogger(MD5Tools.class);
    int partSize;

    MD5Tools(int partSize) {
        this.partSize = partSize;
    }

    String getMultiCheckSum(String fileName) throws IOException {
        logger.debug("Call to getMultiCheckSum [filename = {}]", fileName);
        String mpHash = null;
        FileInputStream fis = null;
        List<String> hashList = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            File inputFile = new File(fileName);
            fis = new FileInputStream(inputFile);
            boolean isReading = true;
            long bytesRead = 0;
            while (isReading) {
                byte[] bs;

                long remaining = inputFile.length() - bytesRead;
                if (remaining > partSize) {
                    bs = new byte[(int)partSize];
                    bytesRead = bytesRead + fis.read(bs, 0, (int)partSize);
                } else {
                    bs = new byte[(int)remaining];
                    bytesRead = bytesRead + fis.read(bs, 0, (int)remaining);
                }
                byte[] hash = md.digest(bs);
                hashList.add(getMD5(hash));
                if (bytesRead == inputFile.length()) {
                    isReading = false;
                }
            }
            mpHash = calculateChecksumForMultipartUpload(hashList);
        } catch (IOException ioe) {
            // Blah
        } catch (Exception ex) {
            System.out.println("MD5Tools : getMultiPartHash Error " + ex.toString());
        } finally {
            try {
                assert fis != null;
                fis.close();
            } catch (AssertionError ae) {
                logger.error("getMultiCheckSum FileInputStream closed prematurely");
            } catch (IOException ioe) {
                logger.error("getMultiCheckSum : fis.close() (IO) {}", ioe.getMessage());
            } catch (Exception e) {
                logger.error("getMultiCheckSum : fis.close() {}", e.getMessage());
            }
        }
        return mpHash;
    }String getMultiCheckSum(String fileName, long minimumUploadPartSize) throws IOException {
        logger.debug("Call to getMultiCheckSum [filename = {}]", fileName);
        String mpHash = null;
        FileInputStream fis = null;
        List<String> hashList = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            File inputFile = new File(fileName);
            long optimalPartSize = calculateOptimalPartSize(inputFile.length(), minimumUploadPartSize);
            fis = new FileInputStream(inputFile);
            boolean isReading = true;
            long bytesRead = 0;
            while (isReading) {
                byte[] bs;

                long remaining = inputFile.length() - bytesRead;
                if (remaining > optimalPartSize) {
                    bs = new byte[(int)optimalPartSize];
                    bytesRead = bytesRead + fis.read(bs, 0, (int)optimalPartSize);
                } else {
                    bs = new byte[(int)remaining];
                    bytesRead = bytesRead + fis.read(bs, 0, (int)remaining);
                }
                byte[] hash = md.digest(bs);
                hashList.add(getMD5(hash));
                if (bytesRead == inputFile.length()) {
                    isReading = false;
                }
            }
            mpHash = calculateChecksumForMultipartUpload(hashList);
        } catch (IOException ioe) {
            // Blah
        } catch (Exception ex) {
            System.out.println("MD5Tools : getMultiPartHash Error " + ex.toString());
        } finally {
            try {
                assert fis != null;
                fis.close();
            } catch (AssertionError ae) {
                logger.error("getMultiCheckSum FileInputStream closed prematurely");
            } catch (IOException ioe) {
                logger.error("getMultiCheckSum : fis.close() (IO) {}", ioe.getMessage());
            } catch (Exception e) {
                logger.error("getMultiCheckSum : fis.close() {}", e.getMessage());
            }
        }
        return mpHash;
    }

    String getMultiCheckSum(String fileName, TransferManager manager) throws IOException {
        logger.debug("Call to getMultiCheckSum [filename = {}]", fileName);
        String mpHash = null;
        FileInputStream fis = null;
        List<String> hashList = new ArrayList<>();

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            File inputFile = new File(fileName);
            long optimalPartSize = calculateOptimalPartSize(inputFile.length(), manager.getConfiguration());
            fis = new FileInputStream(inputFile);
            boolean isReading = true;
            long bytesRead = 0;
            while (isReading) {
                byte[] bs;

                long remaining = inputFile.length() - bytesRead;
                if (remaining > optimalPartSize) {
                    bs = new byte[(int)optimalPartSize];
                    bytesRead = bytesRead + fis.read(bs, 0, (int)optimalPartSize);
                } else {
                    bs = new byte[(int)remaining];
                    bytesRead = bytesRead + fis.read(bs, 0, (int)remaining);
                }
                byte[] hash = md.digest(bs);
                hashList.add(getMD5(hash));
                if (bytesRead == inputFile.length()) {
                    isReading = false;
                }
            }
            mpHash = calculateChecksumForMultipartUpload(hashList);
        } catch (IOException ioe) {
            // Blah
        } catch (Exception ex) {
            System.out.println("MD5Tools : getMultiPartHash Error " + ex.toString());
        } finally {
            try {
                assert fis != null;
                fis.close();
            } catch (AssertionError ae) {
                logger.error("getMultiCheckSum FileInputStream closed prematurely");
            } catch (IOException ioe) {
                logger.error("getMultiCheckSum : fis.close() (IO) {}", ioe.getMessage());
            } catch (Exception e) {
                logger.error("getMultiCheckSum : fis.close() {}", e.getMessage());
            }
        }
        return mpHash;
    }

    private String calculateChecksumForMultipartUpload(List<String> md5s) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String md5 : md5s) {
            stringBuilder.append(md5);
        }

        String hex = stringBuilder.toString();
        byte raw[] = BaseEncoding.base16().decode(hex.toUpperCase());
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putBytes(raw);
        String digest = hasher.hash().toString();

        return digest + "-" + md5s.size();
    }

    private String getMD52(byte[] hash) {
        Hasher hasher = Hashing.md5().newHasher();
        hasher.putBytes(hash);
        return hasher.hash().toString();
    }

    private String getMD5(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte hashByte : hash) {
            //for (int i = 0; i < hash.length; i++) {
            if ((0xff & /*hash[i]*/ hashByte) < 0x10) {
                hexString.append("0");
                hexString.append(Integer.toHexString(0xFF & /*hash[i]*/ hashByte));
            } else {
                hexString.append(Integer.toHexString(0xFF & /*hash[i]*/ hashByte));
            }
        }
        return hexString.toString();
    }

    String getCheckSum(String path) throws IOException {
        String hash = null;
        //FileInputStream fis = null;
        try (FileInputStream fis = new FileInputStream(new File(path))) {
            //fis = new FileInputStream(new File(path));
            hash = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);
        } catch (Exception ex) {
            System.out.println("MD5Tools : getCheckSum Error : " + ex.toString());
        }/* finally {
            fis.close();
		}*/
        return hash;

    }

    public String getCheckSum2(String path) {
        String checksum = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            MessageDigest md = MessageDigest.getInstance("MD5");

            //Using MessageDigest update() method to provide input
            byte[] buffer = new byte[8192];
            long numOfBytesRead;
            while ((numOfBytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, (int) numOfBytesRead);
            }
            byte[] hash = md.digest();
            checksum = new BigInteger(1, hash).toString(16); //don't use this, truncates leading zero
            fis.close();
        } catch (Exception ex) {
            System.out.println("ObjectEngine : checkSum");
        }
        return checksum;
    }

    private long calculateOptimalPartSize(long contentLength, long minimumUploadPartSize) {
        double optimalPartSize = (double)contentLength / (double)MAXIMUM_UPLOAD_PARTS;
        // round up so we don't push the upload over the maximum number of parts
        optimalPartSize = Math.ceil(optimalPartSize);
        return (long)Math.max(optimalPartSize, minimumUploadPartSize);
    }

    private long calculateOptimalPartSize(long contentLength, TransferManagerConfiguration configuration) {
        double optimalPartSize = (double)contentLength / (double)MAXIMUM_UPLOAD_PARTS;
        // round up so we don't push the upload over the maximum number of parts
        optimalPartSize = Math.ceil(optimalPartSize);
        return (long)Math.max(optimalPartSize, configuration.getMinimumUploadPartSize());
    }
}
