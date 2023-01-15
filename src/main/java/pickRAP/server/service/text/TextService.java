package pickRAP.server.service.text;

import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.analysis.TextResponse;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.text.Text;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.text.TextRepository;
import pickRAP.server.service.etri.EtriService;

import java.util.*;
import java.util.Map.Entry;

@Service
@RequiredArgsConstructor
public class TextService {

    private final TextRepository textRepository;

    private final EtriService etriService;

    private final MemberRepository memberRepository;

    @Transactional
    public void save(Member member, String sentence) {
        Map<String, Long> texts = etriService.analyzeText(sentence);

        for (Entry<String, Long> entry : texts.entrySet()) {
            String word = entry.getKey();
            Long count = entry.getValue();

            Optional<Text> findText = textRepository.findByWordAndMember(word, member);

            if (findText.isPresent()) {
                Text text = findText.get();
                text.plusCount(count);
            } else {
                Text text = Text.builder()
                        .word(word)
                        .count(count)
                        .member(member)
                        .build();
                textRepository.save(text);
            }
        }
    }

    @Transactional
    public void delete(Member member, String sentence) {
        Map<String, Long> texts = etriService.analyzeText(sentence);

        for (Entry<String, Long> entry : texts.entrySet()) {
            String word = entry.getKey();
            Long count = entry.getValue();

            Optional<Text> findText = textRepository.findByWordAndMember(word, member);
            if (findText.isPresent()) {
                Text text = findText.get();

                text.minusCount(count);
                if (text.getCount() == 0) {
                    textRepository.delete(text);
                }
            }
        }
    }

    public List<TextResponse> getTextAnalysisResults(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        List<TextResponse> textResponses = textRepository.findWordCountByMember(member);

        return setRate(textResponses);
    }

    private List<TextResponse> setRate(List<TextResponse> textResponses) {
        long sum = 0;
        long totalRate = 0;

        for (TextResponse textResponse : textResponses) {
            sum += textResponse.getCount();
        }
        if (sum == 0) {
            return textResponses;
        }

        for (TextResponse textResponse : textResponses) {
            long rate = (long) ((double) textResponse.getCount() / sum * 100.0);
            totalRate += rate;
            textResponse.setRate(rate);
        }
        if (totalRate < 100) {
            textResponses.get(0).setRate(textResponses.get(0).getRate() + (100L - totalRate));
        }

        return textResponses;
    }
}
