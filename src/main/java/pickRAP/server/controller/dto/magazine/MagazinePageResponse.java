package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MagazinePageResponse {
    @JsonProperty(value="page_id")
    private Long pageId;
    //private String contents;
    private String text;
}
