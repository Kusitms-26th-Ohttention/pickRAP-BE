package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.magazine.MagazineListResponse;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
import pickRAP.server.controller.dto.magazine.MagazineResponse;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.magazine.MagazineTemplate;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.magazine.MagazinePageRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.magazine.MagazineRepositoryCustom;
import pickRAP.server.repository.member.MemberRepository;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MagazineService {

    final static int MAX_TEXT_LENGTH = 400;

    private final MemberRepository memberRepository;
    private final MagazineRepository magazineRepository;
    private final MagazinePageRepository magazinePageRepository;

    private final MagazineRepositoryCustom magazineRepositoryCustom;

    @Transactional
    public void save(MagazineRequest request, String email, String template) {
        Member writer = memberRepository.findByEmail(email).orElseThrow();

        Magazine magazine = Magazine.builder()
                .title(request.getTitle())
                .openStatus(request.isOpenStatus())
                .template(MagazineTemplate.valueOf(template.toUpperCase(Locale.ROOT)))
                .build();

        magazine.setMember(writer);

        magazineRepository.save(magazine);

        request.getPageList().forEach(p -> {
            if(p.getText().length() > MAX_TEXT_LENGTH) {
                throw new BaseException(BaseExceptionStatus.EXCEED_TEXT_LENGTH);
            }

            MagazinePage page = MagazinePage.builder()
                    .text(p.getText())
                    .build();

            // 스크랩 콘텐츠 가져와서 MagazinePage와 세팅
            // Scrap scrap = scrapRepository.findById(page.getScrapId()).orElseThrow();
            // page.setScrap(scrap);

            page.setMagazine(magazine);
            magazinePageRepository.save(page);
        });
        return;
    }

    @Transactional(readOnly = true)
    public List<MagazineListResponse> findMagazine(String email) {
        List<Magazine> findMagazines = magazineRepositoryCustom.findMemberMagazines(email);

        /*
            스크랩 기능 구현 뒤 이미지 미리보기 구현 예정
            if(m.getTemplate() == LINK)
            m.getPages().get(0).getScrap().getContents() => url => URLPreview.get~..

            if(m.getTemplate() == IMAGE || VIDEO)
            m.getPages().get(0).getScrap().getContents()
        */
        List<MagazineListResponse> collect = findMagazines.stream()
                .map(m-> new MagazineListResponse(m.getId(), m.getTitle()))
                .collect(Collectors.toList());

        return collect;
    }
}
