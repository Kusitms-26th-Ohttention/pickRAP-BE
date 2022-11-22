package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MagazineResponse {
    @JsonProperty(value="magazine_id")
    private Long magazineId;
    private String title;

    @JsonProperty(value="open_status")
    private boolean openStatus;

    @JsonProperty(value="created_date")
    private LocalDateTime createdDate;

    @JsonProperty(value="page_list")
    List<MagazinePageResponse> pageList;

    @Builder
    public MagazineResponse(Long magazineId, String title, boolean openStatus,
                            LocalDateTime createdDate, List<MagazinePageResponse> pageList) {
        this.magazineId = magazineId;
        this.title = title;
        this.openStatus = openStatus;
        this.createdDate = createdDate;
        this.pageList = pageList;
    }
}
