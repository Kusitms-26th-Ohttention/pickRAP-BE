package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.category.CategoryResponse;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;

import java.util.List;

import static pickRAP.server.common.BaseExceptionStatus.SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/category")
public class CategoryController {

    private final CategoryService categoryService;

    private final AuthService authService;

    @GetMapping
    @ApiOperation(value = "카테고리 불러오기", notes = "로그인한 아이디의 모든 카테고리 불러오기")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<List<CategoryResponse>>> selectCategory() {
        List<CategoryResponse> categoryResponse = categoryService.findMemberCategories(authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(categoryResponse));
    }

    @PostMapping
    @ApiOperation(value = "카테고리 저장", notes = "로그인한 아이디에 카테고리 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4004-중복카테고리"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<CategoryResponse>> insertCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.save(categoryRequest, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(categoryResponse));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "카테고리 수정", notes = "카테고리 수정(기능 명세서에는 없지만 api만 구축했습니다")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4004-중복카테고리, 4005-현재카테고리와이름동일"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateCategory(@RequestBody CategoryRequest categoryRequest, @PathVariable("id") Long id) {
        categoryService.update(categoryRequest, id, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "카테고리 삭제", notes = "카테고리 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> deleteCategory(@PathVariable("id") Long id) {
        categoryService.delete(id);

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }
}
