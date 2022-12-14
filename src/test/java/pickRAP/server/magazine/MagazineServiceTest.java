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
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;
import pickRAP.server.service.magazine.MagazineService;
import pickRAP.server.service.scrap.ScrapService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pickRAP.server.auth.AuthEnv.EMAIL_OK;
import static pickRAP.server.auth.AuthEnv.PASSWORD_OK;
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

    List<Long> scrapIds = new ArrayList<>();
    List<Long> coverIds = new ArrayList<>();

    private MemberSignUpRequest memberSignUpRequest() {
        return MemberSignUpRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .name("???????????????")
                .build();
    }

    private CategoryRequest categoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .build();
    }

    private ScrapRequest scrapRequest(Long categoryId) {
        List<String> hashtags = new ArrayList<>();
        hashtags.add("#??????");
        hashtags.add("#??????");
        hashtags.add("#??????");

        return ScrapRequest.builder()
                .title("??????")
                .content("??????")
                .memo("??????")
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
                            .text("????????? ?????????")
                            .build()
            );
        }
        return list;
    }

    private Long saveCover(Member member, Category category, String title) {
        Scrap cover = Scrap.builder()
                .title(title)
                .memo("?????? ??????")
                .fileUrl("?????? URL")
                .scrapType(ScrapType.IMAGE)
                .build();
        cover.setMember(member);
        cover.setCategory(category);

        return scrapRepository.save(cover).getId();
    }

    @BeforeEach
    void before() throws IOException {
        MemberSignUpRequest memberSignUpRequest = memberSignUpRequest();
        CategoryRequest categoryRequest = categoryRequest("??????");
        List<ScrapRequest> scrapRequests = new ArrayList<>();

        authService.signUp(memberSignUpRequest);
        categoryService.save(categoryRequest, EMAIL_OK);

        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();

        scrapRequests.add(scrapRequest(category.getId()));
        scrapRequests.add(scrapRequest(category.getId()));
        scrapRequests.add(scrapRequest(category.getId()));

        for(ScrapRequest sr : scrapRequests) {
            scrapService.save(sr, null, member.getEmail());
        }
        coverIds.add(saveCover(member, category, "?????? ??????"));
        coverIds.add(saveCover(member, category, "?????? ?????? ??????"));
    }

    @Test
    @DisplayName("???????????? ????????? ??????")
    void saveMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("????????? ??????", true, coverIds.get(0), magazinePageRequest);

        // when
        magazineService.save(magazineRequest, member.getEmail());

        // then
        Magazine magazine = magazineRepository.findByTitleAndMember("????????? ??????", member).get();
        assertThat(magazine.getTitle()).isEqualTo(magazineRequest.getTitle());
        assertThat(magazine.isOpenStatus()).isEqualTo(magazineRequest.isOpenStatus());
        assertThat(magazine.getMember()).isEqualTo(member);

        Scrap cover = scrapRepository.findById(coverIds.get(0)).get();
        assertThat(magazine.getCover()).isEqualTo(cover.getFileUrl());

        List<MagazinePage> page = magazine.getPages();
        assertThat(page.size()).isEqualTo(scrapIds.size());
        assertThat(page.get(0).getScrap().getId()).isEqualTo(scrapIds.get(0));
        assertThat(page.get(0).getText()).isEqualTo("????????? ?????????");
    }

    @Test
    @DisplayName("????????? ??????")
    void updateMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("????????? ??????", true, coverIds.get(0), magazinePageRequest);
        magazineService.save(magazineRequest, member.getEmail());
        Magazine magazine = magazineRepository.findByTitleAndMember("????????? ??????", member).get();
        MagazineRequest magazineUpdateRequest = magazineRequest("????????? ?????? ??????", false, coverIds.get(1), magazinePageRequest);

        // when
        magazineService.updateMagazine(magazineUpdateRequest, magazine.getId(), member.getEmail());

        // then
        Magazine updateMagazine = magazineRepository.findByTitleAndMember("????????? ?????? ??????", member).get();
        assertThat(updateMagazine.getTitle()).isEqualTo(magazineUpdateRequest.getTitle());
        assertThat(updateMagazine.isOpenStatus()).isEqualTo(magazineUpdateRequest.isOpenStatus());
        assertThat(updateMagazine.getMember()).isEqualTo(member);

        Scrap cover = scrapRepository.findById(coverIds.get(1)).get();
        assertThat(updateMagazine.getCover()).isEqualTo(cover.getFileUrl());
    }

    @Test
    @DisplayName("????????? ??????")
    void deleteMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("????????? ??????", true, coverIds.get(0), magazinePageRequest);
        magazineService.save(magazineRequest, member.getEmail());
        Magazine magazine = magazineRepository.findByTitleAndMember("????????? ??????", member).get();

        // when
        magazineService.deleteMagazine(magazine.getId(), member.getEmail());

        // then
        Optional<Magazine> findMagazine = magazineRepository.findById(magazine.getId());
        assertThat(findMagazine).isEmpty();
    }

    @Test
    @DisplayName("????????? ?????? - ????????? ?????? ????????? ??????")
    void magazineExceptionTest_?????????_??????_?????????_??????() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("????????? ????????? 15??? ????????? ?????????", true, 0L, magazinePageRequest);

        // when
        BaseException e = assertThrows(BaseException.class,
                () -> magazineService.save(magazineRequest, member.getEmail()));

        // then
        assertThat(e.getStatus()).isEqualTo(EXCEED_TITLE_LENGTH);
    }

    @Test
    @DisplayName("????????? ?????? - ???????????? ?????? ????????? ??????")
    void magazineExceptionTest_????????????_??????_?????????_??????() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("????????? ??????", true, 100L, magazinePageRequest);

        // when
        BaseException e = assertThrows(BaseException.class,
                () -> magazineService.save(magazineRequest, member.getEmail()));

        // then
        assertThat(e.getStatus()).isEqualTo(DONT_EXIST_SCRAP);
    }

    @Test
    @DisplayName("????????? ?????? - ?????? ????????? ??????")
    void magazineExceptionTest_??????_??????() {
        // given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        Category category = categoryRepository.findMemberCategory("??????", member.getEmail()).get();
        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());

        for(Scrap s : scrapList) {
            scrapIds.add(s.getId());
        }
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest("????????? ??????", true, scrapIds.get(0), magazinePageRequest);

        // when
        BaseException e = assertThrows(BaseException.class,
                () -> magazineService.save(magazineRequest, member.getEmail()));

        // then
        assertThat(e.getStatus()).isEqualTo(DONT_MATCH_TYPE);
    }
}