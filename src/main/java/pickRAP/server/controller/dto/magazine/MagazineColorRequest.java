package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MagazineColorRequest {

    @JsonProperty("color_style")
    private String colorType;

}
