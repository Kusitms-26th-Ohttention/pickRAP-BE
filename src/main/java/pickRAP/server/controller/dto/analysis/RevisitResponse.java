package pickRAP.server.controller.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class RevisitResponse {

    @JsonProperty("scrap_id")
    Long scrapId;

    @JsonProperty("scrap_type")
    String scrapType;

    @JsonProperty("preview_url")
    String previewUrl;

    @JsonProperty("file_url")
    String fileUrl;

    String content;

    String title;

    @QueryProjection
    public RevisitResponse(Long scrapId, String scrapType, String previewUrl, String fileUrl, String content, String title) {
        this.scrapId = scrapId;
        this.scrapType = scrapType;
        this.previewUrl = previewUrl;
        this.fileUrl = fileUrl;
        this.content = content;
        this.title = title;
    }
}
