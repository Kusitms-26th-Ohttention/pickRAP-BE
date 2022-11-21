package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import pickRAP.server.domain.magazine.MagazinePage;

@Data
public class MagazineListResponse {
    @JsonProperty(value="magazine_id")
    private Long magazineId;
    @JsonProperty(value="cover_url")
    private String coverUrl;
    private String title;

    @Builder
    public MagazineListResponse(Long magazineId, String coverUrl, String title) {
        this.magazineId = magazineId;
        this.coverUrl = coverUrl;
        this.title = title;
    }
}
