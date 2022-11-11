package pickRAP.server.repository.category;

import pickRAP.server.domain.category.Category;

import java.util.List;

public interface CategoryRepositoryCustom {

    List<Category> findMemberCategories(String email);
}
