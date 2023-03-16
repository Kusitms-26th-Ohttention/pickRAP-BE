package pickRAP.server.controller.dto.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import pickRAP.server.controller.dto.Hashtag.HashtagResponse;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ProfileResponse {

    String nickname;

    String introduction;

    @JsonProperty("profile_image_url")
    String profileImageUrl;

    List<HashtagResponse> hashtags;
}
