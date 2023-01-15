package pickRAP.server.controller.dto.word;

import lombok.Builder;
import lombok.Data;

@Data
public class TextResponse {

    String text;

    long count;

    long rate;

    @Builder
    public TextResponse(String text, long count, long rate) {
        this.text = text;
        this.count = count;
        this.rate = rate;
    }
}
