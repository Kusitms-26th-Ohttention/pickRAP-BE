package pickRAP.server.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class S3Util {

    private static String bucket;

    @Value("${cloud.aws.s3.bucket}")
    public void setBucket(String buc) {
        bucket = buc;
    }

    private static final AmazonS3Client amazonS3Client = amazonS3Client();

    //단일 파일 올리기 (s3 파일 이름 반환)
    public static String uploadFile(MultipartFile multipartFile, String dir, String scrapType) {
        try {
            if(!checkFile(multipartFile.getContentType().substring(multipartFile.getContentType().lastIndexOf("/")), scrapType)) {
                throw new BaseException(BaseExceptionStatus.NOT_SUPPORT_FILE);
            }

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

    public static boolean checkFile(String contentType, String scrapType) {
        if (contentType.contains("png") || contentType.contains("jpeg")
                || contentType.contains("gif") || contentType.contains("bmp")) {

            if (scrapType.equals("image")) {
                return true;
            } else {
                throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE_FILE_SCRAP);
            }
        } else if (contentType.contains("pdf")) {

            if (scrapType.equals("pdf")) {
                return true;
            } else {
                throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE_FILE_SCRAP);
            }
        } else if (contentType.contains("mp4") || contentType.contains("mov")
                || contentType.contains("ogg") || contentType.contains("wmv")
                || contentType.contains("avi") || contentType.contains("avchd")
                || contentType.contains("mpeg") || contentType.contains("mkv")
                || contentType.contains("webm")) {


            if (scrapType.equals("video")) {
                return true;
            } else {
                throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE_FILE_SCRAP);
            }
        } else {
            return false;
        }
    }
}
