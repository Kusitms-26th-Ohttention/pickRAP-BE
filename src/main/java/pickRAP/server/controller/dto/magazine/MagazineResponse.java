package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MagazineResponse {
    @JsonProperty(value="magazine_id")
    private Long magazineId;
    private String title;

    private boolean openStatus;

    @JsonProperty(value="created_date")
    private LocalDateTime createdDate;

    @JsonProperty(value="page_list")
    List<MagazinePageResponse> pageList;
}
