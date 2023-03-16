package pickRAP.server.analysis.color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.analysis.PersonalMoodResponse;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.category.CategoryResponse;
import pickRAP.server.controller.dto.magazine.MagazineColorRequest;
import pickRAP.server.controller.dto.magazine.MagazinePageRequest;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
import pickRAP.server.controller.dto.scrap.ScrapRequest;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;
import pickRAP.server.service.color.ColorService;
import pickRAP.server.service.magazine.MagazineService;
import pickRAP.server.service.scrap.ScrapService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static pickRAP.server.magazine.RecommEnv.*;
import static pickRAP.server.magazine.RecommEnv.PASSWORD;

@SpringBootTest
@Transactional
public class ColorServiceTest {

    @Autowired
    ColorService colorService;
    @Autowired
    MagazineService magazineService;
    @Autowired
    MagazineRepository magazineRepository;
    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ScrapService scrapService;
    @Autowired
    ScrapRepository scrapRepository;
    @Autowired
    AuthService authService;
    @Autowired
    MemberRepository memberRepository;

    private final static String colorStyleResult[] = {"화려한 레드", "따뜻한 레몬", "맑은 민트"};
    private final static long rateResult[] = {68, 16, 16};

    private MemberSignUpRequest memberSignUpRequest(String email, String password, String name) {
        return MemberSignUpRequest.builder()
                .email(email)
                .password(password)
                .name(name)
                .build();
    }

    private CategoryRequest categoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .build();
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

    private ScrapRequest scrapRequest(Long categoryId) {
        return ScrapRequest.builder()
                .title("제목")
                .content("내용")
                .memo("메모")
                .scrapType("text")
                .hashtags(Arrays.asList("#맥모닝","#먹고싶다"))
                .categoryId(categoryId)
                .build();
    }


    @BeforeEach
    void setUp() {
        // 멤버
        List<MemberSignUpRequest> memberSignUpRequestList = new ArrayList<>();
        memberSignUpRequestList.add(memberSignUpRequest(Mem1EMAIL, PASSWORD, "매거진제작자"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem2EMAIL, PASSWORD, "memberA"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem3EMAIL, PASSWORD, "memberB"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem4EMAIL, PASSWORD, "memberC"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem5EMAIL, PASSWORD, "memberD"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem6EMAIL, PASSWORD, "memberE"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem7EMAIL, PASSWORD, "memberF"));

        for(MemberSignUpRequest m : memberSignUpRequestList) {
            authService.signUp(m);
        }

        // 카테고리
        CategoryRequest categoryRequest = categoryRequest("카테고리");
        CategoryResponse categoryResponse = categoryService.save(categoryRequest, Mem1EMAIL);
        Category category = categoryRepository.findById(categoryResponse.getId()).orElseThrow();

        // 커버
        Member member = memberRepository.findByEmail(Mem1EMAIL).orElseThrow();
        Long coverId = saveCover(member, category, "커버제목");

        // 스크랩
        scrapService.save(scrapRequest(categoryResponse.getId()), null, Mem1EMAIL);
        List<Scrap> scraps = scrapRepository.findByCategoryId(categoryResponse.getId());

        // 매거진
        Long scrapId = scraps.get(0).getId();
        MagazinePageRequest magazinePageRequest = MagazinePageRequest.builder().scrapId(scrapId).text("텍스트").build();

        // 매거진
        MagazineRequest magazineRequest = MagazineRequest.builder()
                                .title("제목")
                                .openStatus(true)
                                .coverScrapId(coverId)
                                .pageList(Arrays.asList(magazinePageRequest))
                                .build();

        magazineService.save(magazineRequest, Mem1EMAIL);
    }

    /**
     * 화려한 레드 : 66% -> 68%
     * 따뜻한 레몬 : 16%
     * 맑은 민트 : 16%
     * 총합 : 98% -> 100%
     */
    @Test
    @DisplayName("색반응 분석")
    void colorReactionAnalysisTest() {
        // given
        Member member = memberRepository.findByEmail(Mem1EMAIL).orElseThrow();
        Magazine magazine = magazineRepository.findByTitleAndMember("제목", member).orElseThrow();
        MagazineColorRequest magazineColorRequestA = MagazineColorRequest.builder().colorType("화려한 레드").build();
        MagazineColorRequest magazineColorRequestB = MagazineColorRequest.builder().colorType("화려한 레드").build();
        MagazineColorRequest magazineColorRequestC = MagazineColorRequest.builder().colorType("화려한 레드").build();
        MagazineColorRequest magazineColorRequestD = MagazineColorRequest.builder().colorType("화려한 레드").build();
        MagazineColorRequest magazineColorRequestE = MagazineColorRequest.builder().colorType("따뜻한 레몬").build();
        MagazineColorRequest magazineColorRequestF = MagazineColorRequest.builder().colorType("맑은 민트").build();

        magazineService.addMagazineColor(Mem2EMAIL, magazine.getId(), magazineColorRequestA);
        magazineService.addMagazineColor(Mem3EMAIL, magazine.getId(), magazineColorRequestB);
        magazineService.addMagazineColor(Mem4EMAIL, magazine.getId(), magazineColorRequestC);
        magazineService.addMagazineColor(Mem5EMAIL, magazine.getId(), magazineColorRequestD);
        magazineService.addMagazineColor(Mem6EMAIL, magazine.getId(), magazineColorRequestE);
        magazineService.addMagazineColor(Mem7EMAIL, magazine.getId(), magazineColorRequestF);


        // when
        List<PersonalMoodResponse> results = colorService.getPersonalMoodAnalysisResults(Mem1EMAIL);

        // then
        long sum = 0;
        for (int i = 0; i < 3; i++) {
            assertThat(results.get(i).getColorStyle()).isEqualTo(colorStyleResult[i]);
            assertThat(results.get(i).getRate()).isEqualTo(rateResult[i]);
            sum += results.get(i).getRate();
        }
        assertThat(sum).isEqualTo(100);

    }



}
