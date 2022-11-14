package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.magazine.*;
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
        Member member = memberRepository.findByEmail(email).orElseThrow();

        Magazine magazine = Magazine.builder()
                .title(request.getTitle())
                .openStatus(request.isOpenStatus())
                .template(MagazineTemplate.valueOf(template.toUpperCase(Locale.ROOT)))
                .build();

        magazine.setMember(member);

        saveMagazinePages(request.getPageList(), magazine);

        return;
    }

    @Transactional(readOnly = true)
    public List<MagazineListResponse> findMagazines(String email) {
        List<Magazine> findMagazines = magazineRepositoryCustom.findMemberMagazines(email);

        /*
            스크랩 기능 구현 뒤 이미지 미리보기 구현 예정
            if(m.getTemplate() == LINK)
            m.getPages().get(0).getScrap().getContents() => url => URLPreview.get~..

            if(m.getTemplate() == IMAGE || VIDEO)
            m.getPages().get(0).getScrap().getContents()
        */
        List<MagazineListResponse> collect = findMagazines.stream()
                .map(m -> new MagazineListResponse(m.getId(), m.getTitle()))
                .collect(Collectors.toList());

        return collect;
    }

    @Transactional(readOnly = true)
    public MagazineResponse findMagazine(Long magazineId) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();
        List<MagazinePage> findMagazinePages = findMagazine.getPages();

        List<MagazinePageResponse> magazinePages = findMagazinePages.stream()
                .map(p -> new MagazinePageResponse(p.getId(), p.getText()))
                .collect(Collectors.toList());

        MagazineResponse magazine = new MagazineResponse(
                findMagazine.getId(), findMagazine.getTitle(),
                findMagazine.getTemplate(), findMagazine.isOpenStatus(),
                findMagazine.getCreateTime(), magazinePages);

        return magazine;
    }

    @Transactional
    public void updateMagazine(MagazineUpdateRequest request, Long magazineId, String email) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        if(!findMagazine.checkWriter(email)) {
            new BaseException(BaseExceptionStatus.NOT_MATCH_WRITER);
        }

        findMagazine.updateTitle(request.getTitle());

        magazinePageRepository.deleteByMagazine(findMagazine);

        saveMagazinePages(request.getPageList(), findMagazine);

        return;
    }

    @Transactional
    public void saveMagazinePages(List<MagazinePageRequest> requestList, Magazine magazine) {
        requestList.forEach(p -> {
            if (p.getText().length() > MAX_TEXT_LENGTH) {
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

        magazineRepository.save(magazine);
    }

    @Transactional
    public void updateOpenStatus(Long magazineId, String email) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        if(!findMagazine.checkWriter(email)) {
            new BaseException(BaseExceptionStatus.NOT_MATCH_WRITER);
        }

        findMagazine.updateOpenStatus();

        return;
    }
}
