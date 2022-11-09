package pickRAP.server.service.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.profile.ProfileRequest;
import pickRAP.server.controller.dto.profile.ProfileResponse;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.member.MemberRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MemberRepository memberRepository;

    @Transactional
    public ProfileResponse getProfile (String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        String[] keywords = member.getKeyword().split("#");

        return new ProfileResponse(member.getName(), member.getIntroduction(), member.getProfileImageUrl(), keywords);
    }

    @Transactional
    public void updateProfile(String email, ProfileRequest request, String profileImageUrl) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        String keyword = "";
        for(String k : request.getKeywords()) {
            keyword += k + "#";
        }
        member.updateProfile(request.getName(), request.getIntroduction(), profileImageUrl, keyword);
    }

}
