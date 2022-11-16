package pickRAP.server.controller.dto.scrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pickRAP.server.domain.scrap.ScrapType;

import java.util.List;

@Data
public class ScrapRequest {

    private String title;

    private String content;

    private String memo;

    @JsonProperty("scrap_type")
    private ScrapType scrapType;

    private List<HashtagRequest> hashtags;

    @JsonProperty("category_id")
    private Long categoryId;
}