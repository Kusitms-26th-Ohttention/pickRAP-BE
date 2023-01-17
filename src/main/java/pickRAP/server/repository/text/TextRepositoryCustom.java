package pickRAP.server.repository.text;

import pickRAP.server.controller.dto.analysis.TextResponse;
import pickRAP.server.domain.member.Member;

import java.util.List;

public interface TextRepositoryCustom {

    List<TextResponse> findWordCountByMember(Member member);
}
