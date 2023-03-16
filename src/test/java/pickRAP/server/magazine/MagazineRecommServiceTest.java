package pickRAP.server.magazine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.magazine.MagazineColorRequest;
import pickRAP.server.controller.dto.magazine.MagazineListResponse;
import pickRAP.server.controller.dto.magazine.MagazinePageRequest;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
import pickRAP.server.controller.dto.scrap.ScrapRequest;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.magazine.Magazine;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pickRAP.server.magazine.RecommEnv.*;

@SpringBootTest
@Transactional
public class MagazineRecommServiceTest {
    @Autowired
    ScrapRepository scrapRepository;

    @Autowired
    ScrapService scrapService;

    @Autowired
    ScrapHashtagRepository scrapHashtagRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    CategoryService categoryService;

    @Autowired
    MagazineRepository magazineRepository;

    @Autowired
    MagazineService magazineService;

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    HashtagRepository hashtagRepository;

    List<Long> scrapIds = new ArrayList<>();

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

    private ScrapRequest scrapRequest(Long categoryId, List<String> hashtags) {
        return ScrapRequest.builder()
                .title("제목")
                .content("내용")
                .memo("메모")
                .scrapType("text")
                .hashtags(hashtags)
                .categoryId(categoryId)
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

    private MagazineRequest magazineRequest(String title, Long coverScrapId, List<MagazinePageRequest> pageList) {
        return MagazineRequest.builder()
                .title(title)
                .openStatus(true)
                .coverScrapId(coverScrapId)
                .pageList(pageList)
                .build();
    }

    private MagazineColorRequest magazineColorRequest(String colorType) {
        return MagazineColorRequest.builder().colorType(colorType).build();
    }

    void categorySetting() {
        int index = 0;
        while(index < 4) { categoryService.save(categoryRequest(String.valueOf(index++)), Mem1EMAIL); }
        categoryService.save(categoryRequest(String.valueOf(index++)), Mem2EMAIL);
        for(int i = index; i < 31; i++) { categoryService.save(categoryRequest(String.valueOf(i)), Mem4EMAIL); }
    }

    void MagazineSetting(String email, String categoryName, List<String> hashtags) {
        Member member = memberRepository.findByEmail(email).get();
        Category category = categoryRepository.findMemberCategory(categoryName, member.getEmail()).get();
        scrapIds = new ArrayList<>();

        // 스크랩 생성
        scrapService.save(scrapRequest(category.getId(), hashtags), null, member.getEmail());

        if(email.equals(Mem2EMAIL)) { return; }

        Long coverId = saveCover(member, category, "매거진 커버");

        List<Scrap> scrapList = scrapRepository.findByCategoryId(category.getId());
        for(Scrap s : scrapList) { scrapIds.add(s.getId()); }

        // 매거진 생성
        List<MagazinePageRequest> magazinePageRequest = magazinePageRequests();
        MagazineRequest magazineRequest = magazineRequest(hashtags.get(0), coverId, magazinePageRequest);

        magazineService.save(magazineRequest, member.getEmail());
    }

    void PersonalMoodSetting(String email, String hashtag, String colorType) {
        Long magazineId = magazineService.findMagazineByHashtag(hashtag).get(0).getMagazineId();
        magazineService.addMagazineColor(email, magazineId, magazineColorRequest(colorType));
    }

    @BeforeEach
    void setup() {
        List<MemberSignUpRequest> memberSignUpRequestList = new ArrayList<>();
        memberSignUpRequestList.add(memberSignUpRequest(Mem1EMAIL, PASSWORD, "유정민"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem2EMAIL, PASSWORD, "윤태민"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem3EMAIL, PASSWORD, "문민혁"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem4EMAIL, PASSWORD, "대량주"));
        memberSignUpRequestList.add(memberSignUpRequest(Mem5EMAIL, PASSWORD, "퍼스널"));

        for(MemberSignUpRequest m : memberSignUpRequestList) {
            authService.signUp(m);
        }

        categorySetting();

        int index = 0;
        // 기준이 되는 멤버 (TOP3: 백엔드, IT, 구름)
        MagazineSetting(Mem1EMAIL, String.valueOf(index), Arrays.asList("백엔드", "IT", "구름"));
        MagazineSetting(Mem1EMAIL, String.valueOf(index), Arrays.asList("백엔드", "IT", "구름"));
        MagazineSetting(Mem1EMAIL, String.valueOf(index++), Arrays.asList("백엔드", "IT", "구름"));
        MagazineSetting(Mem1EMAIL, String.valueOf(index++), Arrays.asList("공스타"));
        MagazineSetting(Mem1EMAIL, String.valueOf(index++), Arrays.asList("셀카"));
        MagazineSetting(Mem1EMAIL, String.valueOf(index++), Arrays.asList("피크랩"));

        // 스크랩만 있는 멤버
        MagazineSetting(Mem2EMAIL, String.valueOf(index++), Arrays.asList("민머리", "대머리", "탈모"));

        // 매거진 대량 생산 멤버
        // 추천 정책 0번-(1) : 스크랩 해시태그
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("민머리"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("탈모"));

        // 추천 정책 0번-(2) : 가장 많은 퍼스널 무드
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("퍼스널"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("무드"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("컬러"));

        // 추천 정책 1번 : 최근 매거진 3개
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("공스타"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("공스타"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("공스타"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("셀카"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("셀카"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("셀카"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("피크랩"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("피크랩"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("피크랩"));

        // 추천 정책 2번 : TOP3
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT", "백엔드"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT", "백엔드"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT", "백엔드"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("IT", "백엔드", "구름"));

        // 옵션 : 무관한 매거진
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("무관"));

        // 추천 정책 3번 : 사용자가 반응한 매거진
        PersonalMoodSetting(Mem1EMAIL, "탈모", "맑은 민트");
        PersonalMoodSetting(Mem1EMAIL, "민머리", "맑은 민트");
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("프론트엔드", "탈모"));
        MagazineSetting(Mem4EMAIL, String.valueOf(index++), Arrays.asList("개발자", "민머리"));

        // 추천 정책 4번 : 사용자의 퍼스널 무드 분석 결과 -> 같은 반응 매거진
        PersonalMoodSetting(Mem5EMAIL, "탈모", "화려한 레드");
        PersonalMoodSetting(Mem5EMAIL, "퍼스널", "화려한 레드");
        PersonalMoodSetting(Mem5EMAIL, "무드", "시원한 블루");
        PersonalMoodSetting(Mem5EMAIL, "컬러", "포근한 오렌지");

        PersonalMoodSetting(Mem5EMAIL, "공스타", "포근한 오렌지");
        PersonalMoodSetting(Mem5EMAIL, "셀카", "화려한 레드");
        PersonalMoodSetting(Mem5EMAIL, "피크랩", "시원한 블루");
    }

    @Test
    @DisplayName("매거진 추천 - 통합")
    void recommendationMagazineTest() {
        // given

        // when
        List<MagazineListResponse> standardMemberResult = magazineService.recommendedMagazineByMember(Mem1EMAIL);
        List<MagazineListResponse> noMagazineMemberResult = magazineService.recommendedMagazineByMember(Mem2EMAIL);
        List<MagazineListResponse> noScrapMemberResult = magazineService.recommendedMagazineByMember(Mem3EMAIL);

        // then
        assertThat(standardMemberResult.size()).isEqualTo(20);

        assertThat(noMagazineMemberResult.size()).isEqualTo(4);

        assertThat(noScrapMemberResult.size()).isEqualTo(8);
        assertThat(noScrapMemberResult.get(0).getTitle()).isEqualTo("탈모");
    }

    @Test
    @DisplayName("매거진 추천 - 단위")
    void recommendationUnitMagazineTest() {
        // given
        Member member = memberRepository.findByEmail(Mem1EMAIL).orElseThrow();
        List<Magazine> latestCreatedMagazine = magazineRepository.findTop3ByMemberOrderByCreateTimeDesc(member);

        // when
        List<Magazine> latestMagazineResult = magazineService.getRecommendationForLatestMagazine(member, latestCreatedMagazine);
        List<Magazine> Top3MagazineResult = magazineService.getRecommendationForTop3(member);
        List<Magazine> respondedMagazineResult = magazineService.getRecommendationForRespondedMagazine(member);
        List<Magazine> personalMoodMagazineResult = magazineService.getRecommendationForPersonalMood(member);

        //then
        assertThat(latestMagazineResult.size()).isEqualTo(8);
        assertThat(Top3MagazineResult.size()).isEqualTo(6);
        assertThat(respondedMagazineResult.size()).isEqualTo(3);
        assertThat(personalMoodMagazineResult.size()).isEqualTo(3);

    }
}
