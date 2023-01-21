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

import java.io.IOException;

import static pickRAP.server.common.BaseExceptionStatus.SUCCESS;
import static pickRAP.server.util.s3.S3Util.uploadFile;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final AuthService authService;

    @GetMapping("/profile")
    @ApiOperation(value = "프로필 정보 가져오기", notes = "유저 프로필 정보 가져오기")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> getProfile() {
        ProfileResponse response = profileService.getProfile(authService.getUserEmail());
        return ResponseEntity.ok(new BaseResponse(response));
    }

    @PostMapping("/profile")
    @ApiOperation(value = "프로필 설정", notes = "유저 프로필 이름, 사진, 소개글, 키워드를 설정")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateProfile(@RequestPart("profile-request") ProfileRequest request,
                                                      @RequestPart("file") MultipartFile multipartFile
                                                     ) throws IOException {
        String profileImageUrl = "";
        if(!multipartFile.isEmpty()) {
            profileImageUrl = uploadFile(multipartFile, "content", "image");
        }

        profileService.updateProfile(authService.getUserEmail(), request, profileImageUrl);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
