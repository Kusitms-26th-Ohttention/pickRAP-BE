package pickRAP.server.util.preview;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import marvin.image.MarvinImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.marvinproject.image.transform.scale.Scale;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.CustomMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static pickRAP.server.util.s3.S3Util.uploadFile;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PreviewUtil {

    public static String createLinkPreview(String url) {
        if(!url.startsWith("http") && !url.startsWith("https")) {
            url = "http://" + url;
        }

        try {
            Document document = Jsoup.connect(url).get();

            return getMetaTagContent(document, "meta[property=og:image]");
        } catch (IOException ex) {
            log.error("Unable to connect to : {}", url);

            return null;
        }
    }

    public static String createPdfPreview(MultipartFile multipartFile) {
        try {
            PDDocument document = PDDocument.load(multipartFile.getInputStream());
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            MultipartFile resizedFile = resizeImage(multipartFile.getOriginalFilename(), bufferedImage, 1000);

            return uploadFile(resizedFile, "preview", "image");
        } catch (IOException e) {
            log.error("error message : {}", e.getMessage());

            return null;
        }
    }

    public static String createVideoPreview(MultipartFile multipartFile) {
        File file = transferToFile(multipartFile);

        try {
            Picture picture = FrameGrab.getFrameFromFile(file, 0);

            BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
            MultipartFile resizedFile = resizeImage(multipartFile.getOriginalFilename(), bufferedImage, 1000);

            file.delete();

            return uploadFile(resizedFile, "preview", "image");
        } catch (IOException|JCodecException e) {
            if (file != null) {
                file.delete();
            }
            log.error("error message : {}", e.getMessage());

            return null;
        }
    }

    private static String getMetaTagContent(Document document, String cssQuery) {
        Element elm = document.select(cssQuery).first();
        if(elm != null) {
            return elm.attr("content");
        }
        return "";
    }

    private static MultipartFile resizeImage(String fileName, BufferedImage bufferedImage, int width) {
        try {
            int originWidth = bufferedImage.getWidth();
            int originHeight = bufferedImage.getHeight();
            MarvinImage marvinImage = new MarvinImage(bufferedImage);
            Scale scale = new Scale();

            scale.load();
            scale.setAttribute("newWidth", width);
            scale.setAttribute("newHeight", (width * originHeight) / originWidth);
            scale.process(marvinImage.clone(), marvinImage, null, null, false);

            BufferedImage imageNoAlpha = marvinImage.getBufferedImageNoAlpha();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(imageNoAlpha, "jpeg", byteArrayOutputStream);
            byteArrayOutputStream.flush();

            return new CustomMultipartFile(byteArrayOutputStream.toByteArray(), fileName, fileName + ".jpeg"
                    , "image/jpeg", byteArrayOutputStream.toByteArray().length);

        } catch (IOException e) {
            log.error("error message : {}", e.getMessage());

            return null;
        }
    }

    private static File transferToFile(MultipartFile multipartFile) {
        try {
            File file = new File(multipartFile.getOriginalFilename());

            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipartFile.getBytes());
            fileOutputStream.close();

            return file;
        } catch (IOException e) {
            log.error("error message : {}", e.getMessage());

            return null;
        }
    }
}
