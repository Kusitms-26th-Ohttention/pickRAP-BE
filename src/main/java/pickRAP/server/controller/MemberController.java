package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.service.auth.AuthService;

import static pickRAP.server.common.BaseExceptionStatus.*;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;

    /*
    로그아웃
     */
    @PostMapping("/log-out")
    @ApiOperation(value = "로그아웃", notes = "redis 토큰 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> logout() {
        authService.logout();
        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

}