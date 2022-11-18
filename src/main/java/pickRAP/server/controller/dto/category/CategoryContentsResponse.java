package pickRAP.server.controller.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import pickRAP.server.domain.scrap.ScrapType;

import java.util.List;

public class CategoryContentsResponse {
    @JsonProperty(value="category_id")
    private Long categoryId;
    private String name;

    @JsonProperty(value="scrap_response_list")
    private List<ScrapResponse> scrapResponseList;

    @Builder
    public CategoryContentsResponse(Long categoryId, String name,
                                    List<ScrapResponse> scrapResponseList) {
        this.categoryId = categoryId;
        this.name = name;
        this.scrapResponseList = scrapResponseList;
    }

    public static class ScrapResponse {
        @JsonProperty(value="scrap_id")
        private Long scrapId;
        private String content;
        @JsonProperty(value="url_preview")
        private String urlPreview;
        @JsonProperty(value="file_url")
        private String fileUrl;
        @JsonProperty(value="scrap_type")
        private ScrapType scrapType;
        private String category;

        @Builder
        public ScrapResponse(Long scrapId, String content,
                             String urlPreview, String fileUrl,
                             ScrapType scrapType, String category) {
            this.scrapId = scrapId;
            this.content = content;
            this.urlPreview = urlPreview;
            this.fileUrl = fileUrl;
            this.scrapType = scrapType;
            this.category = category;
        }
    }
}
