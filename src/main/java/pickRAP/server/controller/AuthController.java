package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.config.security.jwt.TokenDto;
import pickRAP.server.controller.dto.auth.MemberSignInRequest;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.oauth.OauthService;

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
    private final OauthService oauthService;
    private static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$",Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD = Pattern.compile("^.*(?=^.{8,}$)(?=.*[a-zA-Z])(?=.*[!@#$%^&+=]).*$",Pattern.CASE_INSENSITIVE);

    /*
    회원가입
     */
    @PostMapping("/sign-up")
    @ApiOperation(value = "회원가입", notes = "이메일 형식 검사 + 비밀번호 형식(영문 + 특수문자 8자이상)")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2002-이메일형식예외, 2003-비밀번호형식예외, 2004-이미존재하는회원, 2006-미입력칸존재"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> signUp(@Validated @RequestBody MemberSignUpRequest memberSignUpRequest) {
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
    로그인
     */

    @PostMapping("/sign-in")
    @ApiOperation(value = "로그인", notes = "유저 로그인")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2005-로그인실패, 2006-미입력칸존재"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> signIn(@Validated @RequestBody MemberSignInRequest memberSignInRequest, HttpServletResponse response) {
        TokenDto tokenDto = authService.signIn(memberSignInRequest);
        setToken(response, tokenDto);
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }

    public void setToken(HttpServletResponse response, TokenDto tokenDto) {
        response.setHeader("Authorization", "Bearer "+tokenDto.getAccessToken());
        response.setHeader("Set-Cookie", setRefreshToken(tokenDto.getRefreshToken()).toString());
    }

    public ResponseCookie setRefreshToken(String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("RefreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .maxAge(60 * 60 * 24 * 7)  //7일
                .sameSite("None")
                .domain("pickrap.com")
                .path("/")
                .build();
        return cookie;
    }

    /*
    소셜로그인
     */
    @GetMapping("/{provider}")
    @ApiOperation(value = "소셜로그인", notes = "소셜 인증 후, 토큰 발급 - 네이버의 경우 parameter에 state값도 포함하여 요청!!")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "리소스 서버 예외")
    })
    public ResponseEntity<BaseResponse> socialSignIn(@PathVariable("provider")String provider
                                                    , @RequestParam("code") String code
                                                    , @RequestParam(value = "state", required = false) String state
                                                    , HttpServletResponse response ){
        TokenDto tokenDto = oauthService.socialAuth(provider, code, state);
        setToken(response, tokenDto);
        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }


    /*
    재발급
     */
    @PostMapping("/reissue")
    @ApiOperation(value = "재발급", notes = "토큰 재발급")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "2007-재발급실패"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> refresh(HttpServletRequest request
                                            , HttpServletResponse response
                                            , @CookieValue(name = "RefreshToken", required = false) String refreshToken) throws IOException {
        TokenDto tokenDto = authService.reissue(refreshToken, request);
        setToken(response, tokenDto);
        return ResponseEntity.ok(new BaseResponse<>(SUCCESS));
    }
}
