package pickRAP.server.util.etri;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EtriUtil {

    private static String openApiURL;

    private static String accessKey;

    @Value("${etri.api.url}")
    public void setOpenApiURL(String url) {
        openApiURL = url;
    }

    @Value("${etri.api.access-key}")
    public void setAccessKey(String key) {
        accessKey = key;
    }

    public static Map<String, Long> analyzeText(String text) {

        String analysisCode = "morp";

        Gson gson = new Gson();

        try {
            RestTemplate restTemplate = new RestTemplate();

            //request header 설정
            HttpHeaders requestHeader = new HttpHeaders();
            requestHeader.set("Authorization", accessKey);
            requestHeader.setContentType(MediaType.APPLICATION_JSON);

            //request body 설정
            JSONObject argument = new JSONObject();
            argument.put("analysis_code", analysisCode);
            argument.put("text", text);

            JSONObject requestBody = new JSONObject();
            requestBody.put("request_id", "reserved field");
            requestBody.put("argument", argument);

            //request message 생성
            HttpEntity<JSONObject> request = new HttpEntity<>(requestBody, requestHeader);

            //api 통신
            ResponseEntity<String> response = restTemplate.postForEntity(openApiURL, request, String.class);

            Map<String, Object> responseBody = gson.fromJson(response.getBody(), Map.class);
            Map<String, Object> returnObject = (Map<String, Object>) responseBody.get("return_object");
            List<Map<String, Object>> sentences = (List<Map<String, Object>>) returnObject.get("sentence");

            Map<String, Long> morphemesMap = new HashMap<>();
            //문장 별로 단어 수 구하기(NNP : 고유명사, NNG : 일반명사)
            for (Map<String, Object> sentence : sentences) {
                List<Map<String, Object>> morphologicalAnalysisResult = (List<Map<String, Object>>) sentence.get("morp");
                for( Map<String, Object> morphemeInfo : morphologicalAnalysisResult ) {
                    if (morphemeInfo.get("type").equals("NNP") || morphemeInfo.get("type").equals("NNG")) {
                        String lemma = (String) morphemeInfo.get("lemma");

                        morphemesMap.put(lemma, morphemesMap.getOrDefault(lemma, 0L) + 1L);
                    }
                }
            }

            return morphemesMap;
        } catch (Exception e) {
            e.printStackTrace();

            return new HashMap<>();
        }
    }
}
