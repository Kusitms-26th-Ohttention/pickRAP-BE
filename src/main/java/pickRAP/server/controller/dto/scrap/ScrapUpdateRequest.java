package pickRAP.server.controller.dto.scrap;

import lombok.Data;

import java.util.List;

@Data
public class ScrapUpdateRequest {

    private Long id;

    private String title;

    private String memo;

    private List<HashtagRequest> hashtags;
}
