package pickRAP.server.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.domain.category.Category;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.category.CategoryRepositoryImpl;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryRepositoryImpl categoryRepositoryImpl;

    private final MemberRepository memberRepository;

    public List<Category> findMemberCategories(String email) {
        return categoryRepositoryImpl.findMemberCategories(email);
    }

    @Transactional
    public void save(CategoryRequest categoryRequest, String email) {
        List<Category> memberCategories = findMemberCategories(email);

        for(Category obj : memberCategories) {
            if(obj.getName().equals(categoryRequest.getName())) {
                throw new BaseException(BaseExceptionStatus.EXIST_CATEGORY);
            }
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .build();
        category.setMember(memberRepository.findByEmail(email).orElseThrow());
        categoryRepository.save(category);
    }
}
