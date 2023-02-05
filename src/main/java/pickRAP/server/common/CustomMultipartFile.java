package pickRAP.server.common;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

@Getter
public class CustomMultipartFile implements MultipartFile {

    private final byte[] bytes;

    String name;

    String originalFilename;

    String contentType;

    boolean isEmpty;

    long size;

    public CustomMultipartFile(byte[] bytes, String name, String originalFilename,
                               String contentType, long size) {
        this.bytes = bytes;
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.isEmpty = false;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IllegalStateException {
    }
}
