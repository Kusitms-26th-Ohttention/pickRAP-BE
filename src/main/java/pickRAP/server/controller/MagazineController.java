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
            @ApiResponse(responseCode = "500", description = "5001-매거진페이지수초과, 5002-매거진텍스트글자수초과"),
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
    public ResponseEntity<BaseResponse> getMagazineList() {
        String email = authService.getUserEmail();

        List<MagazineListResponse> response = magazineService.findMagazines(email);

        return ResponseEntity.ok(new BaseResponse(response));
    }

    @GetMapping("/magazine/{magazine_id}")
    @ApiOperation(value = "매거진 상세 내용 보기", notes = "클릭한 매거진의 상세 내용을 출력하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> getMagazine(@PathVariable(name="magazine_id") Long magazineId) {
        MagazineResponse response = magazineService.findMagazine(magazineId);

        return ResponseEntity.ok(new BaseResponse(response));
    }

    @PutMapping("/magazine/{magazine_id}")
    @ApiOperation(value = "매거진 내용 수정하기", notes = "매거진 내용을 수정하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "5001-매거진페이지수초과, 5002-매거진텍스트글자수초과"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateMagazine(
            @PathVariable(name="magazine_id") Long magazineId,
            @RequestBody MagazineUpdateRequest request) {

        if(request.getPageList().size() > MAX_PAGE_SIZE) {
            throw new BaseException(BaseExceptionStatus.EXCEED_PAGE_SIZE);
        }
        String email = authService.getUserEmail();

        magazineService.updateMagazine(request, magazineId, email);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @PutMapping("/magazine/{magazine_id}/open-status")
    @ApiOperation(value = "매거진 공개여부 수정하기", notes = "매거진의 공개 여부를 수정하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "5003-작성자불일치"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
            })
    public ResponseEntity<BaseResponse> updateOpenStatus(@PathVariable(name="magazine_id") Long magazineId) {
        String email = authService.getUserEmail();

        magazineService.updateOpenStatus(magazineId, email);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @DeleteMapping("/magazine")
    @ApiOperation(value = "매거진 삭제하기", notes = "매거진을 삭제하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "5003-작성자불일치, 5004-선택된매거지없음"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> deleteMagazine(@RequestBody MagazineDeleteRequest request) {
        if(request.getMagazines().size() == 0) {
            throw new BaseException(BaseExceptionStatus.FAIL_DELETE_MAGAZINE);
        }

        String email = authService.getUserEmail();

        magazineService.deleteMagazines(request.getMagazines(), email);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
