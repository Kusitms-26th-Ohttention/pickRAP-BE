package pickRAP.server.controller.dto.scrap;

import lombok.Builder;
import lombok.Data;
import pickRAP.server.domain.scrap.ScrapType;

@Data
public class ScrapFilterCondition {

    private ScrapType scrapType;

    private Long memberId;

    private Long categoryId;

    private String searchKeyword;

    @Builder
    public ScrapFilterCondition(ScrapType scrapType, Long categoryId, Long memberId, String searchKeyword) {
        this.scrapType = scrapType;
        this.memberId = memberId;
        this.categoryId = categoryId;
        this.searchKeyword = searchKeyword;
    }
}
