package pickRAP.server.repository.scrap;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.util.StringUtils;
import pickRAP.server.controller.dto.scrap.QScrapResponse;
import pickRAP.server.controller.dto.scrap.ScrapFilterCondition;
import pickRAP.server.controller.dto.scrap.ScrapResponse;
import pickRAP.server.domain.scrap.ScrapType;

import java.util.List;

import static pickRAP.server.domain.category.QCategory.category;
import static pickRAP.server.domain.scrap.QScrap.*;
import static pickRAP.server.domain.scrap.QScrapHashtag.scrapHashtag;

@RequiredArgsConstructor
public class ScrapRepositoryImpl implements ScrapRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ScrapResponse> filterPageScraps(Long lastScrapId, ScrapFilterCondition scrapFilterCondition, Pageable pageable) {
        List<ScrapResponse> scrapResponses = queryFactory
                .select(new QScrapResponse(
                        scrap.id.as("id"),
                        scrap.title,
                        scrap.content,
                        scrap.memo,
                        scrap.fileUrl.as("fileUrl"),
                        scrap.scrapType,
                        category.name,
                        scrap.createTime))
                .distinct()
                .from(scrap)
                .leftJoin(scrap.category, category)
                .leftJoin(scrap.scrapHashtags, scrapHashtag)
                .where(
                        ltLastScrapId(lastScrapId, scrapFilterCondition.getOrderKeyword()),

                        scrap.member.id.eq(scrapFilterCondition.getMemberId()),

                        contentTypeEq(scrapFilterCondition.getScrapType()),
                        categoryEq(scrapFilterCondition.getCategoryId()),
                        titleTagLike(scrapFilterCondition.getSearchKeyword())
                )
                .orderBy(orderSpecifier(scrapFilterCondition.getOrderKeyword()))
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return checkLastPage(scrapResponses, pageable);
    }

    private BooleanExpression contentTypeEq(ScrapType scrapType) {
        return scrapType != null ? scrap.scrapType.eq(scrapType) : null;
    }

    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId != null ? scrap.category.id.eq(categoryId) : null;
    }

    private BooleanExpression titleTagLike(String searchKeyword) {
        return StringUtils.hasText(searchKeyword) ? scrap.title.contains(searchKeyword).or(scrapHashtag.hashtag.tag.contains(searchKeyword)) : null;
    }

    private BooleanExpression ltLastScrapId(Long scrapId, String orderKeyword) {
        if(scrapId == null) {
            return null;
        } else if(StringUtils.isEmpty(orderKeyword)) {
            return scrap.id.lt(scrapId);
        } else if(orderKeyword.equals("desc")) {
            return scrap.id.lt(scrapId);
        } else if(orderKeyword.equals("asc")) {
            return scrap.id.gt(scrapId);
        } else {
            return null;
        }
    }

    private Slice<ScrapResponse> checkLastPage(List<ScrapResponse> scrapResponses, Pageable pageable) {
        boolean hasNext = false;

        if(scrapResponses.size() > pageable.getPageSize()) {
            hasNext = true;
            scrapResponses.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(scrapResponses, pageable, hasNext);
    }

    private OrderSpecifier orderSpecifier(String orderKeyword) {
        if(StringUtils.isEmpty(orderKeyword)) {
            return scrap.createTime.desc();
        } else if(orderKeyword.equals("desc")) {
            return scrap.createTime.desc();
        } else if(orderKeyword.equals("asc")) {
            return scrap.createTime.asc();
        } else {
            return null;
        }
    }
}
