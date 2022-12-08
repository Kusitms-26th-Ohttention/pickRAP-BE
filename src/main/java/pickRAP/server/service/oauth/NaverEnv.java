package pickRAP.server.service.oauth;

import io.netty.util.CharsetUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.controller.dto.oauth.NaverProfile;
import pickRAP.server.controller.dto.oauth.OauthToken;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.member.SocialType;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.auth.DefaultImageEnv;
import pickRAP.server.service.category.CategoryService;

import java.net.URLDecoder;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverEnv implements ProviderEnv{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final CategoryService categoryService;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUrl;

    @Value("${naver.token-uri}")
    private String tokenUrl;

    @Value("${naver.user-uri}")
    private String userUrl;


    @Override
    public OauthToken getOauthToken(RestTemplate rt, HttpHeaders headers, String code, String state) {
        log.info("getOauthToken 호출");
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type","authorization_code");
        params.add("client_id", this.clientId );
        params.add("client_secret", this.clientSecret );
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> TokenRequest = new HttpEntity<>(params, headers);

        log.info("uri = {}", this.tokenUrl);
        ResponseEntity<String> accessTokenResponse = rt.exchange(
                this.tokenUrl,
                HttpMethod.POST,
                TokenRequest,
                String.class
        );

        log.info("토큰 응답값 = {}", accessTokenResponse.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        OauthToken oauthToken = null;

        try {
            oauthToken = objectMapper.readValue(accessTokenResponse.getBody(), OauthToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return oauthToken;
    }

    @Override
    public TokenDto findProfile(RestTemplate rt, HttpHeaders headers, String token) {
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


        HttpEntity<MultiValueMap<String, String>> ProfileRequest = new HttpEntity<>(headers);


        ResponseEntity<String> profileResponse = rt.exchange(
                this.userUrl,
                HttpMethod.GET,
                ProfileRequest,
                String.class
        );
        log.info("프로필 응답값 = {}", profileResponse.getBody());

        ObjectMapper objectMapper = new ObjectMapper();
        NaverProfile naverProfile = null;

        try {
            naverProfile = objectMapper.readValue(profileResponse.getBody(), NaverProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return socialLogin(naverProfile);
    }


    public TokenDto socialLogin(NaverProfile profile) {
        if (memberRepository.findByEmail(profile.getResponse().getId() + profile.getResponse().getEmail()).isEmpty()){
            Member member = Member.builder()
                    .email(profile.getResponse().getId() + profile.getResponse().getEmail())
                    .name(URLDecoder.decode(profile.getResponse().getName(), CharsetUtil.UTF_8))
                    .password(passwordEncoder.encode(profile.getResponse().getEmail()))
                    .profileImageUrl(DefaultImageEnv.DEFAULT_IMAGE_URL)
                    .socialType(SocialType.NAVER)
                    .build();
            memberRepository.save(member);
            categoryService.initial(member);
        }

        return authService.authenticationMember(profile.getResponse().getId() + profile.getResponse().getEmail(), profile.getResponse().getEmail());
    }
}
