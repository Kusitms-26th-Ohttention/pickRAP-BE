package pickRAP.server.repository.scrap;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.scrap.ScrapHashtag;

import java.util.List;

public interface ScrapHashtagRepository extends JpaRepository<ScrapHashtag, Long> {

    @EntityGraph(attributePaths = {"hashtag"})
    List<ScrapHashtag> findByScrapId(Long id);
}
