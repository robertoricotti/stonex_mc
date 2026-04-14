package cloud;

public class S3Credentials {
    private String accessKey;
    private String secretKey;
    private String sessionToken;
    private String region;
    private String bucketName;
    private long expirationTime;

    public S3Credentials(String region, String accessKey, String secretKey, String sessionToken, String bucketName, long expirationTime) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.sessionToken = sessionToken;
        this.region = region;
        this.bucketName = bucketName;
        this.expirationTime = expirationTime;

    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
