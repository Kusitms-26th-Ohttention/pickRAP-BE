package pickRAP.server.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pickRAP.server.common.BaseException;
import pickRAP.server.config.security.config.SecurityUtil;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.config.security.jwt.TokenProvider;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.member.SocialType;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.service.category.CategoryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static pickRAP.server.common.BaseExceptionStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;


    /*
    회원가입
     */
    public void signUp(MemberSignUpRequest memberSignUpRequest) {
        if(memberRepository.existsByEmail(memberSignUpRequest.getEmail())){
            throw new BaseException(EXIST_ACCOUNT);
        }
        Member member = Member.builder()
                .email(memberSignUpRequest.getEmail())
                .name(memberSignUpRequest.getName())
                .password(passwordEncoder.encode(memberSignUpRequest.getPassword()))
                .profileImageUrl(DefaultImageEnv.DEFAULT_IMAGE_URL)
                .socialType(SocialType.NONE)
                .build();
        memberRepository.save(member);
        categoryService.initial(member);
    }

    /*
    로그인
     */
    public TokenDto signIn(MemberSignInRequest memberSignInRequest) {
        return authenticationMember(memberSignInRequest.getEmail(), memberSignInRequest.getPassword());
    }


    /*
    재발급
     */
    public TokenDto reissue(String refreshToken, HttpServletRequest request) throws IOException {
        // refreshToken 만료 시
        if (refreshToken == null) {
            throw new BaseException(EXPIRED_REFRESH);
        }

        // 기존 accessToken 찾기
        String accessToken = tokenProvider.resolveToken(request);
        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        // 토큰 발급
        return generateToken(authentication);
    }

    /*
    사용자 검증
     */
    public TokenDto authenticationMember(String principal, String credentials) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principal, credentials
        );
        return generateToken(authenticationManagerBuilder.getObject().authenticate(authenticationToken));
    }


    /*
    토큰 생성
     */
    public TokenDto generateToken(Authentication authentication) {
        return tokenProvider.generateTokenDto(authentication);
    }

    /*
    로그아웃
     */
    public void logout(HttpServletResponse response) {

        // 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("RefreshToken", null)
                .maxAge(0)
                .path("/")
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .build();

        response.setHeader("Set-Cookie", cookie.toString());
    }

    /*
    사용자 아이디
     */
    public String getUserEmail() {
        return SecurityUtil.getCurrentMemberId();
    }
}
