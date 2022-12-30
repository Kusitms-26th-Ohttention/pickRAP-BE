package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagazineRequest {
    private String title;

    @JsonProperty(value="open_status")
    private boolean openStatus;

    @JsonProperty(value="cover_scrap_id")
    private Long coverScrapId;

    @JsonProperty(value="page_list")
    private List<MagazinePageRequest> pageList;
}
