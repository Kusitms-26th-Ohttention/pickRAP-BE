package pickRAP.server.service.oauth;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.controller.dto.oauth.OauthToken;

public interface ProviderEnv {

    OauthToken getOauthToken(RestTemplate rs, HttpHeaders httpHeaders, String code, String state);

    String findProfile(RestTemplate rt, HttpHeaders headers, String token);
}
