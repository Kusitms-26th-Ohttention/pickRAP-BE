package pickRAP.server.common;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

@Slf4j
public class URLPreview {
    public static String getLinkPreviewInfo(String url) {
        if(!url.startsWith("http") && !url.startsWith("https")) {
            url = "http://" + url;
        }

        try {
            Document document = Jsoup.connect(url).get();
            String ogImage = getMetaTagContent(document, "meta[property=og:image]");

            return ogImage;
        } catch (IOException ex) {
            log.error("Unable to connect to : {}", url);
        }

        return null;
    }

    private static String getMetaTagContent(Document document, String cssQuery) {
        Element elm = document.select(cssQuery).first();
        if(elm != null) {
            return elm.attr("content");
        }
        return "";
    }
}
