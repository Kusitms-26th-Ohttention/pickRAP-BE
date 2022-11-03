package pickRAP.server.service.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.controller.dto.oauth.OauthToken;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthService {

    private final KakaoEnv kakaoEnv;

    private final NaverEnv naverEnv;

    public String socialAuth(String provider, String code, String state){

        ProviderEnv providerEnv = null;

        if(provider.equals("kakao")) {
            providerEnv = kakaoEnv;
        }
        else if(provider.equals("naver")){
            providerEnv = naverEnv;
        }

        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        OauthToken oauthToken = providerEnv.getOauthToken(rt, headers, code, state);

        return providerEnv.findProfile(rt, headers, oauthToken.getAccess_token());
    }
}


