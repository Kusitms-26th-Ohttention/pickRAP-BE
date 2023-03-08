package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pickRAP.server.domain.magazine.Magazine;

@Data
public class MagazineListResponse {
    @JsonProperty(value="magazine_id")
    private Long magazineId;
    @JsonProperty(value="cover_url")
    private String coverUrl;
    private String title;

    public MagazineListResponse(Magazine magazine) {
        this.magazineId = magazine.getId();
        this.coverUrl = magazine.getCover();
        this.title = magazine.getTitle();
    }
}
