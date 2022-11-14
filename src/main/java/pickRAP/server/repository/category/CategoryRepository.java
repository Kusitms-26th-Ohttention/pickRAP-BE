package pickRAP.server.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.category.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {
}
