package pickRAP.server.controller.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class OauthToken {

    private String token_type;

    private String access_token;

    @JsonIgnore
    private int expires_in;

    private String refresh_token;

    private int refresh_token_expires_in;

    private String scope;

    private String id_token;
}
