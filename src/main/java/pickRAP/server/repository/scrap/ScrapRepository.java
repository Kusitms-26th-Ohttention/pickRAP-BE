package pickRAP.server.repository.scrap;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.scrap.Scrap;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long>, ScrapRepositoryCustom {

    List<Scrap> findByCategoryId(Long id);

    @EntityGraph(attributePaths = {"category"})
    Optional<Scrap> findById(Long id);
}
