package pickRAP.server.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
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
    public static String uploadFile(MultipartFile multipartFile, String dir, String scrapType) throws IOException {
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

    //파일 가져와서 http 전송
    public static ResponseEntity<byte[]> downloadFile(String fileName, String dir) throws Exception {
        try {
            S3Object s3Object = amazonS3Client.getObject(new GetObjectRequest(bucket, dir + "/" + fileName));
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            byte[] file = IOUtils.toByteArray(s3ObjectInputStream);

            String responseFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            HttpHeaders httpHeaders = new HttpHeaders();
            setContentType(httpHeaders, fileName.substring(fileName.lastIndexOf(".")));
            httpHeaders.setContentLength(file.length);
            httpHeaders.setContentDispositionFormData("attachment", responseFileName);

            return new ResponseEntity<>(file, httpHeaders, HttpStatus.OK);
        } catch (Exception e) {
            throw new BaseException(BaseExceptionStatus.FILE_DOWNLOAD_FAIL);
        }
    }

    //db 구축 완료되면 테이블에 file_content_type 추가해서 업로드 시 저장해 놓고 그걸로 사용하기
    public static void setContentType(HttpHeaders httpHeaders, String contentType) {
        if(contentType.contains("png")) {
            httpHeaders.set("Content-Type", "image/png");
        } else if(contentType.contains("jpeg")) {
            httpHeaders.set("Content-Type", "image/jpeg");
        } else if(contentType.contains("gif")) {
            httpHeaders.set("Content-Type", "image/gif");
        } else if(contentType.contains("bmp")) {
            httpHeaders.set("Content-Type", "image/bmp");
        } else if(contentType.contains("pdf")) {
            httpHeaders.set("Content-Type", "application/pdf");
        } else if(contentType.contains("mp4") || contentType.contains("mov")
                || contentType.contains("webm") || contentType.contains("ogg")
                || contentType.contains("wmv") || contentType.contains("avi")
                || contentType.contains("avchd") || contentType.contains("mpeg")
                || contentType.contains("mkv")) {
            httpHeaders.set("Content-Type", "video/webm");
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
