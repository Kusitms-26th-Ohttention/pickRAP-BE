package pickRAP.server.magazine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.magazine.MagazineListResponse;
import pickRAP.server.controller.dto.magazine.MagazinePageRequest;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
import pickRAP.server.controller.dto.scrap.ScrapRequest;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.hashtag.HashtagRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;
import pickRAP.server.service.magazine.MagazineService;
import pickRAP.server.service.scrap.ScrapService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pickRAP.server.auth.AuthEnv.*;
import static pickRAP.server.common.BaseExceptionStatus.*;

@SpringBootTest
@Transactional
public class MagazineServiceTest {
    @Autowired ScrapRepository scrapRepository;

    @Autowired ScrapService scrapService;

    @Autowired ScrapHashtagRepository scrapHashtagRepository;

    @Autowired CategoryRepository categoryRepository;

    @Autowired CategoryService categoryService;

    @Autowired MagazineRepository magazineRepository;

    @Autowired MagazineService magazineService;

    @Autowired AuthService authService;

    @Autowired MemberRepository memberRepository;

    @Autowired HashtagRepository hashtagRepository;

    List<Long> scrapIds = new ArrayList<>();
    List<Long> coverIds = new ArrayList<>();

    private MemberSignUpRequest memberSignUpRequest() {
        return MemberSignUpRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .name("테스트유저")
                .build();
    }

