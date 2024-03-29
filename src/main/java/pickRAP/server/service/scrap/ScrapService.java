package pickRAP.server.service.scrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.scrap.*;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.hashtag.Hashtag;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapHashtag;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.magazine.MagazinePageRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.hashtag.HashtagRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.magazine.MagazineService;
import pickRAP.server.service.text.TextService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static pickRAP.server.util.preview.PreviewUtil.*;
import static pickRAP.server.util.s3.S3Util.uploadFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;

    private final HashtagRepository hashtagRepository;

    private final ScrapHashtagRepository scrapHashtagRepository;

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    private final TextService textService;

    private final MagazinePageRepository magazinePageRepository;

    private final MagazineService magazineService;

    @Transactional
    public ScrapResponse findOne(Long id, String email) {
        emptyScrapId(id);

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Scrap scrap = scrapRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP));
        dontExistScrap(scrap, member);
        List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrap.getId());

        ScrapResponse scrapResponse = ScrapResponse.builder()
                .id(scrap.getId())
                .title(scrap.getTitle())
                .content(scrap.getContent())
                .memo(scrap.getMemo())
                .fileUrl(scrap.getFileUrl())
                .scrapType(scrap.getScrapType().toString().toLowerCase(Locale.ROOT))
                .category(scrap.getCategory().getName())
                .createTime(scrap.getCreateTime())
                .previewUrl(scrap.getPreviewUrl())
                .build();
        for(ScrapHashtag scrapHashtag : scrapHashtags) {
            scrapResponse.getHashtags().add(scrapHashtag.getHashtag().getTag());
        }

        if(compareToRevisitDay(scrap.getRevisitTime()) == 1) {
            scrap.updateRevisitRecord();
        }

        return scrapResponse;
    }

    private int compareToRevisitDay(LocalDateTime lastedRevisitTime) {
        // 일 단위 비교
        lastedRevisitTime = lastedRevisitTime
                .truncatedTo(ChronoUnit.DAYS);
        LocalDateTime today = LocalDateTime.now()
                .truncatedTo(ChronoUnit.DAYS);

        if(lastedRevisitTime.compareTo(today) != 0) {
            // 재방문 날짜가 오늘이 아니라면
            return 1;
        }
        return 0;
    }

    public Slice<ScrapResponse> searchPageScraps(String searchKeyword, String orderKeyword, String email, Pageable pageable) {
        emptySearchKeyword(searchKeyword);
        emptyOrderKeyword(orderKeyword);

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Slice<ScrapResponse> scrapResponses = null;
        ScrapFilterCondition scrapFilterCondition;

        scrapFilterCondition = ScrapFilterCondition.builder()
                .searchKeyword(searchKeyword)
                .orderKeyword(orderKeyword)
                .memberId(member.getId())
                .build();

        if(pageable.getPageNumber() == 0) {
            scrapResponses = scrapRepository.filterPageScraps(null, scrapFilterCondition, pageable);
        } else {
            if(scrapFilterCondition.getOrderKeyword().equals("asc")) {
                scrapResponses = scrapRepository.filterPageScraps(Long.valueOf(pageable.getPageNumber() - 1L), scrapFilterCondition, pageable);
            } else if(scrapFilterCondition.getOrderKeyword().equals("desc")) {
                scrapResponses = scrapRepository.filterPageScraps(Long.valueOf(pageable.getPageNumber() + 1L), scrapFilterCondition, pageable);
            }
        }

        //로직 고민
        for(ScrapResponse scrapResponse : scrapResponses) {
            List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrapResponse.getId());

            for(ScrapHashtag scrapHashtag : scrapHashtags) {
                scrapResponse.getHashtags().add(scrapHashtag.getHashtag().getTag());
            }
        }

        return scrapResponses;
    }

    public Slice<ScrapResponse> filterTypePageScraps(String filter, String orderKeyword, String email, Pageable pageable) {
        emptyOrderKeyword(orderKeyword);

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Slice<ScrapResponse> scrapResponses = null;
        ScrapFilterCondition scrapFilterCondition;

        //filter 예외처리?
        scrapFilterCondition = ScrapFilterCondition.builder()
                .scrapType(ScrapType.valueOf(filter.toUpperCase(Locale.ROOT)))
                .orderKeyword(orderKeyword)
                .memberId(member.getId())
                .build();

        if(pageable.getPageNumber() == 0) {
            scrapResponses = scrapRepository.filterPageScraps(null, scrapFilterCondition, pageable);
        } else {
             if(scrapFilterCondition.getOrderKeyword().equals("asc")) {
                scrapResponses = scrapRepository.filterPageScraps(Long.valueOf(pageable.getPageNumber() - 1L), scrapFilterCondition, pageable);
            } else if(scrapFilterCondition.getOrderKeyword().equals("desc")) {
                scrapResponses = scrapRepository.filterPageScraps(Long.valueOf(pageable.getPageNumber() + 1L), scrapFilterCondition, pageable);
            }
        }

        //로직 고민
        for(ScrapResponse scrapResponse : scrapResponses) {
            List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrapResponse.getId());

            for(ScrapHashtag scrapHashtag : scrapHashtags) {
                scrapResponse.getHashtags().add(scrapHashtag.getHashtag().getTag());
            }
        }

        return scrapResponses;
    }

    @Transactional
    public void save(ScrapRequest scrapRequest, MultipartFile multipartFile, String email) {
        emptyStringValue(scrapRequest.getScrapType());
        emptyHashtag(scrapRequest.getHashtags());
        scrapTitleLong(scrapRequest.getTitle());

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Category category;
        if(Objects.isNull(scrapRequest.getCategoryId())) {
            category = categoryRepository.findMemberCategory("카테고리 미지정", email)
                    .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY));
        } else {
            category = categoryRepository.findById(scrapRequest.getCategoryId())
                    .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY));
            if(!category.getMember().equals(member)) {
                throw new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY);
            }
        }

        Scrap scrap = Scrap.builder().build();
        String previewUrl = null;

        if(scrapRequest.getScrapType().equals("text")) {

            emptyStringValue(scrapRequest.getContent());
            scrap = createContentScrap(scrapRequest, member, category, previewUrl);

        } else if(scrapRequest.getScrapType().equals("link")) {

            emptyStringValue(scrapRequest.getContent());
            previewUrl = createLinkPreview(scrapRequest.getContent());
            scrap = createContentScrap(scrapRequest, member, category, previewUrl);

        } else if(scrapRequest.getScrapType().equals("pdf")) {

            emptyFile(multipartFile);
            previewUrl = createPdfPreview(multipartFile);
            scrap = createFileScrap(scrapRequest, member, category, multipartFile, previewUrl);

        } else if(scrapRequest.getScrapType().equals("video")) {

            emptyFile(multipartFile);
            previewUrl = createVideoPreview(multipartFile);
            scrap = createFileScrap(scrapRequest, member, category, multipartFile, previewUrl);

        } else if(scrapRequest.getScrapType().equals("image")) {

            emptyFile(multipartFile);
            scrap = createFileScrap(scrapRequest, member, category, multipartFile, previewUrl);

        }

        List<String> hashtags = scrapRequest.getHashtags();
        saveScrapHashTag(hashtags, scrap, member);

        scrapRepository.save(scrap);

        saveTextByScrap(scrap, member, false);
    }

    @Transactional
    public void update(ScrapUpdateRequest scrapUpdateRequest, String email) {
        emptyHashtag(scrapUpdateRequest.getHashtags());
        scrapTitleLong(scrapUpdateRequest.getTitle());

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Scrap scrap = scrapRepository.findById(scrapUpdateRequest.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP));
        dontExistScrap(scrap, member);

        deleteTextByScrap(scrap, member, true);
        deleteHashtag(scrapUpdateRequest.getId());

        scrap.updateScrap(scrapUpdateRequest.getTitle(), scrapUpdateRequest.getMemo());

        saveTextByScrap(scrap, member, true);
        List<String> hashtags = scrapUpdateRequest.getHashtags();
        saveScrapHashTag(hashtags, scrap, member);
    }

    private void saveScrapHashTag(List<String> hashtags, Scrap scrap, Member member) {
        List<Hashtag> saveHashtags = saveHashtags(hashtags, member);

        for(Hashtag hashtag : saveHashtags) {
            ScrapHashtag scrapHashtag = ScrapHashtag.builder()
                    .scrap(scrap)
                    .hashtag(hashtag)
                    .build();
            scrapHashtagRepository.save(scrapHashtag);
        }
    }

    @Transactional
    public void delete(Long id, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Scrap scrap = scrapRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP));

        dontExistScrap(scrap, member);

        List<Long> magazinePageIds = magazinePageRepository.findByScrapId(id);
        magazinePageIds.forEach(mp -> magazineService.deletePage(mp, email));
        deleteHashtag(id);
        deleteTextByScrap(scrap, member, false);

        scrapRepository.deleteById(id);
    }

    private List<Hashtag> saveHashtags(List<String> hashtagRequests, Member member) {
        List<Hashtag> hashtags = new ArrayList<>();

        for(String hashtagRequest : hashtagRequests) {
            List<Hashtag> findHashtags = hashtagRepository.findByMemberAndTag(member, hashtagRequest);
            boolean usedInProfile = false;
            if(!findHashtags.isEmpty() && findHashtags.get(0).isUsedInProfile()) {
                usedInProfile = true;
            }

            Hashtag hashtag = Hashtag.builder()
                    .tag(hashtagRequest)
                    .member(member)
                    .build();
            hashtag.updateProfile(usedInProfile);

            hashtags.add(hashtag);
            hashtagRepository.save(hashtag);
        }

        return hashtags;
    }

    private Scrap createContentScrap(ScrapRequest scrapRequest, Member member, Category category, String previewUrl) {
        Scrap scrap = Scrap.builder()
                .title(scrapRequest.getTitle())
                .content(scrapRequest.getContent())
                .memo(scrapRequest.getMemo())
                .scrapType(ScrapType.valueOf(scrapRequest.getScrapType().toUpperCase(Locale.ROOT)))
                .revisitTime(LocalDateTime.now())
                .revisitCount(1L)
                .previewUrl(previewUrl)
                .build();
        scrap.setMember(member);
        scrap.setCategory(category);

        return scrap;
    }

    private Scrap createFileScrap(ScrapRequest scrapRequest, Member member, Category category, MultipartFile multipartFile, String previewUrl) {
        String fileUrl = uploadFile(multipartFile, "scrap", scrapRequest.getScrapType());
        Scrap scrap = Scrap.builder()
                .title(scrapRequest.getTitle())
                .memo(scrapRequest.getMemo())
                .fileUrl(fileUrl)
                .scrapType(ScrapType.valueOf(scrapRequest.getScrapType().toUpperCase(Locale.ROOT)))
                .revisitTime(LocalDateTime.now())
                .revisitCount(1L)
                .previewUrl(previewUrl)
                .build();
        scrap.setMember(member);
        scrap.setCategory(category);

        return scrap;
    }

    private void deleteHashtag(Long scrapId){
        List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrapId);
        for(ScrapHashtag scrapHashtag : scrapHashtags) {
            Long hashtagId = scrapHashtag.getHashtag().getId();
            scrapHashtagRepository.delete(scrapHashtag);
            hashtagRepository.deleteById(hashtagId);
        }
    }

    private void saveTextByScrap(Scrap scrap, Member member, boolean update) {
        if (StringUtils.hasText(scrap.getMemo())) {
            textService.save(member, scrap.getMemo());
        }
        if (scrap.getScrapType() == ScrapType.TEXT && !update) {
            textService.save(member, scrap.getContent());
        }
    }

    private void deleteTextByScrap(Scrap scrap, Member member, boolean update) {
        if (StringUtils.hasText(scrap.getMemo())) {
            textService.delete(member, scrap.getMemo());
        }
        if (scrap.getScrapType() == ScrapType.TEXT && !update) {
            textService.delete(member, scrap.getContent());
        }
    }

    //예외처리 메소드
    private void dontExistScrap(Scrap scrap, Member member) {
        if(!scrap.getMember().equals(member)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
        }
    }

    private void scrapTitleLong(String title) {
        if(StringUtils.hasText(title) && title.length() > 15) {
            throw new BaseException(BaseExceptionStatus.SCRAP_TITLE_LONG);
        }
    }

    private void emptyOrderKeyword(String orderKeyword) {
        if(!StringUtils.hasText(orderKeyword)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
    }

    private void emptySearchKeyword(String searchKeyword) {
        if(!StringUtils.hasText(searchKeyword)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_KEYWORD);
        }
    }

    private void emptyHashtag(List<String> hashtags) {
        if(hashtags.isEmpty()) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
    }

    private void emptyScrapId(Long id) {
        if(Objects.isNull(id)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
    }

    private void emptyStringValue(String type) {
        if(!StringUtils.hasText(type)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
    }

    private void emptyFile(MultipartFile multipartFile) {
        if(multipartFile.isEmpty()) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_FILE);
        }
    }
}
