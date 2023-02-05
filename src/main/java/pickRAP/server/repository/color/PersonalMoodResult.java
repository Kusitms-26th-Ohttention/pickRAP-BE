package pickRAP.server.repository.color;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import pickRAP.server.domain.magazine.ColorType;

@Data
public class PersonalMoodResult {

    private ColorType colorStyle;

    private long count;

    @QueryProjection
    public PersonalMoodResult(ColorType colorStyle, long count) {
        this.colorStyle = colorStyle;
        this.count = count;
    }
}
