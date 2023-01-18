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
import pickRAP.server.common.URLPreview;
import pickRAP.server.controller.dto.scrap.*;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.hashtag.Hashtag;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapHashtag;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.hashtag.HashtagRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.text.TextService;

import java.io.IOException;
import java.util.*;

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

    public ScrapResponse findOne(Long id, String email) {
        if(Objects.isNull(id)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Scrap scrap = scrapRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP));
        if(!scrap.getMember().equals(member)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
        }
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
                .build();
        if(scrap.getScrapType().equals(ScrapType.LINK)) {
            scrapResponse.setUrlPreview(URLPreview.getLinkPreviewInfo(scrap.getContent()));
        }
        for(ScrapHashtag scrapHashtag : scrapHashtags) {
            scrapResponse.getHashtags().add(scrapHashtag.getHashtag().getTag());
        }

        return scrapResponse;
    }

    public Slice<ScrapResponse> searchPageScraps(String searchKeyword, String orderKeyword, String email, Pageable pageable) {
        if(!StringUtils.hasText(orderKeyword)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Slice<ScrapResponse> scrapResponses = null;
        ScrapFilterCondition scrapFilterCondition;

        if(!StringUtils.hasText(searchKeyword)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_KEYWORD);
        }

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
            if(scrapResponse.getScrapType().equals("link")) {
                scrapResponse.setUrlPreview(URLPreview.getLinkPreviewInfo(scrapResponse.getContent()));
            }

            List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrapResponse.getId());

            for(ScrapHashtag scrapHashtag : scrapHashtags) {
                scrapResponse.getHashtags().add(scrapHashtag.getHashtag().getTag());
            }
        }

        return scrapResponses;
    }

    public Slice<ScrapResponse> filterTypePageScraps(String filter, String orderKeyword, String email, Pageable pageable) {
        if(!StringUtils.hasText(orderKeyword)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }

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
            if(scrapResponse.getScrapType().equals("link")) {
                scrapResponse.setUrlPreview(URLPreview.getLinkPreviewInfo(scrapResponse.getContent()));
            }

            List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrapResponse.getId());

            for(ScrapHashtag scrapHashtag : scrapHashtags) {
                scrapResponse.getHashtags().add(scrapHashtag.getHashtag().getTag());
            }
        }

        return scrapResponses;
    }

    @Transactional
    public void save(ScrapRequest scrapRequest, MultipartFile multipartFile, String email) throws IOException {
        if(scrapRequest.getHashtags().isEmpty()
                || !StringUtils.hasText(scrapRequest.getScrapType())) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
        if(StringUtils.hasText(scrapRequest.getTitle()) && scrapRequest.getTitle().length() > 15) {
            throw new BaseException(BaseExceptionStatus.SCRAP_TITLE_LONG);
        }

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

        if(scrapRequest.getScrapType().equals("text")
                || scrapRequest.getScrapType().equals("link")) {

            if(!StringUtils.hasText(scrapRequest.getContent())) {
                throw new BaseException(BaseExceptionStatus.DONT_EXIST_CONTENT);
            }
            scrap = createContentScrap(scrapRequest, member, category);

        } else if(scrapRequest.getScrapType().equals("image")
                || scrapRequest.getScrapType().equals("video")
                || scrapRequest.getScrapType().equals("pdf")) {

            if(multipartFile.isEmpty()) {
                throw new BaseException(BaseExceptionStatus.DONT_EXIST_FILE);
            }
            String fileUrl = uploadFile(multipartFile, "scrap");
            scrap = createFileScrap(scrapRequest, member, category, fileUrl);

        }

        List<String> hashtags = scrapRequest.getHashtags();
        saveScrapHashTag(hashtags, scrap, member);

        scrapRepository.save(scrap);

        saveTextByScrap(scrap, member, false);
    }

    @Transactional
    public void update(ScrapUpdateRequest scrapUpdateRequest, String email) {
        if(scrapUpdateRequest.getHashtags().isEmpty()) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
        if(StringUtils.hasText(scrapUpdateRequest.getTitle()) && scrapUpdateRequest.getTitle().length() > 15) {
            throw new BaseException(BaseExceptionStatus.SCRAP_TITLE_LONG);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Scrap scrap = scrapRepository.findById(scrapUpdateRequest.getId())
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP));
        if(!scrap.getMember().equals(member)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
        }

        deleteTextByScrap(scrap, member, true);

        scrap.updateScrap(scrapUpdateRequest.getTitle(), scrapUpdateRequest.getMemo());

        saveTextByScrap(scrap, member, true);

        deleteHashtag(scrapUpdateRequest.getId());

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

        if(!scrap.getMember().equals(member)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
        }

        deleteHashtag(id);
        deleteTextByScrap(scrap, member, false);

        scrapRepository.deleteById(id);
    }

    private List<Hashtag> saveHashtags(List<String> hashtagRequests, Member member) {
        List<Hashtag> hashtags = new ArrayList<>();

        for(String hashtagRequest : hashtagRequests) {
            Hashtag hashtag = Hashtag.builder()
                    .tag(hashtagRequest)
                    .member(member)
                    .build();

            hashtags.add(hashtag);
            hashtagRepository.save(hashtag);
        }

        return hashtags;
    }

    private Scrap createContentScrap(ScrapRequest scrapRequest, Member member, Category category) {
        Scrap scrap = Scrap.builder()
                .title(scrapRequest.getTitle())
                .content(scrapRequest.getContent())
                .memo(scrapRequest.getMemo())
                .scrapType(ScrapType.valueOf(scrapRequest.getScrapType().toUpperCase(Locale.ROOT)))
                .build();
        scrap.setMember(member);
        scrap.setCategory(category);

        return scrap;
    }

    private Scrap createFileScrap(ScrapRequest scrapRequest, Member member, Category category, String fileUrl) {
        Scrap scrap = Scrap.builder()
                .title(scrapRequest.getTitle())
                .memo(scrapRequest.getMemo())
                .fileUrl(fileUrl)
                .scrapType(ScrapType.valueOf(scrapRequest.getScrapType().toUpperCase(Locale.ROOT)))
                .build();
        scrap.setMember(member);
        scrap.setCategory(category);

        return scrap;
    }

    private void deleteHashtag(Long scrapId){
        List<ScrapHashtag> scrapHashtags = scrapHashtagRepository.findByScrapId(scrapId);
        for(ScrapHashtag scrapHashtag : scrapHashtags) {
            hashtagRepository.delete(scrapHashtag.getHashtag());
            scrapHashtagRepository.delete(scrapHashtag);
        }
    }

    private void saveTextByScrap(Scrap scrap, Member member, boolean update) {
        textService.save(member, scrap.getMemo());
        if (scrap.getScrapType() == ScrapType.TEXT && !update) {
            textService.save(member, scrap.getContent());
        }
    }

    private void deleteTextByScrap(Scrap scrap, Member member, boolean update) {
        textService.delete(member, scrap.getMemo());
        if (scrap.getScrapType() == ScrapType.TEXT && !update) {
            textService.delete(member, scrap.getContent());
        }
    }
}
