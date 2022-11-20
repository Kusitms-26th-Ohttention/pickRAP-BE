package pickRAP.server.controller.dto.scrap;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Slice;

@Data
public class ScrapPageResponse {

    Long nextScrapId;

    Slice<ScrapResponse> scrapResponses;

    @Builder
    public ScrapPageResponse(Long nextScrapId, Slice<ScrapResponse> scrapResponses) {
        this.nextScrapId = nextScrapId;
        this.scrapResponses = scrapResponses;
    }
}
