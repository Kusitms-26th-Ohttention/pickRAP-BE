package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;
import pickRAP.server.controller.dto.analysis.PersonalMoodResponse;
import pickRAP.server.controller.dto.magazine.*;
import pickRAP.server.domain.hashtag.Hashtag;
import pickRAP.server.domain.magazine.Color;
import pickRAP.server.domain.magazine.ColorType;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapHashtag;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.color.ColorRepository;
import pickRAP.server.repository.hashtag.HashtagRepository;
import pickRAP.server.repository.magazine.MagazinePageRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.text.TextService;
import pickRAP.server.util.deduplication.DeduplicationUtils;

import java.util.*;
import java.util.stream.Collectors;

import static pickRAP.server.common.BaseExceptionStatus.*;

@Service
@RequiredArgsConstructor
public class MagazineService {

    private final int MAX_TEXT_LENGTH = 200;
    private final int MAX_TITLE_LENGTH = 15;
    private final int RECOMMENDED_TOTAL_SIZE = 20;

    private final MemberRepository memberRepository;
    private final MagazineRepository magazineRepository;
    private final MagazinePageRepository magazinePageRepository;
    private final ScrapRepository scrapRepository;
    private final TextService textService;
    private final ColorRepository colorRepository;
    private final HashtagRepository hashtagRepository;
    private final ScrapHashtagRepository scrapHashtagRepository;

    @Transactional
    public void save(MagazineRequest request, String email) {
        if(request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new BaseException(EXCEED_TITLE_LENGTH);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();

        Optional<Scrap> cover = scrapRepository.findById(request.getCoverScrapId());
        if(!cover.isPresent()) {
            throw new BaseException(DONT_EXIST_SCRAP);
        }
        if(cover.get().getScrapType() != ScrapType.IMAGE) {
            throw new BaseException(DONT_MATCH_TYPE);
        }

        Magazine magazine = Magazine.builder()
                .title(request.getTitle())
                .openStatus(request.isOpenStatus())
                .member(member)
                .cover(cover.get().getFileUrl())
                .build();

        saveMagazinePages(request.getPageList(), magazine, member);

        return;
    }

    @Transactional(readOnly = true)
    public List<MagazineListResponse> findMagazines(String email) {
        List<Magazine> findMagazines = magazineRepository.findMemberMagazines(email);

        List<MagazineListResponse> collect = findMagazines.stream()
                .map(m -> MagazineListResponse.builder()
                        .magazineId(m.getId())
                        .coverUrl(m.getCover())
                        .title(m.getTitle())
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    @Transactional(readOnly = true)
    public MagazineResponse findMagazine(Long magazineId) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        List<MagazinePage> findMagazinePages = findMagazine.getPages();
        List<MagazinePageResponse> magazinePages = new ArrayList<>();

        for(MagazinePage p : findMagazinePages) {
            if(p.getScrap().getScrapType() == ScrapType.IMAGE
                    || p.getScrap().getScrapType() == ScrapType.VIDEO
                    || p.getScrap().getScrapType() == ScrapType.PDF) {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .fileUrl(p.getScrap().getFileUrl())
                        .previewUrl(p.getScrap().getPreviewUrl())
                        .text(p.getText()).build());
            } else if (p.getScrap().getScrapType() == ScrapType.LINK) {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .previewUrl(p.getScrap().getPreviewUrl())
                        .contents(p.getScrap().getContent())
                        .text(p.getText()).build());
            } else {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .contents(p.getScrap().getContent())
                        .text(p.getText()).build());
            }
        }

        List<ColorType> magazineColors = colorRepository.getMagazineColors(findMagazine);

        MagazineResponse magazine = MagazineResponse.builder()
                .magazineId(findMagazine.getId())
                .title(findMagazine.getTitle())
                .openStatus(findMagazine.isOpenStatus())
                .createdDate(findMagazine.getCreateTime())
                .pageList(magazinePages)
                .colors(magazineColors.stream().map(c -> c.getValue()).collect(Collectors.toList()))
                .build();

