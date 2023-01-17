package pickRAP.server.controller.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class TextResponse {

    @JsonProperty("text_word")
    String word;

    @JsonProperty("text_rate")
    long rate;

    @JsonProperty("text_count")
    long count;

    @QueryProjection
    public TextResponse(String word, long count) {
        this.word = word;
        this.count = count;
    }
}
