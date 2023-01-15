package pickRAP.server.controller.dto.analysis;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalysisResponse {

    private List<HashTagResponse> hashtags;

    private List<TextResponse> texts;
}
