package pickRAP.server.controller.dto.Hashtag;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

@Data
public class HashtagResponse {

    String tag;

    boolean use;

    @Builder
    @QueryProjection
    public HashtagResponse(String tag, boolean use) {
        this.tag = tag;
        this.use = use;
    }
}
