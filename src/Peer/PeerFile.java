package Peer;

public class PeerFile {

    private long fileSize;
    private String fileName;
    private PeerInfo fileSource;
    private String base64;

    public PeerFile(String fileName, long fileSize, String base64) {
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.base64 = base64;
    }

    public PeerFile(String fileName, long fileSize, PeerInfo fileSource) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileSource = fileSource;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public PeerInfo getFileSource() {
        return fileSource;
    }

    public String getBase64() {
        return base64;
    }
}
