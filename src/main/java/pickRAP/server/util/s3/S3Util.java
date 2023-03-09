package pickRAP.server.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static pickRAP.server.config.s3.S3Config.amazonS3Client;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@DependsOn("s3Config")
public class S3Util {

    private static final AmazonS3Client amazonS3Client = amazonS3Client();

    private static String bucket;

    @Value("${cloud.aws.credentials.access-key}")
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    //단일 파일 올리기 (s3 파일 이름 반환)
    public static String uploadFile(MultipartFile multipartFile, String dir, String scrapType) {
        try {
            checkFile(multipartFile.getContentType(), scrapType);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            InputStream inputStream = multipartFile.getInputStream();
            objectMetadata.setContentLength(inputStream.available());
            String fileName = dir + "/" + UUID.randomUUID() +
                    multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));

            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata));

            return amazonS3Client.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new BaseException(BaseExceptionStatus.FILE_UPLOAD_FAIL);
        }
    }

    public static void checkFile(String contentType, String scrapType) {
        if (contentType.contains("image")) {
            if (!scrapType.equals("image")) {
                throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE_FILE_SCRAP);
            }
        } else if (contentType.contains("video")) {
            if (!scrapType.equals("video")) {
                throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE_FILE_SCRAP);
            }
        } else if (contentType.contains("pdf")) {
            if (!scrapType.equals("pdf")) {
                throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE_FILE_SCRAP);
            }
        } else {
            throw new BaseException(BaseExceptionStatus.NOT_SUPPORT_FILE);
        }
    }
}
