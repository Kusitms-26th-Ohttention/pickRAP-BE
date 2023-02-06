package pickRAP.server.controller.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalysisResponse {

    private List<HashTagResponse> hashtags;

    @JsonProperty("personal_mood_results")
    private List<PersonalMoodResponse> personalMoods;

    private List<TextResponse> texts;
}
