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

import java.io.IOException;
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
        ScrapResponse scrapResponse = scrapService.findOne(id);

        return ResponseEntity.ok(new BaseResponse(scrapResponse));
    }

    @GetMapping
    @ApiOperation(value = "스크랩 검색", notes = "query string에 search_keyword(검색어), order_keyword(desc는 최근생성, asc 오래된생성순)")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<Slice<ScrapResponse>>> searchScraps(@RequestParam(name = "search_keyword") String searchKeyword,
                                                                           @RequestParam(name = "order_keyword") String orderKeyword,
                                                                           @PageableDefault(size = 10) Pageable pageable) {
        Slice<ScrapResponse> scrapResponses = scrapService.filterPageScraps("keyword", null, searchKeyword, orderKeyword, authService.getUserEmail(), pageable);

        return ResponseEntity.ok(new BaseResponse(scrapResponses));
    }

    @GetMapping("/type/{filter}")
    @ApiOperation(value = "스크랩 필터링(컨텐츠별)", notes = "정렬은 order_keyword(desc는 최근생성, asc 오래된생성순)")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<Slice<ScrapResponse>>> filterScraps(@ApiParam(value = "filter : image, video, text, link, pdf")
                                                                           @PathVariable("filter") String filter,
                                                                           @RequestParam(name = "order_keyword") String orderKeyword,
                                                                           @PageableDefault(size = 10) Pageable pageable) {
        Slice<ScrapResponse> scrapResponses = scrapService.filterPageScraps(filter, null, null, orderKeyword, authService.getUserEmail(), pageable);

        return ResponseEntity.ok(new BaseResponse(scrapResponses));
    }

//    @GetMapping("/{filter}")
//    @ApiOperation(value = "스크랩 필터링 & 검색", notes = "(전체, 카테고리, 컨텐츠, 검색별로 스크랩 필터링) body는 category면 카테고리 category_id, " +
//            "keyword는 search_keyword, 정렬은 order_keyword(desc는 최근생성, asc 오래된생성순)")
//    @ApiResponses({
//            @ApiResponse(responseCode = "500", description = "서버 예외")
//    })
//    public ResponseEntity<BaseResponse<Slice<ScrapResponse>>> selectScraps(@ApiParam(value = "filter : image, video, text, link, pdf, category, all, keyword")
//                                                                           @PathVariable("filter") String filter,
//                                                                           @RequestBody(required = false) ScrapFilterRequest scrapFilterRequest,
//                                                                           @PageableDefault(size = 10) Pageable pageable) {
//        Slice<ScrapResponse> scrapResponses = scrapService.filterPageScraps(filter, scrapFilterRequest, authService.getUserEmail(), pageable);
//
//        return ResponseEntity.ok(new BaseResponse(scrapResponses));
//    }

    @PostMapping
    @ApiOperation(value = "스크랩 저장", notes = "로그인한 아이디에 스크랩 저장, ***scrapRequest의 scrapType은 IMAGE, VIDEO, PDF, TEXT, LINK***")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2006-필수값미입력(해시태그, 스크랩타입), " +
                    "4001-지원하지않는파일, 4007-제목글자수초과, 4006-카테고리미존재, 4008-파일미존재(IMAGE, VIDEO, PDF), 4014-컨텐츠미입력(LINK, TEXT)"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> insertScrap(@RequestPart(value = "scrap-request") ScrapRequest scrapRequest,
                                                    @RequestPart(value = "file", required = false) MultipartFile multipartFile)
            throws IOException {
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
            scrapService.delete(Long.parseLong(id));
        }

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
