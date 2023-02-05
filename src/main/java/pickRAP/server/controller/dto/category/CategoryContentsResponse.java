package pickRAP.server.controller.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import pickRAP.server.domain.scrap.ScrapType;

import java.util.List;

public class CategoryContentsResponse {
    @JsonProperty(value="category_id")
    private Long categoryId;
    @JsonProperty(value="name")
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
        @JsonProperty(value="content")
        private String content;
        @JsonProperty(value="preview_url")
        private String previewUrl;
        @JsonProperty(value="file_url")
        private String fileUrl;
        @JsonProperty(value="scrap_type")
        private ScrapType scrapType;
        @JsonProperty(value="category")
        private String category;

        @Builder
        public ScrapResponse(Long scrapId, String content,
                             String previewUrl, String fileUrl,
                             ScrapType scrapType, String category) {
            this.scrapId = scrapId;
            this.content = content;
            this.previewUrl = previewUrl;
            this.fileUrl = fileUrl;
            this.scrapType = scrapType;
            this.category = category;
        }
    }
}
