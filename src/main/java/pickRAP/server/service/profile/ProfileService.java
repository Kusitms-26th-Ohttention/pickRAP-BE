package pickRAP.server.service.profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseException;
import pickRAP.server.controller.dto.Hashtag.HashtagResponse;
import pickRAP.server.controller.dto.profile.ProfileRequest;
import pickRAP.server.controller.dto.profile.ProfileResponse;
import pickRAP.server.domain.hashtag.Hashtag;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.hashtag.HashtagRepository;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pickRAP.server.common.BaseExceptionStatus.*;
import static pickRAP.server.util.s3.S3Util.uploadFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MemberRepository memberRepository;

    private final HashtagRepository hashtagRepository;

    //프로필 정보 가져오기
    @Transactional
    public ProfileResponse getProfile (String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        List<String> useHashtags = hashtagRepository.findTagDistinct(member, true);

        List<HashtagResponse> hashtagResponses = useHashtags.stream().map(
            h -> HashtagResponse.builder().tag(h).use(true).build()
        ).collect(Collectors.toList());

        return ProfileResponse.builder()
                .nickname(member.getNickname())
                .introduction(member.getIntroduction())
                .profileImageUrl(member.getProfileImageUrl())
                .hashtags(hashtagResponses)
                .build();
    }

    //프로필 정보 수정하기
    @Transactional
    public void updateProfile(String email, ProfileRequest profileRequest, MultipartFile multipartFile) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        validateIntroduction(profileRequest.getIntroduction());
        validateNickname(member, profileRequest.getNickname());
        validateHashtags(profileRequest.getHashtags());

        String profileImageUrl = member.getProfileImageUrl();
        //MultipartFile 테스트를 위해 이중으로 처리
        if(!Objects.isNull(multipartFile)) {
            if (!multipartFile.isEmpty()) {
                profileImageUrl = uploadFile(multipartFile, "profile", "image");
            }
        }

        member.updateProfile(profileRequest.getNickname(), profileRequest.getIntroduction(), profileImageUrl);
        updateProfileHashtag(member, profileRequest);
    }

    private void validateIntroduction(String introduction) {
        if (!StringUtils.hasText(introduction)) {
            throw new BaseException(EMPTY_INPUT_VALUE);
        }
        if (introduction.length() > 25) {
            throw new BaseException(INTRODUCTION_LONG);
        }
    }

    private void validateNickname(Member member, String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new BaseException(EMPTY_INPUT_VALUE);
        }
        if (memberRepository.getNicknameCount(member, nickname) != 0) {
            throw new BaseException(DUPLICATE_NICKNAME);
        }
    }

    private void validateHashtags(List<String> hashtags) {
        if (hashtags.size() > 4) {
            throw new BaseException(EXCEED_HASHTAG);
        }
    }

    private void updateProfileHashtag(Member member, ProfileRequest profileRequest) {
        List<Hashtag> useHashtags = hashtagRepository.findHashtagUseProfile(member);
        useHashtags.forEach(h -> h.updateProfile(false));

        profileRequest.getHashtags().forEach(
            tag -> {
                List<Hashtag> hashtags = hashtagRepository.findByMemberAndTag(member, tag);
                hashtags.forEach(h ->h.updateProfile(true));
            }
        );
    }
}
