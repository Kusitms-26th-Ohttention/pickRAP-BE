package pickRAP.server.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.util.RedisClient;

import java.util.Optional;
import java.util.Random;

import static pickRAP.server.common.BaseExceptionStatus.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifyCodeService {

    private final RedisClient redisClient;
    private final EmailSenderService emailSenderService;

    public void createVerifyCode(String receiverEmail) {
        String code = createKey();

        redisClient.setEmail(code);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(receiverEmail);
        simpleMailMessage.setSubject("[pickRAP] 이메일 인증");
        simpleMailMessage.setText("["+code+"]인증번호를 입력해주세요.");
        emailSenderService.sendEmail(simpleMailMessage);
    }

    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 6; i++) { // 인증코드 6자리
            key.append((rnd.nextInt(10)));
        }
        return key.toString();
    }

    public void verifyCode(String code) {
        // 인증코드 유효시간 만료
        String findCode = Optional.ofNullable(redisClient.getValues(code)).orElseThrow(
                () -> new BaseException(INVALID_EMAIL_CODE)
        );
        redisClient.deleteValues(code);
    }



}