        return magazine;
    }

    @Transactional
    public void updateMagazine(MagazineRequest request, Long magazineId, String email) {
        if(request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new BaseException(EXCEED_TITLE_LENGTH);
        }
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        Member member = memberRepository.findByEmail(email).orElseThrow();

        checkMatchWriter(findMagazine, email);

        Optional<Scrap> cover = scrapRepository.findById(request.getCoverScrapId());
        if(!cover.isPresent()) {
            throw new BaseException(DONT_EXIST_SCRAP);
        }
        if(cover.get().getScrapType() != ScrapType.IMAGE) {
            throw new BaseException(DONT_MATCH_TYPE);
        }

        findMagazine.updateMagazine(request.getTitle(), request.isOpenStatus(), cover.get().getFileUrl());

        deleteTextByMagazine(findMagazine, email);

        magazinePageRepository.deleteByMagazineId(findMagazine.getId());

        saveMagazinePages(request.getPageList(), findMagazine, member);

        return;
    }

    @Transactional
    public void saveMagazinePages(List<MagazinePageRequest> requestList, Magazine magazine, Member member) {
        requestList.forEach(p -> {
            if (p.getText().length() > MAX_TEXT_LENGTH) {
                throw new BaseException(EXCEED_TEXT_LENGTH);
            }

            Optional<Scrap> scrap = scrapRepository.findById(p.getScrapId());
            if(!scrap.isPresent()) {
                throw new BaseException(DONT_EXIST_SCRAP);
            }

            MagazinePage page = MagazinePage.builder()
                    .scrap(scrap.get())
                    .text(p.getText())
                    .magazine(magazine)
                    .build();

            textService.save(member, page.getText());

            magazinePageRepository.save(page);
        });

    }

    public void checkMatchWriter(Magazine magazine, String email) {
        if(!magazine.checkWriter(email)) {
            new BaseException(NOT_MATCH_WRITER);
        }
    }

    @Transactional
    public void deleteMagazine(Long magazineId, String email) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        checkMatchWriter(findMagazine, email);

        deleteTextByMagazine(findMagazine, email);

        magazineRepository.delete(findMagazine);
    }

    public void deleteTextByMagazine(Magazine magazine, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        List<String> texts = magazinePageRepository.findTextByMagazine(magazine);

        texts.forEach(t -> textService.delete(member, t));
    }

    @Transactional
    public void deletePage(Long pageId, String email) {
        deleteTextByPage(pageId, email);

        magazinePageRepository.deleteById(pageId);
    }

    public void deleteTextByPage(Long pageId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String text = magazinePageRepository.findTextById(pageId);

        textService.delete(member, text);
    }

    @Transactional(readOnly = true)
    public boolean isExistMagazineTitle(String title, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        Optional<Magazine> findMagazine = magazineRepository.findByTitleAndMember(title, member);
        if(findMagazine.isPresent()) {
            return true;
        }
        return false;
    }

    public MagazineColorResponse getMagazineColor(String email, Long magazineId) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Magazine magazine = magazineRepository.findById(magazineId).orElseThrow();

        if (magazine.getMember() == member) {
            throw new BaseException(CANT_COLOR_REACTION);
        }

        Optional<Color> findColor = colorRepository.findByMemberAndMagazine(member, magazine);

        if (findColor.isEmpty()) {
            return MagazineColorResponse.builder().colorType(null).build();
        }

        Color color = findColor.get();
        return MagazineColorResponse.builder().colorType(color.getColorType().getValue()).build();
    }

    @Transactional
    public void addMagazineColor(String email, Long magazineId, MagazineColorRequest magazineColorRequest) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Magazine magazine = magazineRepository.findById(magazineId).orElseThrow();

        ColorType colorType = ColorType.from(magazineColorRequest.getColorType());

        Optional<Color> findColor = colorRepository.findByMemberAndMagazine(member, magazine);

        if (findColor.isEmpty()) {
            colorRepository.save(Color.builder()
                    .colorType(colorType)
                    .member(member)
                    .magazine(magazine)
                    .build());
        } else if (findColor.get().getColorType() == colorType) {
            colorRepository.delete(findColor.get());
        } else {
            findColor.get().updateColor(colorType);
        }
    }

    @Transactional(readOnly = true)
    public List<MagazineListResponse> findMagazineByHashtag(String hashtag) {
        List<Magazine> findMagazines = magazineRepository.findMagazineByHashtag(hashtag);

        List<MagazineListResponse> collect = findMagazines.stream()
                .map(m -> MagazineListResponse.builder()
                        .magazineId(m.getId())
                        .coverUrl(m.getCover())
                        .title(m.getTitle())
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    @Transactional(readOnly = true)
    public List<MagazineListResponse> recommendedMagazineByMember(String email) {
        List<Magazine> result = new ArrayList<>();

        Member member = memberRepository.findByEmail(email).orElseThrow();
        List<Magazine> latestCreatedMagazine = magazineRepository.findTop3ByMemberOrderByCreateTimeDesc(member);

        if (latestCreatedMagazine.isEmpty()) {
            result.addAll(getRecommendationForNoMagazine(member));
        } else {
            result.addAll(getRecommendationForLatestMagazine(member, latestCreatedMagazine));
            result.addAll(getRecommendationForTop3(member));
            result.addAll(getRecommendationForRespondedMagazine(member));
            result.addAll(getRecommendationForPersonalMood(member));

            DeduplicationUtils.deduplication(result, Magazine::getId);

            if(result.size() < RECOMMENDED_TOTAL_SIZE) {
                result.addAll(getRecommendationForNoMagazine(member));
                DeduplicationUtils.deduplication(result, Magazine::getId);

                result = result.subList(0, RECOMMENDED_TOTAL_SIZE);
            }
        }

        List<MagazineListResponse> collect = result.stream()
                .map(m -> MagazineListResponse.builder()
                        .magazineId(m.getId())
                        .coverUrl(m.getCover())
                        .title(m.getTitle())
                        .build())
                .collect(Collectors.toList());

        return collect;
    }


    // 추천 정책 0번 : 매거진 제작 이력이 없는 사용자
    @Transactional(readOnly = true)
    public List<Magazine> getRecommendationForNoMagazine(Member member) {
        List<String> hashtags = new ArrayList<>();
        List<Magazine> findMagazines = new ArrayList<>();

        List<Hashtag> findHashtags = hashtagRepository.findByMember(member);

        if(findHashtags.isEmpty()) {
            findMagazines = colorRepository.findTop20MagazinesByColor();
        } else {
            for(Hashtag h : findHashtags) {
                hashtags.add(h.getTag());
            }
            findMagazines = findMagazineByHashtagOrderByPriority(member.getEmail(), hashtags);
        }

        if(findMagazines.size() < RECOMMENDED_TOTAL_SIZE) {
            return findMagazines;
        }
        else {
            return findMagazines.subList(0, RECOMMENDED_TOTAL_SIZE);
        }
    }


    // 추천 정책 1번 : 가장 최근 제작한 3개의 매거진 해시태그 기준 추천 (40%)
    @Transactional(readOnly = true)
    public List<Magazine> getRecommendationForLatestMagazine(Member member, List<Magazine> latestCreatedMagazine){
        List<String> hashtags = getHashtagsInMagazine(latestCreatedMagazine);
        List<Magazine> findMagazines = findMagazineByHashtagOrderByPriority(member.getEmail(), hashtags);

        int recommendationSize = calculateRecommendationSize(40);

        if(findMagazines.size() < recommendationSize) {
            return findMagazines;
        }
        else {
            return findMagazines.subList(0, recommendationSize);
        }
    }

    // 추천 정책 2번 : TOP3 해시태그 기준 추천 (30%)
    @Transactional(readOnly = true)
    public List<Magazine> getRecommendationForTop3(Member member) {
        List<String> hashtags = new ArrayList<>();
        List<Magazine> findMagazines;

        HashtagFilterCondition hashtagFilterCond = HashtagFilterCondition.builder()
                .filter("all")
                .build();

        List<HashTagResponse> hashTagResponses = hashtagRepository.getHashtagAnalysisResults(hashtagFilterCond, member.getEmail());

        // '기타' 태그 제외
        for(int i = 0; i < 3; i++) { hashtags.add(hashTagResponses.get(i).getTag()); }

        findMagazines = findMagazineByHashtagOrderByPriority(member.getEmail(), hashtags);

        int recommendationSize = calculateRecommendationSize(30);

        if(findMagazines.size() < recommendationSize) {
            return findMagazines;
        }
        else {
            return findMagazines.subList(0, recommendationSize);
        }
    }

    // 추천 정책 3번 : 사용자가 반응한 매거진 해시태그 기준 추천 (15%)
    @Transactional(readOnly = true)
    public List<Magazine> getRecommendationForRespondedMagazine(Member member) {
        List<String> hashtags;
        List<Magazine> findMagazines;

        findMagazines = magazineRepository.findMagazinesColorByMember(member);
        hashtags = getHashtagsInMagazine(findMagazines);

        findMagazines = findMagazineByHashtagOrderByPriority(member.getEmail(), hashtags);

        int recommendationSize = calculateRecommendationSize(15);

        if(findMagazines.size() < recommendationSize) {
            return findMagazines;
        }
        else {
            return findMagazines.subList(0, recommendationSize);
        }
    }

    // 추천 정책 4번 : 사용자 퍼스널 무드 분석 결과와 같은 반응을 가장 많이 받은 매거진 추천 (15%)
    @Transactional(readOnly = true)
    public List<Magazine> getRecommendationForPersonalMood(Member member) {
        List<Magazine> findMagazines = new ArrayList<>();

        List<PersonalMoodResponse> personalMoodResponses = colorRepository.getPersonalMoodAnalysisResults(member);

        for(PersonalMoodResponse r : personalMoodResponses) {
            ColorType colorType = ColorType.from(r.getColorStyle());
            findMagazines.add(colorRepository.findMagazineByColor(colorType));
        }

        return findMagazines;
    }

    private int calculateRecommendationSize(int rate) {
        return (int)(RECOMMENDED_TOTAL_SIZE * (0.01 * rate));
    }

    private List<String> getHashtagsInMagazine(List<Magazine> magazine) {
        List<String> hashtags = new ArrayList<>();

        for(Magazine m : magazine) {
            List<ScrapHashtag> scrapHashtags = new ArrayList<>();

            for(MagazinePage page : m.getPages()) {
                Scrap scrap = page.getScrap();
                scrapHashtags.addAll(scrapHashtagRepository.findByScrapId(scrap.getId()));
            }

            for(ScrapHashtag scrapHashtag : scrapHashtags) {
                hashtags.add(scrapHashtag.getHashtag().getTag());
            }
        }
        return DeduplicationUtils.deduplication(hashtags);
    }

    // 해시태그로 매거진 찾기 (우선순위)
    private List<Magazine> findMagazineByHashtagOrderByPriority(String email, List<String> hashtags) {
        List<Magazine> findMagazines = new ArrayList<>();
        boolean[] visited = new boolean[hashtags.size()];

        // 겹치는 해시태그가 많은 순서
        for(int i = hashtags.size(); i > 0 ; i--) {
            findMagazines.addAll(
                    combinationOfHashtag(hashtags, visited, 0, hashtags.size(), i, email));
        }

        return DeduplicationUtils.deduplication(findMagazines, Magazine::getId);
    }

    // 조합(백트래킹) : 순서 상관없는 경우의 수
    private List<Magazine> combinationOfHashtag(List<String> hashtags, boolean[] visited, int start, int n, int r, String email) {
        List<Magazine> result = new ArrayList<>();
        if(r == 0) {
            List<String> priorityHashtags = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (visited[i]) {
                    priorityHashtags.add(hashtags.get(i));
                }
            }
            return magazineRepository.findMagazineByHashtagAndNotWriter(priorityHashtags, email);
        }

        for(int i = start; i < n; i++) {
            visited[i] = true;
            result.addAll(combinationOfHashtag(hashtags, visited, i + 1, n, r - 1, email));
            visited[i] = false;
        }
        return result;
    }
}
