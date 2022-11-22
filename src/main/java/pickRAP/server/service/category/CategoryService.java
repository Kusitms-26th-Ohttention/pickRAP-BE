package pickRAP.server.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.common.URLPreview;
import pickRAP.server.controller.dto.category.CategoryContentsResponse;
import pickRAP.server.controller.dto.category.CategoryRequest;
import pickRAP.server.controller.dto.category.CategoryResponse;
import pickRAP.server.controller.dto.category.CategoryScrapResponse;
import pickRAP.server.controller.dto.scrap.ScrapFilterCondition;
import pickRAP.server.controller.dto.scrap.ScrapResponse;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapHashtag;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.category.CategoryRepository;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapHashtagRepository;
import pickRAP.server.repository.scrap.ScrapRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static pickRAP.server.domain.scrap.QScrap.scrap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final ScrapRepository scrapRepository;

    private final ScrapHashtagRepository scrapHashtagRepository;

    private final MemberRepository memberRepository;

    @Transactional
    public void initial(Member member) {
        Category category = Category.builder()
                .name("카테고리 미지정")
                .build();
        category.setMember(member);

        categoryRepository.save(category);
    }

    @Transactional
    public CategoryResponse save(CategoryRequest categoryRequest, String email) {
        if(StringUtils.isEmpty(categoryRequest.getName())) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
        if(categoryRequest.getName().length() > 20) {
            throw new BaseException(BaseExceptionStatus.CATEGORY_TITLE_LONG);
        }
        if(categoryRepository.findMemberCategory(categoryRequest.getName(), email).isPresent()) {
            throw new BaseException(BaseExceptionStatus.EXIST_CATEGORY);
        }

        Category category = Category.builder()
                .name(categoryRequest.getName())
                .build();
        category.setMember(memberRepository.findByEmail(email).orElseThrow());
        categoryRepository.save(category);

        return new CategoryResponse(category.getId(), category.getName());
    }

    public List<CategoryResponse> findMemberCategories(String email) {
        Member findMember = memberRepository.findByEmail(email).orElseThrow();

        List<Category> result = categoryRepository.findMemberCategories(findMember);

        return result.stream().map(c -> new CategoryResponse(c.getId(), c.getName())).collect(Collectors.toList());
    }

    public List<CategoryScrapResponse> findMemberCategoriesScrap(String email) {
        Member findMember = memberRepository.findByEmail(email).orElseThrow();

        List<Category> result = categoryRepository.findMemberCategories(findMember);
        List<CategoryScrapResponse> categoryScrapResponses = new ArrayList<>();

        for(Category category : result) {
            if(category.getScraps().isEmpty()) {
                categoryScrapResponses.add(CategoryScrapResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build());
            } else {
                Scrap scrap = category.getScraps().get(category.getScraps().size() - 1);

                categoryScrapResponses.add(CategoryScrapResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .scrapType(scrap.getScrapType())
                        .content(scrap.getContent())
                        .fileUrl(scrap.getFileUrl())
                        .build());
            }
        }

        return categoryScrapResponses;
    }

    public Slice<ScrapResponse> filterCategoryPageScraps(Long categoryId, String orderKeyword, String email, Pageable pageable) {
        if(StringUtils.isEmpty(orderKeyword)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();
        Slice<ScrapResponse> scrapResponses = null;
        ScrapFilterCondition scrapFilterCondition;

        if(Objects.isNull(categoryId)) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
        scrapFilterCondition = ScrapFilterCondition.builder()
                .categoryId(categoryId)
                .orderKeyword(orderKeyword)
                .memberId(member.getId())
                .build();

        if(pageable.getPageNumber() == 0) {
            scrapResponses = scrapRepository.filterPageScraps(null, scrapFilterCondition, pageable);
        } else {
            if(scrapFilterCondition.getOrderKeyword().equals("asc")) {
                scrapResponses = scrapRepository.filterPageScraps(Long.valueOf(pageable.getPageNumber() - 1), scrapFilterCondition, pageable);
            } else if(scrapFilterCondition.getOrderKeyword().equals("desc")) {
                scrapResponses = scrapRepository.filterPageScraps(Long.valueOf(pageable.getPageNumber() + 1), scrapFilterCondition, pageable);
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
    public void update(CategoryRequest categoryRequest, Long id, String email) {
        if(StringUtils.isEmpty(categoryRequest.getName())) {
            throw new BaseException(BaseExceptionStatus.EMPTY_INPUT_VALUE);
        }
        if(categoryRequest.getName().length() > 20) {
            throw new BaseException(BaseExceptionStatus.CATEGORY_TITLE_LONG);
        }

        Category findCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY));

        if(!findCategory.getMember().getEmail().equals(email)) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY);
        }
        if(findCategory.getName().equals("카테고리 미지정")) {
            throw new BaseException(BaseExceptionStatus.CANT_UPDATE_CATE);
        }
        if(findCategory.getName().equals(categoryRequest.getName())) {
            throw new BaseException(BaseExceptionStatus.SAME_CATEGORY);
        }
        if(categoryRepository.findMemberCategory(categoryRequest.getName(), email).isPresent()) {
            throw new BaseException(BaseExceptionStatus.EXIST_CATEGORY);
        }

        findCategory.updateName(categoryRequest.getName());
    }

    @Transactional
    public void delete(Long id, String email) {
        Category findCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY));

        if(!findCategory.getMember().equals(memberRepository.findByEmail(email).orElseThrow())) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY);
        }
        if(findCategory.getName().equals("카테고리 미지정")) {
            throw new BaseException(BaseExceptionStatus.CANT_DELETE_CATE);
        }

        Category initialCategory = categoryRepository.findMemberCategory("카테고리 미지정", email)
                .orElseThrow(() -> new BaseException(BaseExceptionStatus.DONT_EXIST_CATEGORY));
        for(Scrap scrap : findCategory.getScraps()) {
            scrap.setCategory(initialCategory);
        }

        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<CategoryContentsResponse> findMemberCategoriesAllScrap(String email) {
        Member findMember = memberRepository.findByEmail(email).orElseThrow();

        List<Category> result = categoryRepository.findMemberCategories(findMember);

        List<CategoryContentsResponse> categoryContentsResponse = new ArrayList<>();

        for(Category category : result) {
            List<CategoryContentsResponse.ScrapResponse> scrapResponse = new ArrayList<>();

            if(!category.getScraps().isEmpty()) {
                List<Scrap> scrapList = category.getScraps();
                scrapList.forEach(s-> {
                    if(s.getScrapType() == ScrapType.IMAGE
                        || s.getScrapType() == ScrapType.VIDEO
                        || s.getScrapType() == ScrapType.PDF) {

                        scrapResponse.add(CategoryContentsResponse.ScrapResponse.builder()
                                .scrapId(s.getId())
                                .fileUrl(s.getFileUrl())
                                .scrapType(s.getScrapType())
                                .category(s.getCategory().getName())
                                .build());
                    } else if(s.getScrapType() == ScrapType.LINK) {
                        scrapResponse.add(CategoryContentsResponse.ScrapResponse.builder()
                                .scrapId(s.getId())
                                .content(s.getContent())
                                .urlPreview(URLPreview.getLinkPreviewInfo(s.getContent()))
                                .scrapType(s.getScrapType())
                                .category(s.getCategory().getName())
                                .build());
                    } else {
                        scrapResponse.add(CategoryContentsResponse.ScrapResponse.builder()
                                .scrapId(s.getId())
                                .content(s.getContent())
                                .scrapType(s.getScrapType())
                                .category(s.getCategory().getName())
                                .build());
                    }
                });
            }
            categoryContentsResponse.add(
                    CategoryContentsResponse.builder()
                            .categoryId(category.getId())
                            .name(category.getName())
                            .scrapResponseList(scrapResponse)
                            .build());
        }

        return categoryContentsResponse;
    }
}
