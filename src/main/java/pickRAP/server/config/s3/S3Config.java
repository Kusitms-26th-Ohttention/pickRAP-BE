package pickRAP.server.config.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component("s3Config")
public class S3Config {

    private static String accessKey;

    private static String secretKey;

    private static String region;

    @Value("${cloud.aws.credentials.access-key}")
    public void setAccessKey(String aKey) {
        accessKey = aKey;
    }

    @Value("${cloud.aws.credentials.secret-key}")
    public void setSecretKey(String sKey) {
        secretKey = sKey;
    }

    @Value("${cloud.aws.region.static}")
    public void setRegion(String reg) {
        region = reg;
    }

    public static AmazonS3Client amazonS3Client() {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKey, secretKey);

        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .build();
    }
}
