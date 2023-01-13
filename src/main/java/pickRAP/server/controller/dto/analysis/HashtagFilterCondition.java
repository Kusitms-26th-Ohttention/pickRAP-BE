package pickRAP.server.controller.dto.analysis;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HashtagFilterCondition {

    private String filter;

    private Integer year;

    private Integer month;

}
