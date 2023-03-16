package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.profile.ProfileRequest;
import pickRAP.server.controller.dto.profile.ProfileResponse;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.profile.ProfileService;

import static pickRAP.server.common.BaseExceptionStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("profile")
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    @GetMapping
    @ApiOperation(value = "프로필 정보 가져오기", notes = "유저 프로필 정보 가져오기")
    @ApiResponse(responseCode = "500", description = "서버 예외")
    public ResponseEntity<BaseResponse<ProfileResponse>> getProfile() {
        ProfileResponse response = profileService.getProfile(authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(response));
    }

    @PutMapping
    @ApiOperation(value = "프로필 설정", notes = "유저 프로필 이름, 사진, 소개글, 해시태그 설정")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "2006 - 필수값 미입력(닉네임, 소개글)<br>" +
                    "3001 - 소개글 길이 초과<br>3002 - 닉네임 중복<br>3003 - 해시태그 4개 초과"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateProfile(@RequestPart("profile_request") ProfileRequest profileRequest,
                                                      @RequestPart("file") MultipartFile multipartFile) {
        profileService.updateProfile(authService.getUserEmail(), profileRequest, multipartFile);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
