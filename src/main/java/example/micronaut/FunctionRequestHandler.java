package example.micronaut;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.inject.Inject;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.function.aws.MicronautRequestHandler;

public class FunctionRequestHandler
        extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String AES_KEY = "0123456789abcdef0123456789abcdef";

    @Inject
    ObjectMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent request) {

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        try {
            String str = run(request);
            response.setStatusCode(200);
            response.setBody(str);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500);
            response.setBody(new JSONObject().toString());
            return response;
        }
    }

    private String run(APIGatewayProxyRequestEvent request)
            throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {

        //        encrypt();
        decrypt();

        JSONObject body = new JSONObject();
        body.put("res", "hello");
        return body.toString();
    }

    private void decrypt() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] encryptedData = Files.readAllBytes(Paths.get("C:\\develop\\workspace-java\\csv\\example-enc.csv"));

        byte[] decryptedData = decryptAES(encryptedData, AES_KEY);

        File decryptedCsv = new File("C:\\develop\\workspace-java\\csv\\example-dec.csv");
        try (FileOutputStream fos = new FileOutputStream(decryptedCsv)) {
            fos.write(decryptedData);
        }
    }

    // AES-256 復号
    private static byte[] decryptAES(byte[] encryptedData, String key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, 16);
        byte[] cipherText = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);

        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

        return cipher.doFinal(cipherText);
    }

    private void encrypt() throws IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] fileData = Files.readAllBytes(Paths.get("C:\\develop\\workspace-java\\csv\\example.csv"));

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        byte[] encryptedData = encryptAES(fileData, AES_KEY, iv);

        File encryptedCsv = new File("C:\\develop\\workspace-java\\csv\\example-enc.csv");
        try (FileOutputStream fos = new FileOutputStream(encryptedCsv)) {
            fos.write(encryptedData);
        }
    }

    private static byte[] encryptAES(byte[] data, String key, byte[] iv) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, IOException {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

        byte[] encryptedData = cipher.doFinal(data);

        // IV + 暗号データを結合
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv); // 先頭にIVを付加
        outputStream.write(encryptedData);
        return outputStream.toByteArray();
    }
}