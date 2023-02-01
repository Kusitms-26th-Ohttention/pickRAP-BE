package pickRAP.server.repository.scrap;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import pickRAP.server.controller.dto.analysis.RevisitResponse;
import pickRAP.server.controller.dto.scrap.ScrapFilterCondition;
import pickRAP.server.controller.dto.scrap.ScrapResponse;

import java.util.List;

public interface ScrapRepositoryCustom {

    Slice<ScrapResponse> filterPageScraps(Long lastScrapId, ScrapFilterCondition scrapFilterCondition, Pageable pageable);

    List<RevisitResponse> findByRevisitTimeAndRevisitCount(String email);
}
