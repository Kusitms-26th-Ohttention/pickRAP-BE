package pickRAP.server.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.profile.ProfileRequest;
import pickRAP.server.controller.dto.profile.ProfileResponse;
import pickRAP.server.controller.dto.scrap.ScrapRequest;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.profile.ProfileService;
import pickRAP.server.service.scrap.ScrapService;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pickRAP.server.auth.AuthEnv.EMAIL_OK;
import static pickRAP.server.auth.AuthEnv.PASSWORD_OK;
import static pickRAP.server.service.auth.DefaultProfileEnv.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ProfileServiceTest {

    @Autowired AuthService authService;

    @Autowired ScrapService scrapService;

    @Autowired ProfileService profileService;

    @Autowired MemberRepository memberRepository;

    @Autowired ObjectMapper objectMapper;

    @Autowired private MockMvc mockMvc;

    private final String UPDATE_NICKNAME = "pickrap";

    private final String UPDATE_INTRODUCTION = "pickrap ZZang";

    private final List<String> HASHTAGS = List.of("#profile", "#test", "#hashtag");


    private MemberSignUpRequest memberSignUpRequest() {
        return MemberSignUpRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .name("테스트유저")
                .build();
    }

    private MemberSignInRequest memberSignInRequest() {
        return MemberSignInRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .build();
    }

    private ScrapRequest scrapRequest() {
        return ScrapRequest.builder()
                .title("title")
                .scrapType("text")
                .content("content")
                .hashtags(HASHTAGS)
                .build();
    }

    private ProfileRequest profileRequest() {
        return ProfileRequest.builder()
                .nickname(UPDATE_NICKNAME)
                .introduction(UPDATE_INTRODUCTION)
                .hashtags(HASHTAGS)
                .build();
    }

    @BeforeEach
    void before() {
        MemberSignUpRequest memberSignUpRequest = memberSignUpRequest();

        authService.signUp(memberSignUpRequest);
    }

    @Test
    @DisplayName("프로필 테스트 - 기본")
    void getProfileTest() {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();

        //when
        ProfileResponse profile = profileService.getProfile(member.getEmail());

        //then
        assertThat(profile.getNickname()).isEqualTo(DEFAULT_NICKNAME);
        assertThat(profile.getIntroduction()).isEqualTo(DEFAULT_INTRODUCTION);
        assertThat(profile.getProfileImageUrl()).isEqualTo(DEFAULT_IMAGE_URL);
        assertThat(profile.getHashtags().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("프로필 테스트 - 수정(사진X)")
    void updateBasicProfileTest() {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        ScrapRequest scrapRequest = scrapRequest();
        ProfileRequest profileRequest = profileRequest();

        scrapService.save(scrapRequest, null, member.getEmail());

        //when
        profileService.updateProfile(member.getEmail(), profileRequest, null);
        ProfileResponse profile = profileService.getProfile(member.getEmail());

        //then
        assertThat(profile.getNickname()).isEqualTo(UPDATE_NICKNAME);
        assertThat(profile.getIntroduction()).isEqualTo(UPDATE_INTRODUCTION);
        assertThat(profile.getProfileImageUrl()).isEqualTo(DEFAULT_IMAGE_URL);
        assertThat(profile.getHashtags().size()).isEqualTo(HASHTAGS.size());
    }

    @Test
    @DisplayName("프로필 테스트 - 수정(사진O)")
    void updateProfileTest() throws Exception {
        //given
        MemberSignInRequest memberSignInRequest = memberSignInRequest();
        TokenDto tokenDto = authService.signIn(memberSignInRequest);

        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        ScrapRequest scrapRequest = scrapRequest();
        ProfileRequest profileRequest = profileRequest();

        scrapService.save(scrapRequest, null, member.getEmail());

        String content = objectMapper.writeValueAsString(profileRequest);
        final String fileName = "test_image";
        final String contentType = "gif";
        final String filePath = "src/test/resources/image/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image =
                new MockMultipartFile("file", fileName + "." + contentType, "image/" + contentType, fileInputStream);

        MockMultipartFile json =
                new MockMultipartFile("profile_request", "jsondata", "application/json", content.getBytes(StandardCharsets.UTF_8));

        MockMultipartHttpServletRequestBuilder builder = multipart("/profile");
        builder.with(request -> {
            request.setMethod("PUT");
            return request;
        });

        //when
        mockMvc.perform(
                builder.file(json)
                        .file(image)
                        .header(AUTHORIZATION, "Bearer " + tokenDto.getAccessToken())
                        .contentType("multipart/mixed")
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andDo(print());
        ProfileResponse profile = profileService.getProfile(member.getEmail());

        //then
        assertThat(profile.getNickname()).isEqualTo(UPDATE_NICKNAME);
        assertThat(profile.getIntroduction()).isEqualTo(UPDATE_INTRODUCTION);
        assertThat(profile.getHashtags().size()).isEqualTo(HASHTAGS.size());
        assertThat(profile.getProfileImageUrl()).isNotEqualTo(DEFAULT_IMAGE_URL);
    }
}
