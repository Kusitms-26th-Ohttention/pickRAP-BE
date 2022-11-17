package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.controller.dto.category.*;
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
    @ApiOperation(value = "카테고리 불러오기", notes = "로그인한 아이디의 모든 카테고리와 스크랩 정보 1개 불러오기")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<List<CategoryScrapResponse>>> selectCategory() {
        List<CategoryScrapResponse> categoryScrapResponses = categoryService.findMemberCategoriesScrap(authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(categoryScrapResponses));
    }

    @PostMapping
    @ApiOperation(value = "카테고리 저장", notes = "로그인한 아이디에 카테고리 저장")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4004-중복카테고리, 4011-제목길이초과"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<CategoryResponse>> insertCategory(@RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.save(categoryRequest, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(categoryResponse));
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "카테고리 수정", notes = "카테고리 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4004-중복카테고리, 4005-현재카테고리와이름동일, 4011-제목길이초과"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> updateCategory(@RequestBody CategoryRequest categoryRequest, @PathVariable("id") Long id) {
        categoryService.update(categoryRequest, id, authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @DeleteMapping
    @ApiOperation(value = "카테고리 삭제", notes = "카테고리 삭제, 속해있던 스크랩은 미분류 카테고리로 이동")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4006-카테고리가존재하지않음, 4010-기본카테고리삭제불가"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> deleteCategory(@RequestBody CategoryDeleteRequest categoryDeleteRequest) {
        for(Long id : categoryDeleteRequest.getId()) {
            categoryService.delete(id, authService.getUserEmail());
        }

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @GetMapping("/contents")
    @ApiOperation(value = "카테고리 내 콘텐츠 불러오기", notes = "로그인한 아이디의 모든 카테고리에 해당하는 스크랩 정보 불러오기")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse<List<CategoryContentsResponse>>> getCategoryContents() {
        List<CategoryContentsResponse> categoryContentResponses = categoryService.findMemberCategoriesAllScrap(authService.getUserEmail());

        return ResponseEntity.ok(new BaseResponse(categoryContentResponses));
    }
}
