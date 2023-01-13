package pickRAP.server.controller.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class HashTagResponse {

    @JsonProperty("hashtag_name")
    private String tag;

    @JsonProperty("hashtag_rate")
    private long rate;

    @JsonProperty("hashtag_count")
    private long count;

    @QueryProjection
    public HashTagResponse(String tag, long count) {
        this.tag = tag;
        this.count = count;
    }

}

