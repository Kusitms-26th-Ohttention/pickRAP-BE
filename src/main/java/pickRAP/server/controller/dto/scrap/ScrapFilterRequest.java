package pickRAP.server.controller.dto.scrap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ScrapFilterRequest {

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("search_keyword")
    private String searchKeyword;

    @JsonProperty("order_keyword")
    private String orderKeyword;
}
