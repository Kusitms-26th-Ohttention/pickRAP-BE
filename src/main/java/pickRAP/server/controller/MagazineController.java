package pickRAP.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.magazine.MagazineService;

import static pickRAP.server.common.BaseExceptionStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
public class MagazineController {

    final static int MAX_PAGE_SIZE = 20;

    private final MagazineService magazineService;
    private final AuthService authService;

    @PostMapping("/magazine/{template}")
    public ResponseEntity<BaseResponse> saveMagazine(
            @PathVariable(name="template") String template,
            @RequestBody MagazineRequest magazineRequest) {

        if(magazineRequest.getPageList().size() > MAX_PAGE_SIZE) {
            throw new BaseException(BaseExceptionStatus.EXCEED_PAGE_SIZE);
        }

        // String email = authService.getUserEmail();
        String email = "luck732002@naver.com";
        magazineService.save(magazineRequest, email, template);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
