package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import pickRAP.server.domain.magazine.MagazinePage;

@Data
public class MagazineListResponse {
    @JsonProperty(value="magazine_id")
    private Long magazineId;
    @JsonProperty(value="magazine_cover")
    private String magazineCover;
    private String title;

    @Builder
    public MagazineListResponse(Long magazineId, String magazineCover, String title) {
        this.magazineId = magazineId;
        this.magazineCover = magazineCover;
        this.title = title;
    }
}
