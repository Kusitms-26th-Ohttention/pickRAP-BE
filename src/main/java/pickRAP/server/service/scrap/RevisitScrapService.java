package pickRAP.server.service.scrap;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.analysis.RevisitResponse;
import pickRAP.server.repository.scrap.ScrapRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevisitScrapService {
    private final ScrapRepository scrapRepository;

    @Transactional
    public List<RevisitResponse> getRevisitContents(String email, String filter) {
        List<RevisitResponse> revisitScrapList = scrapRepository.findByRevisitTimeAndRevisitCount(email);

        if(filter.equals("top")) {
            List<RevisitResponse> top3Contents = new ArrayList<>(3);

            for(int i = 0; i < 3; i++) {
                top3Contents.add(revisitScrapList.get(i));
            }
            return top3Contents;
        }

        return revisitScrapList;
    }
}
