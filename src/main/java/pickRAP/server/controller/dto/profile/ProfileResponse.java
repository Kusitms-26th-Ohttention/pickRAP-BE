package pickRAP.server.controller.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    String name;
    String description;
    String imageUrl;
    String[] keywords;
}
