package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MagazineColorRequest {

    @JsonProperty("color_style")
    private String colorType;

}
