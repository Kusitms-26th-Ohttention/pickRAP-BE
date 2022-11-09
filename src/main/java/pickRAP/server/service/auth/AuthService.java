package pickRAP.server.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.config.security.config.SecurityUtil;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.config.security.jwt.TokenProvider;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.member.SocialType;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.util.RedisClient;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

import static pickRAP.server.common.BaseExceptionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final RedisClient redisClient;
    private final PasswordEncoder passwordEncoder;


    /*
    회원가입
     */
    public void signUp(MemberSignUpRequest memberSignUpRequest) {
        if(memberRepository.existsByEmail(memberSignUpRequest.getEmail())){
            throw new BaseException(EXIST_ACCOUNT);
        }
        memberRepository.save(Member.builder()
                        .email(memberSignUpRequest.getEmail())
                        .name(memberSignUpRequest.getName())
                        .password(passwordEncoder.encode(memberSignUpRequest.getPassword()))
                        .profileImageUrl("user_default_profile.png")
                        .socialType(SocialType.NONE)
                        .build()
        );
    }

    /*
    로그인
     */
    public String signIn(MemberSignInRequest memberSignInRequest) {
        return authenticationMember(memberSignInRequest.getEmail(), memberSignInRequest.getPassword());
    }


    /*
    재발급
     */
    public String reissue(HttpServletRequest request) throws IOException {
        // 기존 accessToken 찾기
        String accessToken = tokenProvider.resolveToken(request);
        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        // redis의 refreshToken과의 검증
        String refreshToken = Optional.ofNullable(redisClient.getValues(authentication.getName())).orElseThrow(
                () -> new BaseException(UN_AUTHORIZED)
        );
        if(!tokenProvider.validateToken(refreshToken)){
            throw new BaseException(UN_AUTHORIZED);
        }

        // 토큰 발급
        return generateToken(authentication);
    }

    /*
    사용자 검증
     */
    public String authenticationMember(String principal, String credentials) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principal, credentials
        );
        return generateToken(authenticationManagerBuilder.getObject().authenticate(authenticationToken));
    }


    /*
    토큰 생성
     */
    public String generateToken(Authentication authentication) {
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
        redisClient.setValues(authentication.getName(), tokenDto.getRefreshToken());
        return tokenDto.getAccessToken();
    }

    /*
    로그아웃
     */
    public void logout() {
        redisClient.deleteValues(SecurityUtil.getCurrentMemberId());
    }


}
