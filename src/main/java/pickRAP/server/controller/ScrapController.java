package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.scrap.*;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.scrap.ScrapService;

import java.util.List;

import static pickRAP.server.common.BaseExceptionStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scrap")
public class ScrapController {

    private final ScrapService scrapService;

    private final AuthService authService;

    @GetMapping("{id}")
    @ApiOperation(value = "스크랩 상세보기", notes = "스크랩 상세보기")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4009-스크랩미존재"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<ScrapResponse>> selectScrap(@PathVariable Long id) {
        ScrapResponse scrapResponse = scrapService.findOne(id, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(scrapResponse));
    }

    @GetMapping
    @ApiOperation(value = "스크랩 검색", notes = "query string에 search_keyword(검색어), order_keyword(desc는 최근생성, asc 오래된생성순)")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<ScrapPageResponse>> searchScraps(@RequestParam(name = "search_keyword") String searchKeyword,
                                                                           @RequestParam(name = "order_keyword") String orderKeyword,
                                                                           @PageableDefault(size = 10) Pageable pageable) {
        Slice<ScrapResponse> scrapResponses = scrapService.searchPageScraps(searchKeyword, orderKeyword, authService.getUserEmail(), pageable);
        Long nextScrapId = null;
        ScrapPageResponse scrapPageResponse = ScrapPageResponse
                .builder()
                .nextScrapId(nextScrapId)
                .scrapResponses(scrapResponses)
                .build();
        if(scrapResponses.getContent().isEmpty()) {
            return ResponseEntity.ok(new BaseResponse(scrapPageResponse));
        }
        if(orderKeyword.equals("desc")) {
            nextScrapId = scrapResponses.getContent().get(scrapResponses.getContent().size() - 1).getId() - 1;
        } else if(orderKeyword.equals("asc")) {
            nextScrapId = scrapResponses.getContent().get(scrapResponses.getContent().size() - 1).getId() + 1;
        }
        scrapPageResponse.setNextScrapId(nextScrapId);

        return ResponseEntity.ok(new BaseResponse(scrapPageResponse));
    }

    @GetMapping("/type/{filter}")
    @ApiOperation(value = "스크랩 필터링(컨텐츠별)", notes = "정렬은 order_keyword(desc는 최근생성, asc 오래된생성순)")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<ScrapPageResponse>> filterScraps(@ApiParam(value = "filter : image, video, text, link, pdf")
                                                                           @PathVariable("filter") String filter,
                                                                           @RequestParam(name = "order_keyword") String orderKeyword,
                                                                           @PageableDefault(size = 10) Pageable pageable) {
        Slice<ScrapResponse> scrapResponses = scrapService.filterTypePageScraps(filter, orderKeyword, authService.getUserEmail(), pageable);
        Long nextScrapId = null;
        ScrapPageResponse scrapPageResponse = ScrapPageResponse
                .builder()
                .nextScrapId(nextScrapId)
                .scrapResponses(scrapResponses)
                .build();
        if(scrapResponses.getContent().isEmpty()) {
            return ResponseEntity.ok(new BaseResponse(scrapPageResponse));
        }
        if(orderKeyword.equals("desc")) {
            nextScrapId = scrapResponses.getContent().get(scrapResponses.getContent().size() - 1).getId() - 1;
        } else if(orderKeyword.equals("asc")) {
            nextScrapId = scrapResponses.getContent().get(scrapResponses.getContent().size() - 1).getId() + 1;
        }
        scrapPageResponse.setNextScrapId(nextScrapId);

        return ResponseEntity.ok(new BaseResponse(scrapPageResponse));
    }

    @PostMapping
    @ApiOperation(value = "스크랩 저장", notes = "로그인한 아이디에 스크랩 저장, ***scrapRequest의 scrapType은 IMAGE, VIDEO, PDF, TEXT, LINK***")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2006-필수값미입력(해시태그, 스크랩타입), " +
                    "4001-지원하지않는파일, 4007-제목글자수초과, 4006-카테고리미존재, 4008-파일미존재(IMAGE, VIDEO, PDF)" +
                    ", 4014-컨텐츠미입력(LINK, TEXT), 4016-파일컨텐츠타입불일치, 4017 & 4018-미리보기생성불가"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> insertScrap(@RequestPart(value = "scrap_request") ScrapRequest scrapRequest,
                                                    @RequestPart(value = "file", required = false) MultipartFile multipartFile) {
        scrapService.save(scrapRequest, multipartFile, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @PutMapping
    @ApiOperation(value = "스크랩 수정", notes = "스크랩 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2006-필수값미입력(해시태그), 4007-제목글자수초과, 4009-스크랩이존재하지않음"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateScrap(@RequestBody ScrapUpdateRequest scrapUpdateRequest) {
        scrapService.update(scrapUpdateRequest, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @DeleteMapping
    @ApiOperation(value = "스크랩 삭제", notes = "스크랩 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4009-스크랩이존재하지않음"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> deleteScrap(@RequestParam(name = "ids") List<String> ids) {
        for(String id : ids) {
            scrapService.delete(Long.parseLong(id), authService.getUserEmail());
        }

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
