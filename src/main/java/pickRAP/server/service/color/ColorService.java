package pickRAP.server.service.color;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pickRAP.server.controller.dto.analysis.PersonalMoodResponse;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.color.ColorRepository;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;
    private final MemberRepository memberRepository;

    public List<PersonalMoodResponse> getPersonalMoodAnalysisResults(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        return colorRepository.getPersonalMoodAnalysisResults(member);
    }
}
