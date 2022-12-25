package pickRAP.server.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.controller.dto.auth.MemberSignUpRequest;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.category.CategoryScrapResponse;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.service.auth.AuthService;
import pickRAP.server.service.category.CategoryService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pickRAP.server.auth.AuthEnv.EMAIL_OK;
import static pickRAP.server.auth.AuthEnv.PASSWORD_OK;
import static pickRAP.server.common.BaseExceptionStatus.*;

@SpringBootTest
@Transactional
public class CategoryServiceTest {

    @Autowired CategoryRepository categoryRepository;

    @Autowired CategoryService categoryService;

    @Autowired MemberRepository memberRepository;

    @Autowired AuthService authService;

    private MemberSignUpRequest memberSignUpRequest() {
        return MemberSignUpRequest.builder()
                .email(EMAIL_OK)
                .password(PASSWORD_OK)
                .name("테스트유저")
                .build();
    }

    private CategoryRequest categoryRequest(String name) {
        return CategoryRequest.builder()
                .name(name)
                .build();
    }

    @BeforeEach
    void before() {
        MemberSignUpRequest memberSignUpRequest = memberSignUpRequest();

        authService.signUp(memberSignUpRequest);
    }

    @Test
    @DisplayName("카테고리 저장 & 조회")
    void saveCategoryTest() {
        //given
        CategoryRequest categoryRequest = categoryRequest("여행");
        Member member = memberRepository.findByEmail(EMAIL_OK).get();

        //when
        categoryService.save(categoryRequest, member.getEmail());
        Category findCategory = categoryRepository.findMemberCategory(categoryRequest.getName(), member.getEmail()).get();

        //then
        assertThat(findCategory.getName()).isEqualTo(categoryRequest.getName());
        assertThat(findCategory.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("카테고리 수정")
    void updateCategoryTest() {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        CategoryRequest categoryRequest = categoryRequest("여행");
        categoryService.save(categoryRequest, member.getEmail());
        Category category = categoryRepository.findMemberCategory(categoryRequest.getName(), member.getEmail()).get();
        categoryRequest = categoryRequest("음식");

        //when
        categoryService.update(categoryRequest, category.getId(), member.getEmail());

        //then
        Category findCategory = categoryRepository.findById(category.getId()).get();
        assertThat(findCategory.getName()).isEqualTo(categoryRequest.getName());
        assertThat(findCategory.getMember()).isEqualTo(member);
    }

    @Test
    @DisplayName("카테고리 삭제")
    void deleteCategoryTest() {
        //given
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        CategoryRequest categoryRequest = categoryRequest("여행");
        categoryService.save(categoryRequest, member.getEmail());
        Category category = categoryRepository.findMemberCategory(categoryRequest.getName(), member.getEmail()).get();

        //when
        categoryService.delete(category.getId(), member.getEmail());

        //then
        Optional<Category> findCategory = categoryRepository.findById(category.getId());
        assertThat(findCategory).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("모든 카테고리 조회")
    void selectCategoryTest() {
        //given
        CategoryRequest categoryRequest = categoryRequest("여행");
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        categoryService.save(categoryRequest, member.getEmail());
        categoryRequest = categoryRequest("음식");
        categoryService.save(categoryRequest, member.getEmail());

        //when
        List<CategoryScrapResponse> categories = categoryService.findMemberCategoriesScrap(member.getEmail());

        //then
        assertThat(categories.size()).isEqualTo(3); //카테고리 미지정 포함
    }

    @Test
    @DisplayName("예외 - 빈 값 전달")
    void categoryExceptionTest1() {
        //given
        CategoryRequest categoryRequest = categoryRequest("");
        Member member = memberRepository.findByEmail(EMAIL_OK).get();

        //when
        BaseException e = assertThrows(BaseException.class,
                () -> categoryService.save(categoryRequest, member.getEmail()));

        //then
        assertThat(e.getStatus()).isEqualTo(EMPTY_INPUT_VALUE);
    }

    @Test
    @DisplayName("예외 - 제목 길이 초과")
    void categoryExceptionTest2() {
        //given
        CategoryRequest categoryRequest = categoryRequest("이건20글자가넘는제목이에요나는몇글자일까요?");
        Member member = memberRepository.findByEmail(EMAIL_OK).get();

        //when
        BaseException e = assertThrows(BaseException.class,
                () -> categoryService.save(categoryRequest, member.getEmail()));

        //then
        assertThat(e.getStatus()).isEqualTo(CATEGORY_TITLE_LONG);
    }

    @Test
    @DisplayName("예외 - 중복 카테고리")
    void categoryExceptionTest3() {
        //given
        CategoryRequest categoryRequest = categoryRequest("여행");
        Member member = memberRepository.findByEmail(EMAIL_OK).get();
        categoryService.save(categoryRequest, member.getEmail());

        //when
        BaseException e = assertThrows(BaseException.class,
                () -> categoryService.save(categoryRequest, member.getEmail()));

        //then
        assertThat(e.getStatus()).isEqualTo(EXIST_CATEGORY);
    }
}
