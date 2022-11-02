package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.auth.MemberEmailRequest;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.auth.MemberVerifyCodeRequest;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.auth.VerifyCodeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

import static pickRAP.server.common.BaseExceptionStatus.*;

@Slf4j
@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final VerifyCodeService verifyCodeService;
    private static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$",Pattern.CASE_INSENSITIVE);
    /*
    회원가입
     */
    @PostMapping("/sign-up")
    @ApiOperation(value = "회원가입", notes = "이메일 형식 검사 + 비밀번호 형식(영문 + 숫자 8~16자)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2002-이메일형식예외, 2003-비밀번호형식예외, 2004-이미존재하는회원"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> signUp(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        if (!isRegexEmail(memberSignUpRequest.getEmail())) {
            throw new BaseException(INVALID_EMAIL);
        }
        if (!isRegexPassword(memberSignUpRequest.getPassword())) {
            throw new BaseException(INVALID_PASSWORD);
        }
        authService.signUp(memberSignUpRequest);
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }

    private boolean isRegexEmail(String email) {
        return EMAIL.matcher(email).find();
    }

    private boolean isRegexPassword(String password) {
        return PASSWORD.matcher(password).find();
    }

    /*
    이메일 인증
     */
    @PostMapping("/send-email")
    @ApiOperation(value = "이메일 인증", notes = "이메일 인증코드 보내기")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2002-이메일형식예외"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> sendEmail(@RequestBody MemberEmailRequest memberEmailRequest) {
        if (!isRegexEmail(memberEmailRequest.getEmail())) {
            throw new BaseException(INVALID_EMAIL);
        }
        verifyCodeService.createVerifyCode(memberEmailRequest.getEmail());
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }

    /*
    인증코드 검증
     */
    @PostMapping("/verify-email")
    @ApiOperation(value = "이메일 인증코드 검증", notes = "이메일 인증코드 검증")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2006-인증코드 검증실패"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> verifyEmail(@RequestBody MemberVerifyCodeRequest memberVerifyCodeRequest) {
        verifyCodeService.verifyCode(memberVerifyCodeRequest.getCode());
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }


    /*
    로그인
     */

    @PostMapping("/sign-in")
    @ApiOperation(value = "로그인", notes = "유저 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2005-로그인실패"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> signIn(@RequestBody MemberSignInRequest memberSignInRequest, HttpServletResponse response) {
        String accessToken = authService.signIn(memberSignInRequest);
        response.setHeader("Authorization", "Bearer "+accessToken);
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }

    /*
    재발급
     */
    @PostMapping("/reissue")
    @ApiOperation(value = "재발급", notes = "토큰 재발급")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "2000-토큰만료"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String newAccessToken = authService.reissue(request);
        response.setHeader("Authorization", "Bearer "+newAccessToken);
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }
}
