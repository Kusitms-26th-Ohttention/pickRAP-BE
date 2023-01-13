package pickRAP.server.service.hashtag;

import com.querydsl.core.QueryResults;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pickRAP.server.common.BaseException;
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

        Member member = memberRepository.findByEmail(email).orElseThrow();

        HashtagFilterCondition hashtagFilterCond = HashtagFilterCondition.builder()
                .filter(filter)
                .year(year)
                .month(month)
                .build();

        List<HashTagResponse> hashTagResponses = hashtagRepository.getHashtagAnalysisResults(hashtagFilterCond, email);


        return AnalysisResponse.builder().hashtags(hashTagResponses).build();
    }


}
