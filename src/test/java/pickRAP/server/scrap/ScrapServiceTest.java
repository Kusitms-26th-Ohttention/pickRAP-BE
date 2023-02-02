package pickRAP.server.scrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.scrap.ScrapRequest;
import pickRAP.server.controller.dto.scrap.ScrapUpdateRequest;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapHashtag;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;
import pickRAP.server.service.scrap.ScrapService;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pickRAP.server.auth.AuthEnv.EMAIL_OK;
import static pickRAP.server.auth.AuthEnv.PASSWORD_OK;
import static pickRAP.server.common.BaseExceptionStatus.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ScrapServiceTest {

    @Autowired ScrapRepository scrapRepository;

    @Autowired ScrapService scrapService;

    @Autowired ScrapHashtagRepository scrapHashtagRepository;

    @Autowired CategoryRepository categoryRepository;

    @Autowired CategoryService categoryService;

    @Autowired MemberRepository memberRepository;

    @Autowired AuthService authService;

    @Autowired ObjectMapper objectMapper;

    @Autowired private MockMvc mockMvc;

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

    private CategoryRequest categoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .build();
    }

    private ScrapRequest scrapContentRequest(Long categoryId, String title, String content) {
        List<String> hashtags = new ArrayList<>();
        hashtags.add("#대한");
        hashtags.add("#민국");
        hashtags.add("#만세");

        return ScrapRequest.builder()
                .title(title)
                .content(content)
                .memo("메모")
                .scrapType("text")
                .hashtags(hashtags)
                .categoryId(categoryId)
                .build();
    }

    private ScrapRequest scrapFileRequest(Long categoryId) {
        List<String> hashtags = new ArrayList<>();
        hashtags.add("#대한");
        hashtags.add("#민국");
        hashtags.add("#만세");

        return ScrapRequest.builder()
                .title("제목")
                .memo("메모")
                .scrapType("image")
                .hashtags(hashtags)
                .categoryId(categoryId)
                .build();
    }

    private ScrapUpdateRequest scrapUpdateRequest(Long scrapId) {
        List<String> hashtags = new ArrayList<>();
        hashtags.add("#우와");
        hashtags.add("#대박");

        return ScrapUpdateRequest.builder()
                .id(scrapId)
                .title("수정제목")
                .memo("수정메모")
                .hashtags(hashtags)
                .build();
    }

    @BeforeEach
    void before() {
        MemberSignUpRequest memberSignUpRequest = memberSignUpRequest();
        CategoryRequest categoryRequest = categoryRequest("여행");

        authService.signUp(memberSignUpRequest);
        categoryService.save(categoryRequest, EMAIL_OK);
    }

    @Test
    @DisplayName("스크랩 저장 & 조회 - (텍스트, 링크)")
    void saveScrapContentTest() throws IOException {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        ScrapRequest scrapRequest = scrapContentRequest(category.getId(), "제목", "내용");

        //when
        scrapService.save(scrapRequest, null, member.getEmail());

        //then
        List<Scrap> scraps = scrapRepository.findByCategoryId(category.getId());
        assertThat(scraps.size()).isEqualTo(1);
        Scrap scrap = scraps.get(0);
        List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrap.getId());

        assertThat(scrap.getTitle()).isEqualTo(scrapRequest.getTitle());
        assertThat(scrap.getContent()).isEqualTo(scrapRequest.getContent());
        assertThat(scrap.getMemo()).isEqualTo(scrapRequest.getMemo());
        assertThat(scrap.getScrapType().toString()).isEqualTo(scrapRequest.getScrapType().toUpperCase(Locale.ROOT));
        assertThat(scrapHashtags.size()).isEqualTo(3);
        assertThat(scrap.getCategory()).isEqualTo(category);
        assertThat(scrap.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("스크랩 저장 & 조회 - (파일)")
    void saveScrapFileTest() throws Exception {
        //given
        MemberSignInRequest memberSignInRequest = memberSignInRequest();
        TokenDto tokenDto = authService.signIn(memberSignInRequest);

        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        ScrapRequest scrapRequest = scrapFileRequest(category.getId());
        String content = objectMapper.writeValueAsString(scrapRequest);

        final String fileName = "test_image";
        final String contentType = "gif";
        final String filePath = "src/test/resources/image/" + fileName + "." + contentType;
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MockMultipartFile image =
                new MockMultipartFile("file", fileName + "." + contentType, "image/" + contentType, fileInputStream);

        MockMultipartFile json =
                new MockMultipartFile("scrap_request", "jsondata", "application/json", content.getBytes(StandardCharsets.UTF_8));

        //when
        mockMvc.perform(
                    multipart("/scrap")
                    .file(json)
                    .file(image)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenDto.getAccessToken())
                    .contentType("multipart/mixed")
                    .accept(APPLICATION_JSON)
                    .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andDo(print());

        //then
    }

    @Test
    @DisplayName("스크랩 수정")
    void updateScrapTest() throws IOException {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        ScrapRequest scrapRequest = scrapContentRequest(category.getId(), "제목", "내용");
        scrapService.save(scrapRequest, null, member.getEmail());
        List<Scrap> scraps = scrapRepository.findByCategoryId(category.getId());
        Scrap scrap = scraps.get(0);
        ScrapUpdateRequest scrapUpdateRequest = scrapUpdateRequest(scrap.getId());

        //when
        scrapService.update(scrapUpdateRequest, member.getEmail());

        //then
        Scrap findScrap = scrapRepository.findById(scrap.getId()).get();
        List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrap.getId());

        assertThat(findScrap.getTitle()).isEqualTo(scrapUpdateRequest.getTitle());
        assertThat(findScrap.getMemo()).isEqualTo(scrapUpdateRequest.getMemo());
        assertThat(scrapHashtags.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("스크랩 삭제")
    void deleteScrapTest() throws IOException {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        ScrapRequest scrapRequest = scrapContentRequest(category.getId(), "제목", "내용");
        scrapService.save(scrapRequest, null, member.getEmail());
        List<Scrap> scraps = scrapRepository.findByCategoryId(category.getId());
        Scrap scrap = scraps.get(0);

        //when
        scrapService.delete(scrap.getId(), member.getEmail());

        //then
        Optional<Scrap> findScrap = scrapRepository.findById(scrap.getId());
        assertThat(findScrap).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("예외 - 빈 값 전달")
    void scrapExceptionTest1() {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        ScrapRequest scrapRequest = scrapContentRequest(category.getId(), "제목?", "");

        //when
        BaseException e = assertThrows(BaseException.class,
                () -> scrapService.save(scrapRequest, null, member.getEmail()));

        //then
        assertThat(e.getStatus()).isEqualTo(EMPTY_INPUT_VALUE);
    }

    @Test
    @DisplayName("예외 - 제목 길이 초과")
    void scrapExceptionTest2() throws IOException {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        ScrapRequest scrapRequest = scrapContentRequest(category.getId(), "이건15글자가넘는제목이에요나는몇글자일까요?", "내용");

        //when
        BaseException e = assertThrows(BaseException.class,
                () -> scrapService.save(scrapRequest, null, member.getEmail()));

        //then
        assertThat(e.getStatus()).isEqualTo(SCRAP_TITLE_LONG);
    }
}
