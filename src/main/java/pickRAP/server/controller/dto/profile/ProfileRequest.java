package pickRAP.server.controller.dto.profile;

import lombok.Data;

@Data
public class ProfileRequest {
    String name;
    String introduction;
    String[] keywords;
}
