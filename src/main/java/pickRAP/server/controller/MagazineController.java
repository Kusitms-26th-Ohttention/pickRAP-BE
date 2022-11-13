package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.common.URLPreview;
import pickRAP.server.controller.dto.magazine.MagazineListResponse;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
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

    @PostMapping("/magazine/{template}")
    @ApiOperation(value = "매거진 제작하기", notes = "매거진을 생성하는 api")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "5001-매거진페이지수초과, 5002-매거진텍스트글자수초과"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> saveMagazine(
            @ApiParam(value = "template type : image, video, text, link, pdf")
            @PathVariable(name="template") String template,
            @RequestBody MagazineRequest magazineRequest) {

        if(magazineRequest.getPageList().size() > MAX_PAGE_SIZE) {
            throw new BaseException(BaseExceptionStatus.EXCEED_PAGE_SIZE);
        }

        String email = authService.getUserEmail();
        magazineService.save(magazineRequest, email, template);

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
}
