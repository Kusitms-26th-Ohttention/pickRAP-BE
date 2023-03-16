package pickRAP.server.repository.hashtag;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import pickRAP.server.controller.dto.Hashtag.HashtagResponse;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;
import pickRAP.server.domain.member.Member;

import java.util.List;

public interface HashtagRepositoryCustom {

    List<HashTagResponse> getHashtagAnalysisResults(HashtagFilterCondition hashtagFilterCondition, String email);

    Slice<HashtagResponse> findSliceHashtagResponse(Member member, Pageable pageable);
}
