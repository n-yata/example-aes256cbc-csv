package example.micronaut;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

@MicronautTest
public class FunctionRequestHandlerTest {

    @Inject
    private static FunctionRequestHandler handler;

    @BeforeAll
    public static void setupSpec() {
        handler = new FunctionRequestHandler();
    }

    @AfterAll
    public static void cleanupSpec() {
        handler.getApplicationContext().close();
    }

    @Test
    public void testHandler() {
        /* 引数設定 */
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();

        /* 呼び出し */
        APIGatewayProxyResponseEvent response = handler.execute(request);

        /* 判定 */
        System.out.println(response.getBody());
        assertEquals(200, response.getStatusCode().intValue());
    }

    @Test
    public void testHandlerWithRandomString() {
        /* 32バイトのランダムな文字列を生成 */
        String randomString = generateRandomString(32);

        System.out.println(randomString);

    }

    /**
     * 32バイトのランダムな英数字文字列を生成
     */
    private String generateRandomString(int byteLength) {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(byteLength);

        for (int i = 0; i < byteLength; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }
}
