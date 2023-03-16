package pickRAP.server.service.hashtag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import pickRAP.server.controller.dto.Hashtag.HashtagPageResponse;
import pickRAP.server.controller.dto.Hashtag.HashtagResponse;
import pickRAP.server.controller.dto.analysis.AnalysisResponse;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.hashtag.HashtagRepository;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HashtagService {
    
    private final HashtagRepository hashtagRepository;
    private final MemberRepository memberRepository;

    public AnalysisResponse getHashtagAnalysisResults(String filter, Integer year, Integer month, String email) {

        HashtagFilterCondition hashtagFilterCond = HashtagFilterCondition.builder()
                .filter(filter)
                .year(year)
                .month(month)
                .build();

        List<HashTagResponse> hashTagResponses = hashtagRepository.getHashtagAnalysisResults(hashtagFilterCond, email);


        return AnalysisResponse.builder().hashtags(hashTagResponses).build();
    }

    //사용한 해시태그 불러오기 (프로필에 사용된 해시태그 우선, 페이징 사용)
    public HashtagPageResponse getSliceHashtagPageResponse(String email, Pageable pageable) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        Slice<HashtagResponse> sliceHashtagResponses = hashtagRepository.findSliceHashtagResponse(member, pageable);

        return createHashtagPageResponse(sliceHashtagResponses, sliceHashtagResponses.hasNext(), pageable);
    }

    private HashtagPageResponse createHashtagPageResponse(Slice<HashtagResponse> sliceHashtagResponses, boolean hasNext, Pageable pageable) {
        Long nextHashtagId = null;
        if (hasNext) {
            nextHashtagId = Long.valueOf(sliceHashtagResponses.getContent().size()) + pageable.getPageNumber();
        }

        return HashtagPageResponse.builder()
                .nextHashtagId(nextHashtagId)
                .hashtagResponses(sliceHashtagResponses.getContent())
                .build();
    }
}
