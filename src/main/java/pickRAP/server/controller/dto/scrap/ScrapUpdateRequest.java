package pickRAP.server.controller.dto.scrap;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScrapUpdateRequest {

    private Long id;

    private String title;

    private String memo;

    private List<String> hashtags;
}
