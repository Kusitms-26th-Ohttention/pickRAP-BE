package pickRAP.server.controller.dto.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class PersonalMoodResponse {

    @JsonProperty("color_style")
    private String colorStyle;

    private long rate;

    public PersonalMoodResponse(String colorStyle, long count, long total) {
        this.colorStyle = colorStyle;
        setRate(count, total);
    }

    private void setRate(long count, long total) {
        rate = (long) ((count/(double)total)*100);
    }


}
