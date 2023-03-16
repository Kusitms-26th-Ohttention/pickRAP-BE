package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.Hashtag.HashtagPageResponse;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.hashtag.HashtagService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hashtag")
public class HashtagController {

    private final AuthService authService;

    private final HashtagService hashtagService;

    @GetMapping
    @ApiOperation(value = "프로필 설정을 위한 해시태그 가져오기", notes = "페이징, 중복X<br>" +
            "초기 요청 : https://api.pickrap.com/hashtag?page=<br>" +
            "이후 요청 : https://api.pickrap.com/hashtag?page=next_hashtag_id<br>" +
            "next_hashtag_id : 이전 응답에 포함, null 값이면 이후 페이지 없음")
    @ApiResponse(responseCode = "500", description = "서버 예외")
    public ResponseEntity<BaseResponse<HashtagPageResponse>> getSliceHashtag(@PageableDefault(size = 20) Pageable pageable) {
        String email = authService.getUserEmail();

        HashtagPageResponse hashtagPageResponse = hashtagService.getSliceHashtagPageResponse(email, pageable);

        return ResponseEntity.ok(new BaseResponse(hashtagPageResponse));
    }
}
