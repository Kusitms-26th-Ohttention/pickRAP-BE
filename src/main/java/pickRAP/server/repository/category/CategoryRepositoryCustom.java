package pickRAP.server.repository.category;

import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.member.Member;

import java.util.List;
import java.util.Optional;

public interface CategoryRepositoryCustom {

    List<Category> findMemberCategories(Member findMember);

    Optional<Category> findMemberCategory(String categoryName, String email);
}
