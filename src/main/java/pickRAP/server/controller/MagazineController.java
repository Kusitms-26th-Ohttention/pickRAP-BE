package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.magazine.*;
import pickRAP.server.domain.magazine.ColorType;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.magazine.MagazineService;

import java.util.List;

import static pickRAP.server.common.BaseExceptionStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
public class MagazineController {

    final static int MAX_PAGE_SIZE = 20;

    private final MagazineService magazineService;
    private final AuthService authService;

    @PostMapping("/magazine")
    @ApiOperation(value = "매거진 제작하기", notes = "매거진을 생성하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4009-존재하지않는스크랩"),
            @ApiResponse(responseCode = "500", description = "5001-매거진페이지수초과, 5002-매거진텍스트글자수초과," +
                    " 5005-매거진제목글자수초과, 5006-매거진커버타입에러"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> saveMagazine(@RequestBody MagazineRequest request) {
        if(request.getPageList().size() > MAX_PAGE_SIZE) {
            throw new BaseException(BaseExceptionStatus.EXCEED_PAGE_SIZE);
        }

        String email = authService.getUserEmail();
        magazineService.save(request, email);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @GetMapping("/magazine")
    @ApiOperation(value = "내 매거진 목록 불러오기", notes = "사용자의 매거진 목록을 출력하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<List<MagazineListResponse>>> getMagazineList() {
        String email = authService.getUserEmail();

        List<MagazineListResponse> response = magazineService.findMagazines(email);

        return ResponseEntity.ok(new BaseResponse(response));
    }

    @GetMapping("/magazine/{magazine_id}")
    @ApiOperation(value = "매거진 상세 내용 보기", notes = "클릭한 매거진의 상세 내용을 출력하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<MagazineResponse>> getMagazine(@PathVariable(name="magazine_id") Long magazineId) {
        MagazineResponse response = magazineService.findMagazine(magazineId);

        return ResponseEntity.ok(new BaseResponse(response));
    }

    @PutMapping("/magazine/{magazine_id}")
    @ApiOperation(value = "매거진 내용 수정하기", notes = "매거진 내용을 수정하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4009-존재하지않는스크랩"),
            @ApiResponse(responseCode = "500", description = "5001-매거진페이지수초과, 5002-매거진텍스트글자수초과," +
                    " 5005-매거진제목글자수초과, 5006-매거진커버타입에러"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateMagazine(
            @PathVariable(name="magazine_id") Long magazineId,
            @RequestBody MagazineRequest request) {

        if(request.getPageList().size() > MAX_PAGE_SIZE) {
            throw new BaseException(BaseExceptionStatus.EXCEED_PAGE_SIZE);
        }
        String email = authService.getUserEmail();

        magazineService.updateMagazine(request, magazineId, email);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @DeleteMapping("/magazine")
    @ApiOperation(value = "매거진 삭제하기", notes = "매거진을 삭제하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "5003-작성자불일치, 5004-선택된항목없음"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> deleteMagazine(@RequestParam List<String> ids) {
        if(ids.size() == 0) {
            throw new BaseException(BaseExceptionStatus.NOT_SELECTED_ELEMENT);
        }
        String email = authService.getUserEmail();

        ids.forEach(id->
                magazineService.deleteMagazine(Long.parseLong(id), email));

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @PostMapping("/magazine/{magazine_id}/color")
    @ApiOperation(value = "색반응하기", notes = "색반응 API<br>" +
                                            "- 최초 반응 : 색반응 추가<br>" +
                                            "- 마지막 반응과 다른색의 반응 : 반응 대체<br>" +
                                            "- 마지막 반응과 같은색의 반응 : 반응 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> addMagazineColor(@PathVariable("magazine_id") Long magazineId
                                                 , @RequestBody MagazineColorRequest magazineColorRequest) {
        String email = authService.getUserEmail();
        magazineService.addMagazineColor(email, magazineId, magazineColorRequest);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }


    @GetMapping("/magazine/{magazine_id}/color")
    @ApiOperation(value = "색 반응 조회", notes = "매거진에서 색반응+ 버튼 클릭 시 API<br>" +
                                                "- 마지막으로 반응한 색상을 리턴<br>" +
                                                "- 없으면 null 값 리턴")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "7001-자신의 매거진에 반응하려는 경우"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<MagazineColorResponse>> getMagazineColor(@PathVariable("magazine_id") Long magazineId){
        String email = authService.getUserEmail();
        MagazineColorResponse magazineColorResponse = magazineService.getMagazineColor(email, magazineId);

        return ResponseEntity.ok(new BaseResponse(magazineColorResponse));
    }


    @DeleteMapping("/magazine/page")
    @ApiOperation(value = "매거진 페이지 삭제하기", notes = "매거진의 페이지를 삭제하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "5004-선택된항목없음"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> deleteMagazinePage(@RequestParam List<String> ids) {
        if(ids.size() == 0) {
            throw new BaseException(BaseExceptionStatus.NOT_SELECTED_ELEMENT);
        }
        ids.forEach(id->
                magazineService.deletePage(Long.parseLong(id), authService.getUserEmail()));

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @GetMapping("/magazine/check-exist-title/{title}")
    @ApiOperation(value = "매거진 제목 중복 확인", notes = "매거진의 제목 중복 여부를 확인하는 api, true - 이미 존재함/false - 존재하지 않음")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> checkTitle(@PathVariable("title") String title) {
        String email = authService.getUserEmail();
        boolean result = magazineService.isExistMagazineTitle(title, email);

        return ResponseEntity.ok(new BaseResponse(result));
    }

    @GetMapping("/magazine/search")
    @ApiOperation(value = "매거진 검색", notes = "query string에 search_keyword(검색어)")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<List<MagazineListResponse>>> getSearchMagazineList(
            @RequestParam("search_keyword") String keyword) {
        String email = authService.getUserEmail();

        List<MagazineListResponse> response = magazineService.findMagazineByHashtag(email, keyword);

        return ResponseEntity.ok(new BaseResponse(response));
    }

}
