package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MagazineColorResponse {

    @JsonProperty("color_style")
    private String colorType;

}
