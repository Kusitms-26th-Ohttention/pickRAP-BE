package pickRAP.server.service.scrap;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static pickRAP.server.common.URLPreview.*;

public class PreviewService {

    public String createLinkPreview(String content) {
        return getLinkPreviewInfo(content);
    }

    public String createPdfPreview(MultipartFile multipartFile) throws IOException {
        PDDocument document = PDDocument.load(multipartFile.getInputStream());
        PDPage page = document.getPage(0);

    }
}
