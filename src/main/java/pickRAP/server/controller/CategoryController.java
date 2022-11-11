package pickRAP.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;

import static pickRAP.server.common.BaseExceptionStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    private final AuthService authService;

//    @GetMapping("/category")
//    public ResponseEntity<BaseResponse>

    @PostMapping("/category")
    public ResponseEntity<BaseResponse> saveCategory(@RequestBody CategoryRequest categoryRequest) {
        categoryService.save(categoryRequest, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
