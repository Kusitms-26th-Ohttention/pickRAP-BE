package pickRAP.server.analysis.hashtag;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.analysis.AnalysisResponse;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.scrap.ScrapRequest;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.hashtag.HashtagService;
import pickRAP.server.service.scrap.ScrapService;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pickRAP.server.auth.AuthEnv.EMAIL_OK;
import static pickRAP.server.auth.AuthEnv.PASSWORD_OK;

@SpringBootTest
@Transactional
public class HashtagServiceTest {

    @Autowired
    HashtagService hashtagService;
    @Autowired
    ScrapService scrapService;
    @Autowired
    AuthService authService;


    private static final String[] analysisTags = {"#나는", "#누구", "#여긴", "기타"};
    private static final int[] analysisCounts = {4, 3, 2, 2};
    private static final int[] analysisRates = {37, 27, 18, 18};

    private MemberSignUpRequest memberSignUpRequest() {
        return MemberSignUpRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .name("테스트유저")
                .build();
    }

    private ScrapRequest scrapRequest(List<String> hashtags) {
        return ScrapRequest.builder()
                .title("민머리 방지법")
                .content("풍성한 머리")
                .memo("젊을 때 관리하자")
                .scrapType("text")
                .hashtags(hashtags)
                .build();
    }

    /**
     * #나는 : 36% -> 37%
     * #누구 : 27%
     * #여긴 : 18%
     * 기타 : 18%
     * 총합 : 99% -> 100%
     */
    @BeforeEach
    void saveScraps() {
        authService.signUp(memberSignUpRequest());
        // 스크랩 생성
        ScrapRequest scrapRequestA = scrapRequest(Arrays.asList("#나는", "#누구", "#여긴", "#어디"));
        ScrapRequest scrapRequestB = scrapRequest(Arrays.asList("#나는", "#누구", "#여긴"));
        ScrapRequest scrapRequestC = scrapRequest(Arrays.asList("#나는", "#누구"));
        ScrapRequest scrapRequestD = scrapRequest(Arrays.asList("#나는"));
        ScrapRequest scrapRequestE = scrapRequest(Arrays.asList("#정수파괴범"));

        // 스크랩 저장
        scrapService.save(scrapRequestA, null, EMAIL_OK);
        scrapService.save(scrapRequestB, null, EMAIL_OK);
        scrapService.save(scrapRequestC, null, EMAIL_OK);
        scrapService.save(scrapRequestD, null, EMAIL_OK);
        scrapService.save(scrapRequestE, null, EMAIL_OK);
    }


    @DisplayName("해시태그 분석 테스트")
    @Test
    void hashtagAnalysisBasicTest() {
        // when
        AnalysisResponse analysisResponse = hashtagService.getHashtagAnalysisResults("all", null, null, EMAIL_OK);
        List<HashTagResponse> hashtags = analysisResponse.getHashtags();
        // then
        int sum = 0;
        for (int i = 0; i < 4; i++) {
            assertThat(hashtags.get(i).getTag()).isEqualTo(analysisTags[i]);
            assertThat(hashtags.get(i).getCount()).isEqualTo(analysisCounts[i]);
            assertThat(hashtags.get(i).getRate()).isEqualTo(analysisRates[i]);
            sum += hashtags.get(i).getRate();
        }
        assertThat(sum).isEqualTo(100);
    }



}
