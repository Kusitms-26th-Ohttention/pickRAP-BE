package pickRAP.server.controller.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import pickRAP.server.domain.scrap.ScrapType;

@Data
public class CategoryScrapResponse {

    private Long id;

    private String name;

    @JsonProperty("scrap_type")
    private String scrapType;

    private String content;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("url_preview")
    private String urlPreview;

    @Builder
    public CategoryScrapResponse(Long id, String name, String scrapType, String content, String fileUrl) {
        this.id = id;
        this.name = name;
        this.scrapType = scrapType;
        this.content = content;
        this.fileUrl = fileUrl;
    }
}
