package pickRAP.server.repository.text;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.controller.dto.analysis.QTextResponse;
import pickRAP.server.controller.dto.analysis.TextResponse;
import pickRAP.server.domain.member.Member;

import java.util.List;

import static pickRAP.server.domain.text.QText.*;

@RequiredArgsConstructor
public class TextRepositoryImpl implements TextRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<TextResponse> findWordCountByMember(Member member) {
        return jpaQueryFactory
                .select(new QTextResponse(text.word, text.count.longValue()))
                .from(text)
                .where(text.member.eq(member))
                .orderBy(text.count.desc(), text.createTime.desc())
                .offset(0)
                .limit(10)
                .fetch();
    }
}
