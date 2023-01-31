package pickRAP.server.controller.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class RevisitResponse {

    @JsonProperty("scrap_id")
    Long scrapId;

    // TODO : preview 저장 추가 이후 update
    // String preview;

    String title;

    @QueryProjection
    public RevisitResponse(Long scrapId, String title) {
        this.scrapId = scrapId;
        // this.preview = preview;
        this.title = title;
    }
}
