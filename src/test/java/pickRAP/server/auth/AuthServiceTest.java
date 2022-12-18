package pickRAP.server.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.config.security.jwt.TokenProvider;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.oauth.OauthService;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static pickRAP.server.auth.AuthEnv.*;
import static pickRAP.server.common.BaseExceptionStatus.*;

@SpringBootTest
@Transactional
public class AuthServiceTest {

    @Autowired MemberRepository memberRepository;
    @Autowired AuthService authService;
    @Autowired OauthService oauthService;
    @Autowired TokenProvider tokenProvider;


    private MemberSignUpRequest memberSignUpRequest() {
        return MemberSignUpRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .name("테스트유저")
                .build();
    }

    private MemberSignInRequest memberSignInRequest(String email) {
        return MemberSignInRequest
                .builder()
                .email(email)
                .password(PASSWORD_OK)
                .build();
    }

    private void createSignUpCase() {
        MemberSignUpRequest signUpReq = memberSignUpRequest();
        authService.signUp(signUpReq);
    }


    @DisplayName("회원가입 테스트")
    @Test
    public void signUpTest() {
        // given
        MemberSignUpRequest signUpReq = memberSignUpRequest();

        // when
        authService.signUp(signUpReq);

        // then
        Member findMember = memberRepository.findByEmail(signUpReq.getEmail()).get();
        assertThat(findMember.getEmail()).isEqualTo(signUpReq.getEmail());
    }

    @DisplayName("중복 검사 테스트")
    @Test
    public void duplicateUserTest() {
        // given
        MemberSignUpRequest signUpReqA = memberSignUpRequest();
        MemberSignUpRequest signUpReqB = memberSignUpRequest();

        // when
        authService.signUp(signUpReqA);
        BaseException e = assertThrows(BaseException.class, () -> authService.signUp(signUpReqB));

        // then
        assertThat(e.getStatus()).isEqualTo(EXIST_ACCOUNT);
    }

    @DisplayName("로그인 테스트 - 성공")
    @Test
    public void signInOkTest() {
        // given
        createSignUpCase();
        MemberSignInRequest signInReq = memberSignInRequest(EMAIL_OK);

        // when
        TokenDto tokenDto = authService.signIn(signInReq);
        String accessToken = tokenDto.getAccessToken();

        // then
        assertThat(EMAIL_OK).isEqualTo(tokenProvider.getAuthentication(accessToken).getName());
    }

    @DisplayName("로그인 테스트 - 실패")
    @Test
    public void signInFailTest() {
        // given
        createSignUpCase();
        MemberSignInRequest signInReq = memberSignInRequest(EMAIL_FAIL);

        // when & then
        assertThrows(BadCredentialsException.class, () -> authService.signIn(signInReq));
    }


}