    private CategoryRequest categoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .build();
    }

    private ScrapRequest scrapRequest(Long categoryId, String hashtag1, String hashtag2, String hashtag3) {
        List<String> hashtags = new ArrayList<>();
        hashtags.add(hashtag1);
        hashtags.add(hashtag2);
        hashtags.add(hashtag3);

        return ScrapRequest.builder()
                .title("제목")
                .content("내용")
                .memo("메모")
                .scrapType("text")
                .hashtags(hashtags)
                .categoryId(categoryId)
                .build();
    }

    private MagazineRequest magazineRequest(String title, boolean openStatus, Long coverScrapId, List<MagazinePageRequest> pageList) {
        return MagazineRequest.builder()
                .title(title)
                .openStatus(openStatus)
                .coverScrapId(coverScrapId)
                .pageList(pageList)
                .build();
    }

    private List<MagazinePageRequest> magazinePageRequests() {
        List<MagazinePageRequest> list = new ArrayList<>();
        for(int i = 0; i < scrapIds.size(); i++) {
            list.add(
                    MagazinePageRequest.builder()
                            .scrapId(scrapIds.get(i))
                            .text("매거진 텍스트")
                            .build()
            );
        }
        return list;
    }

    private Long saveCover(Member member, Category category, String title) {
        Scrap cover = Scrap.builder()
                .title(title)
                .memo("커버 메모")
                .fileUrl("커버 URL")
                .scrapType(ScrapType.IMAGE)
                .build();
        cover.setMember(member);
        cover.setCategory(category);

        return scrapRepository.save(cover).getId();
    }

    @BeforeEach
    void before() {
        MemberSignUpRequest memberSignUpRequest = memberSignUpRequest();
        CategoryRequest categoryRequest = categoryRequest("여행");
        List<ScrapRequest> scrapRequests = new ArrayList<>();

        authService.signUp(memberSignUpRequest);
        categoryService.save(categoryRequest, EMAIL_OK);

        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();

        scrapRequests.add(scrapRequest(category.getId(), "대한", "민국", "만세"));
        scrapRequests.add(scrapRequest(category.getId(), "대한", "민국", "만세"));
        scrapRequests.add(scrapRequest(category.getId(), "대한", "민국", "만세"));

        for(ScrapRequest sr : scrapRequests) {
            scrapService.save(sr, null, member.getEmail());
        }
        coverIds.add(saveCover(member, category, "커버 제목"));
        coverIds.add(saveCover(member, category, "커버 수정 제목"));
    }

    @Test
    @DisplayName("매거진과 페이지 저장")
    void saveMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("매거진 제목", true, coverIds.get(0), magazinePageRequest);

        // when
        magazineService.save(magazineRequest, member.getEmail());

        // then
        Magazine magazine = magazineRepository.findByTitleAndMember("매거진 제목", member).get();
        assertThat(magazine.getTitle()).isEqualTo(magazineRequest.getTitle());
        assertThat(magazine.isOpenStatus()).isEqualTo(magazineRequest.isOpenStatus());
        assertThat(magazine.getMember()).isEqualTo(member);

        Scrap cover = scrapRepository.findById(coverIds.get(0)).get();
        assertThat(magazine.getCover()).isEqualTo(cover.getFileUrl());

        List<MagazinePage> page = magazine.getPages();
        assertThat(page.size()).isEqualTo(scrapIds.size());
        assertThat(page.get(0).getScrap().getId()).isEqualTo(scrapIds.get(0));
        assertThat(page.get(0).getText()).isEqualTo("매거진 텍스트");
    }

    @Test
    @DisplayName("매거진 수정")
    void updateMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("매거진 제목", true, coverIds.get(0), magazinePageRequest);
        magazineService.save(magazineRequest, member.getEmail());
        Magazine magazine = magazineRepository.findByTitleAndMember("매거진 제목", member).get();
        MagazineRequest magazineUpdateRequest = magazineRequest("매거진 제목 수정", false, coverIds.get(1), magazinePageRequest);

        // when
        magazineService.updateMagazine(magazineUpdateRequest, magazine.getId(), member.getEmail());

        // then
        Magazine updateMagazine = magazineRepository.findByTitleAndMember("매거진 제목 수정", member).get();
        assertThat(updateMagazine.getTitle()).isEqualTo(magazineUpdateRequest.getTitle());
        assertThat(updateMagazine.isOpenStatus()).isEqualTo(magazineUpdateRequest.isOpenStatus());
        assertThat(updateMagazine.getMember()).isEqualTo(member);

        Scrap cover = scrapRepository.findById(coverIds.get(1)).get();
        assertThat(updateMagazine.getCover()).isEqualTo(cover.getFileUrl());
    }

    @Test
    @DisplayName("매거진 삭제")
    void deleteMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("매거진 제목", true, coverIds.get(0), magazinePageRequest);
        magazineService.save(magazineRequest, member.getEmail());
        Magazine magazine = magazineRepository.findByTitleAndMember("매거진 제목", member).get();

        // when
        magazineService.deleteMagazine(magazine.getId(), member.getEmail());

        // then
        Optional<Magazine> findMagazine = magazineRepository.findById(magazine.getId());
        assertThat(findMagazine).isEmpty();
    }

    @Test
    @DisplayName("매거진 검색")
    void searchMagazineTest() {
        // given
        String keyword = "완전";

        // when
        List<MagazineListResponse> searchMagazines = magazineService.findMagazineByHashtag(keyword);

        // then
        assertThat(searchMagazines.size()).isEqualTo(1);
        assertThat(searchMagazines.get(0).getTitle()).isEqualTo("무관 매거진");
    }

    @Test
    @DisplayName("매거진 예외 - 매거진 제목 글자수 초과")
    void magazineExceptionTest_매거진_제목_글자수_초과() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("매거진 제목은 15자 넘기지 말아요", true, 0L, magazinePageRequest);

        // when
        BaseException e = assertThrows(BaseException.class,
                () -> magazineService.save(magazineRequest, member.getEmail()));

        // then
        assertThat(e.getStatus()).isEqualTo(EXCEED_TITLE_LENGTH);
    }

    @Test
    @DisplayName("매거진 예외 - 존재하지 않는 매거진 커버")
    void magazineExceptionTest_존재하지_않는_매거진_커버() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("매거진 제목", true, 100L, magazinePageRequest);

        // when
        BaseException e = assertThrows(BaseException.class,
                () -> magazineService.save(magazineRequest, member.getEmail()));

        // then
        assertThat(e.getStatus()).isEqualTo(DONT_EXIST_SCRAP);
    }

    @Test
    @DisplayName("매거진 예외 - 커버 스크랩 타입")
    void magazineExceptionTest_커버_타입() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("여행", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("매거진 제목", true, scrapIds.get(0), magazinePageRequest);

        // when
        BaseException e = assertThrows(BaseException.class,
                () -> magazineService.save(magazineRequest, member.getEmail()));

        // then
        assertThat(e.getStatus()).isEqualTo(DONT_MATCH_TYPE);
    }
}