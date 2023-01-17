package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.analysis.AnalysisResponse;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.hashtag.HashtagService;
import pickRAP.server.service.text.TextService;


@Slf4j
@RequestMapping("/analysis")
@RestController
@RequiredArgsConstructor
public class AnalysisController {

    private final AuthService authService;
    private final HashtagService hashtagService;
    private final TextService textService;

    @GetMapping
    @ApiOperation(value = "분석", notes = "해시태그 분석 & 텍스트 분석<br>" +
            "parameter 정리<br>" +
            "- filter : all(전체), recent(3개월), year(년), month(월)<br>" +
            "- year : filter 가 year or month 인 경우<br>" +
            "- month : filter 가 month 인 경우"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<AnalysisResponse>> getAnalysis(@RequestParam("filter") String filter
            , @RequestParam(value="year", required = false) Integer year
            , @RequestParam(value="month", required = false) Integer month) {

        String email = authService.getUserEmail();
        // 해시태그 분석
        AnalysisResponse analysisResponse = hashtagService.getHashtagAnalysisResults(filter, year, month, email);

        // 텍스트 분석
        analysisResponse.setTexts(textService.getTextAnalysisResults(email));

        return ResponseEntity.ok(new BaseResponse(analysisResponse));
    }
}
