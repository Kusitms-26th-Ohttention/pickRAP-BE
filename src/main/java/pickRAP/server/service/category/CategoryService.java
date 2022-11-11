package pickRAP.server.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.category.CategoryResponse;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void initial(Member member) {
        Category category = Category.builder()
                .name("미분류 카테고리")
                .build();
        category.setMember(member);

        categoryRepository.save(category);
    }

    @Transactional
    public CategoryResponse save(CategoryRequest categoryRequest, String email) {
        if(categoryRepository.findMemberCategory(categoryRequest.getName(), email).isPresent()) {
            throw new BaseException(BaseExceptionStatus.EXIST_CATEGORY);
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .build();
        category.setMember(memberRepository.findByEmail(email).orElseThrow());
        categoryRepository.save(category);

        return new CategoryResponse(category.getId(), category.getName());
    }

    public List<CategoryResponse> findMemberCategories(String email) {
        Member findMember = memberRepository.findByEmail(email).orElseThrow();

        List<Category> result = categoryRepository.findMemberCategories(findMember);

        return result.stream().map(c -> new CategoryResponse(c.getId(), c.getName())).collect(Collectors.toList());
    }

    @Transactional
    public void update(CategoryRequest categoryRequest, Long id, String email) {
        Category findCategory = categoryRepository.findById(id).orElseThrow();

        if(findCategory.getName().equals(categoryRequest.getName())) {
            throw new BaseException(BaseExceptionStatus.SAME_CATEGORY);
        }
        if(categoryRepository.findMemberCategory(categoryRequest.getName(), email).isPresent()) {
            throw new BaseException(BaseExceptionStatus.EXIST_CATEGORY);
        }

        findCategory.updateName(categoryRequest.getName());
    }

    @Transactional
    public void delete(Long id) {
        if(categoryRepository.findById(id).isEmpty()) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY);
        }

        categoryRepository.deleteById(id);
    }
}
