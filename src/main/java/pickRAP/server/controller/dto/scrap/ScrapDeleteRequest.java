package pickRAP.server.controller.dto.scrap;

import lombok.Data;

import java.util.List;

@Data
public class ScrapDeleteRequest {

    private List<Long> id;
}
