package pickRAP.server.service.word;

import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.controller.dto.word.TextResponse;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.word.Word;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.word.WordRepository;
import pickRAP.server.service.etri.EtriService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;

    private final EtriService etriService;

    private final MemberRepository memberRepository;

    @Transactional
    public void save(Member member, String text) {
        Map<String, Long> words = etriService.analyzeText(text);

        for (String value : words.keySet()) {
            Optional<Word> findWord = wordRepository.findByValueAndMember(value, member);

            if (findWord.isPresent()) {
                Word word = findWord.get();
                word.plusCount(words.get(value));
            } else {
                Word word = Word.builder()
                        .value(value)
                        .count(words.get(value))
                        .member(member)
                        .build();
                wordRepository.save(word);
            }
        }
    }

    @Transactional
    public void delete(Member member, String text) {
        Map<String, Long> words = etriService.analyzeText(text);

        for (String value : words.keySet()) {
            Word word = wordRepository.findByValueAndMember(value, member).orElseThrow();
            word.minusCount(words.get(value));

            if (word.getCount() == 0) {
                wordRepository.delete(word);
            }
        }
    }

    public AnalysisResponse getTextAnalysisResult(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        List<Word> words = wordRepository.findTop5ByMemberOrderByCountDesc(member);

        return setRate(words);
    }

    private AnalysisResponse setRate(List<Word> words) {
        AnalysisResponse analysisResponse = new AnalysisResponse();

        long sum = 0;
        for (Word word : words) {
            sum += word.getCount();
        }
        for (Word word : words) {
            long rate = (long) ((double) word.getCount() / (double) sum * 100.0);
            analysisResponse.getTextResponses().add(
                    TextResponse.builder()
                            .text(word.getValue())
                            .count(word.getCount())
                            .rate(rate)
                            .build()
            );
        }

        return analysisResponse;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AnalysisResponse {
        List<TextResponse> textResponses = new ArrayList<>();
    }
}
