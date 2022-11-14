package pickRAP.server.controller.dto.magazine;

import lombok.Data;
import pickRAP.server.domain.magazine.MagazinePage;

@Data
public class MagazineListResponse {
    private Long magazineId;
    private String thumbnail;
    private String title;

    public MagazineListResponse(Long magazineId, String title) {
        this.magazineId = magazineId;
        this.title = title;
    }
}
