package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MagazineCover {
    @JsonProperty(value="scrap_id")
    private Long scrapId;
}
