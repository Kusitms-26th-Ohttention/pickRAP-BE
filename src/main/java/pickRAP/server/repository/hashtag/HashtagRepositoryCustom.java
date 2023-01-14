package pickRAP.server.repository.hashtag;

import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;

import java.util.List;

public interface HashtagRepositoryCustom {

    List<HashTagResponse> getHashtagAnalysisResults(HashtagFilterCondition hashtagFilterCondition, String email);
}
