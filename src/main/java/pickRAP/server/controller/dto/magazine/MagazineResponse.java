package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import pickRAP.server.domain.magazine.MagazineTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MagazineResponse {
    @JsonProperty(value="magazine_id")
    private Long magazineId;
    private String title;

    @JsonProperty(value="template_type")
    private MagazineTemplate templateType;

    private boolean openStatus;

    @JsonProperty(value="created_date")
    private LocalDateTime createdDate;

    @JsonProperty(value="page_list")
    List<MagazinePageResponse> pageList;
}
