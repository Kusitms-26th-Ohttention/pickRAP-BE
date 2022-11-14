package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class MagazineUpdateRequest {
    private String title;

    @JsonProperty(value="page_list")
    private List<MagazinePageRequest> pageList;
}
