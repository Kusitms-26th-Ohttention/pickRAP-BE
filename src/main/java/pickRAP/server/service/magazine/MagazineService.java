package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;
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
import pickRAP.server.repository.magazine.MagazineRepositoryCustom;
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

    final static int MAX_TEXT_LENGTH = 200;
    final static int MAX_TITLE_LENGTH = 15;

    final static int RECOMMENDED_MAGAZINE_FIRST_SIZE = 8;
    final static int RECOMMENDED_MAGAZINE_REMAIN_SIZE = 6;

    private final MemberRepository memberRepository;
    private final MagazineRepository magazineRepository;
    private final MagazineRepositoryCustom magazineRepositoryCustom;
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
        List<Magazine> findMagazines = magazineRepositoryCustom.findMemberMagazines(email);

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

    @Transactional
    public List<MagazineListResponse> findMagazineByHashtag(String hashtag) {
        List<Magazine> findMagazines = magazineRepositoryCustom.findMagazineByHashtag(hashtag);

        List<MagazineListResponse> collect = findMagazines.stream()
                .map(m -> MagazineListResponse.builder()
                        .magazineId(m.getId())
                        .coverUrl(m.getCover())
                        .title(m.getTitle())
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    @Transactional
    public List<MagazineListResponse> recommendedMagazineByMember(String email) {
        // 기준별 콘텐츠가 비율만큼 서치되었는지 여부
        boolean isFullFirst = false, isFullSecond = false, isFullThird = false;

        // 최종 추천 매거진을 수집
        List<Magazine> result = new ArrayList<>();

        // 1-1. 사용자의 최근 제작된 3개의 매거진의 해시태그 사용하기
        Member member = memberRepository.findByEmail(email).orElseThrow();
        List<Magazine> latestMagazine = magazineRepository.findTop3ByMemberOrderByCreateTimeDesc(member);

        List<String> hashtags = new ArrayList<>();
        List<Magazine> findMagazines = new ArrayList<>();

        if (latestMagazine.size() == 0) {
            // 사용자의 매거진 제작 이력이 없다면 스크랩 해시태그 기준 추천
            List<Hashtag> findHashtags = hashtagRepository.findByMember(member);

            if(hashtags.size() == 0) {
                //TODO : 스크랩 제작 이력이 없다면 가장 많은 퍼스널 무드를 받은 매거진 20개 추천
                /**
                 * List<MagazineListResponse> collect = result.stream()
                 *                 .map(m -> MagazineListResponse.builder()
                 *                         .magazineId(m.getId())
                 *                         .coverUrl(m.getCover())
                 *                         .title(m.getTitle())
                 *                         .build())
                 *                 .collect(Collectors.toList());
                 *
                 *         return collect;
                 */
            }

            for(Hashtag h : findHashtags) {
                hashtags.add(h.getTag());
            }
        }
        else {
            // 1-2. 사용자의 해시태그 String 리스트를 먼저 찾기
            hashtags = getMagazineHashtags(latestMagazine);
            // 해시태그 중복 제거
            hashtags = DeduplicationUtils.deduplication(hashtags);
        }

        // 1-2. 사용자의 해시태그를 바탕으로 Magazine 찾기
        findMagazines = findMagazineByHashtagOrderByPriority(findMagazines, hashtags, email);
        findMagazines = magazineDeduplication(findMagazines);

        if(findMagazines.size() < RECOMMENDED_MAGAZINE_FIRST_SIZE) {
            result.addAll(findMagazines);
        } else {
            isFullFirst = true;
            result.addAll(findMagazines.subList(0, RECOMMENDED_MAGAZINE_FIRST_SIZE));
        }

        // 2-1. 사용자의 TOP3 해시태그 String 리스트를 먼저 찾기 - 이전에 사용한 해시태그 분석 로직 재사용
        HashtagFilterCondition hashtagFilterCond = HashtagFilterCondition.builder()
                .filter("all")
                .build();

        List<HashTagResponse> hashTagResponses = hashtagRepository.getHashtagAnalysisResults(hashtagFilterCond, email);

        hashtags = new ArrayList<>();
        // '기타' 태그 제외
        for(int i = 0; i < 3; i++) {
            hashtags.add(hashTagResponses.get(i).getTag());
        }

        // 2-2. 사용자의 해시태그를 바탕으로 Magazine 찾기
        findMagazines = findMagazineByHashtagOrderByPriority(findMagazines, hashtags, email);
        findMagazines = magazineDeduplication(findMagazines);

        if(findMagazines.size() < RECOMMENDED_MAGAZINE_REMAIN_SIZE) {
            result.addAll(findMagazines);
            result = magazineDeduplication(result);
        }
        else {
            result.addAll(findMagazines);
            result = magazineDeduplication(result);

            if(result.size() < RECOMMENDED_MAGAZINE_FIRST_SIZE + RECOMMENDED_MAGAZINE_REMAIN_SIZE) {
                result = result.subList(0, result.size());
            }
            else {
                result = result.subList(0, RECOMMENDED_MAGAZINE_FIRST_SIZE + RECOMMENDED_MAGAZINE_REMAIN_SIZE);
            }
        }

        //TODO : 3-1) 사용자가 반응한 매거진 해시태그 기준 추천 - 15%
        //TODO : 3-2) 사용자의 퍼스널 무드 분석 결과와 같은 반응을 받은 매거진 기준 추천 - 15%

        List<MagazineListResponse> collect = result.stream()
                .map(m -> MagazineListResponse.builder()
                        .magazineId(m.getId())
                        .coverUrl(m.getCover())
                        .title(m.getTitle())
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    // 매거진의 모든 해시태그를 반환
    private List<String> getMagazineHashtags(List<Magazine> magazine) {
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
        return hashtags;
    }

    // List Object 중복제거 (key:제목)
    private List<Magazine> magazineDeduplication(List<Magazine> magazines) {
        return DeduplicationUtils.deduplication(magazines, Magazine::getId);
    }

    // 해시태그로 매거진 찾기 (우선순위)
    private List<Magazine> findMagazineByHashtagOrderByPriority(List<Magazine> findMagazines,
                                                             List<String> hashtags, String email) {
        boolean[] visited = new boolean[hashtags.size()];

        // 겹치는 해시태그가 많은 순서
        for(int i = hashtags.size(); i > 0 ; i--) {
            findMagazines.addAll(combination(hashtags, visited, 0, hashtags.size(), i, email));
        }

        return findMagazines;
    }

    // 조합(백트래킹) : 순서 상관없는 경우의 수
    private List<Magazine> combination(List<String> hashtags, boolean[] visited, int start, int n, int r, String email) {
        List<Magazine> result = new ArrayList<>();
        if(r == 0) {
            List<String> priorityHashtags = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (visited[i]) {
                    priorityHashtags.add(hashtags.get(i));
                }
            }
            return magazineRepositoryCustom.findMagazineByHashtagAndNotWriter(priorityHashtags, email);
        }

        for(int i = start; i < n; i++) {
            visited[i] = true;
            result.addAll(combination(hashtags, visited, i + 1, n, r - 1, email));
            visited[i] = false;
        }
        return result;
    }
}
