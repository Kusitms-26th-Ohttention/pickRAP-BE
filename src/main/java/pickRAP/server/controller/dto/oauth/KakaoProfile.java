package pickRAP.server.controller.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoProfile {

    private long id;
    private String connected_at;
    private Properties properties;
    private KakaoAccount kakao_account;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Properties{
        private String nickname;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class KakaoAccount{
        private boolean profile_nickname_needs_agreement;
        private Profile profile;
        private boolean has_email;
        private boolean email_needs_agreement;
        private boolean is_email_valid;
        private boolean is_email_verified;
        private String email;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Profile{
            private String nickname;
        }
    }

}
