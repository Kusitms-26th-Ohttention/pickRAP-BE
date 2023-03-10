package pickRAP.server.controller.dto.Hashtag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class HashtagPageResponse {

    @JsonProperty("next_hashtag_id")
    Long nextHashtagId;

    @JsonProperty("hashtag_responses")
    List<HashtagResponse> hashtagResponses;

    @Builder
    public HashtagPageResponse(Long nextHashtagId, List<HashtagResponse> hashtagResponses) {
        this.nextHashtagId = nextHashtagId;
        this.hashtagResponses = hashtagResponses;
    }
}
