package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagazinePageRequest {
    @JsonProperty(value="scrap_id")
    private Long scrapId;
    private String text;
}
