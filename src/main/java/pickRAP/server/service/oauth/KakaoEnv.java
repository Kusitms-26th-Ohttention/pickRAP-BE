package pickRAP.server.service.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
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
import pickRAP.server.controller.dto.oauth.KakaoProfile;
import pickRAP.server.controller.dto.oauth.OauthToken;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.member.SocialType;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoEnv implements ProviderEnv{

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final CategoryService categoryService;

    @Value("${kakao.client-id}")
    private String clientId;

    private String clientSecret = null;

    @Value("${kakao.redirect-uri}")
    private String redirectUrl;

    @Value("${kakao.token-uri}")
    private String tokenUrl;

    @Value("${kakao.user-uri}")
    private String userUrl;


    @Override
    public OauthToken getOauthToken(RestTemplate rt, HttpHeaders headers, String code, String state) {
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type","authorization_code");
        params.add("client_id", this.clientId );
        params.add("redirect_uri", this.redirectUrl);
        params.add("code", code);

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
    public String findProfile(RestTemplate rt, HttpHeaders headers, String token) {
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


        HttpEntity<MultiValueMap<String, String>> ProfileRequest = new HttpEntity<>(headers);


        ResponseEntity<String> profileResponse = rt.exchange(
                this.userUrl,
                HttpMethod.GET,
                ProfileRequest,
                String.class
        );


        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = objectMapper.readValue(profileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return socialLogin(kakaoProfile);
    }


    public String socialLogin(KakaoProfile profile) {
        if (memberRepository.findByEmail(Long.toString(profile.getId()) + profile.getKakao_account().getEmail()).isEmpty()) {
            Member member = Member.builder()
                    .email(Long.toString(profile.getId()) + profile.getKakao_account().getEmail())
                    .password(passwordEncoder.encode(profile.getKakao_account().getEmail()))
                    .name(profile.getKakao_account().getProfile().getNickname())
                    .profileImageUrl("user_default_profile.png")
                    .socialType(SocialType.KAKAO)
                    .build();
            memberRepository.save(member);
            categoryService.initial(member);
        }

        return authService.authenticationMember(Long.toString(profile.getId()) + profile.getKakao_account().getEmail(), profile.getKakao_account().getEmail());
    }
}
