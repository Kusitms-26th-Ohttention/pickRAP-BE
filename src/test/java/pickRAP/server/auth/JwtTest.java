package pickRAP.server.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.config.security.jwt.TokenProvider;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.service.auth.AuthService;

import javax.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pickRAP.server.auth.AuthEnv.*;
import static pickRAP.server.config.security.jwt.TokenProvider.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class JwtTest {

    @Autowired private MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired AuthService authService;
    @Autowired TokenProvider tokenProvider;


    @DisplayName("로그인 쿠키발급 테스트")
    @Test
    public void signInTest() throws Exception {
        // given
        authService.signUp(MemberSignUpRequest.builder().email(EMAIL_OK).password(PASSWORD_OK).build());

        MemberSignInRequest signInReq = MemberSignInRequest
                .builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .build();

        // when & then
        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signInReq))
                ).andExpect(status().isOk())
                .andExpect(cookie().exists(COOKIE_NAME))
                .andExpect(result ->
                        assertThat(tokenProvider.getAuthentication(result.getResponse().getHeader(AUTHORIZATION_HEADER).substring(7)).getName())
                                .isEqualTo(EMAIL_OK))
                .andDo(print());
    }

    @DisplayName("로그아웃 테스트")
    @Test
    public void logOutTest() throws Exception {

        // when & then
        mockMvc.perform(post("/log-out")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX + createTestToken())
                ).andExpect(cookie().maxAge(COOKIE_NAME, 0))
                .andDo(print());

    }

    @DisplayName("토큰 재발급 테스트 - 실패")
    @Test
    public void reissueFailTest() throws Exception {

        // when & then
        mockMvc.perform(post("/auth/reissue")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX+createTestToken())
                ).andExpect(status().is4xxClientError())
                .andDo(print());

    }

    private String createTestToken() {
        return tokenProvider.createTestToken();
    }

    private Cookie setRefreshToken(String refreshToken, int expire) {
        Cookie cookie = new Cookie(COOKIE_NAME, refreshToken);
        cookie.setMaxAge(expire);
        return cookie;
    }

    @DisplayName("토큰 재발급 테스트 - 성공")
    @Test
    public void reissueOkTest() throws Exception {
        // given
        Cookie cookie = setRefreshToken(REFRESH_TOKEN, 60);

        // when & then
        mockMvc.perform(post("/auth/reissue")
                        .header(AUTHORIZATION_HEADER, BEARER_PREFIX+createTestToken())
                        .cookie(cookie)
                ).andExpect(status().isOk())
                .andExpect(result -> {
                    assertThat(result.getResponse().getCookie(COOKIE_NAME).getValue()).isNotEqualTo(REFRESH_TOKEN);
                    assertThat(result.getResponse().getHeader(AUTHORIZATION_HEADER)).isNotEqualTo(createTestToken());
                })
                .andDo(print());
    }
}
