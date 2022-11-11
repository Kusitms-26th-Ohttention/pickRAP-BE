package pickRAP.server.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.domain.category.Category;

import java.util.List;

import static pickRAP.server.domain.category.QCategory.category;
import static pickRAP.server.domain.member.QMember.member;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Category> findMemberCategories(String email) {
        return queryFactory
                .selectFrom(category)
                .join(category.member, member)
                .where(member.email.eq(email))
                .fetch();
    }
}
