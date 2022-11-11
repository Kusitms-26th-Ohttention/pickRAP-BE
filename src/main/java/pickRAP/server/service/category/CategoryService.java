package pickRAP.server.service.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.category.QCategory;
import pickRAP.server.domain.member.QMember;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;

import static pickRAP.server.domain.category.QCategory.*;
import static pickRAP.server.domain.member.QMember.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final JPAQueryFactory queryFactory;

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    public List<Category> findMemberCategories(String email) {
        List<Category> memberCategories = queryFactory
                .selectFrom(category)
                .join(category.member, member)
                .where(member.email.eq(email))
                .fetch();

        return memberCategories;
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
