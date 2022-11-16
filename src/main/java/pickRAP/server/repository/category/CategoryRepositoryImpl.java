package pickRAP.server.repository.category;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.member.Member;

import java.util.List;
import java.util.Optional;

import static pickRAP.server.domain.category.QCategory.category;
import static pickRAP.server.domain.member.QMember.member;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Category> findMemberCategories(Member findMember) {
        return queryFactory
                .selectFrom(category)
                .where(category.member.eq(findMember))
                .orderBy(category.createTime.desc())
                .fetch();
    }

    @Override
    public Optional<Category> findMemberCategory(String categoryName, String email) {
        Category result =  queryFactory
                .selectFrom(category)
                .join(category.member, member)
                .where(member.email.eq(email))
                .where(category.name.eq(categoryName))
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
