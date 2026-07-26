/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author admin
 */
public class GoogleUtils {

    private static final String CLIENT_ID = "1096276853074-s0bkcjnl6fdica04ie5mot0cuiifbllf.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "";
    private static final String REDIRECT_URI = "http://localhost:8080/Restaurant-Reservation-And-Table-Service-System/login/google/callback";
    // ========================================================

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String SCOPE = "openid email profile";

    public static String buildGoogleAuthUrl(String state) {
        try {
            return AUTH_URL
                    + "?client_id=" + encode(CLIENT_ID)
                    + "&redirect_uri=" + encode(REDIRECT_URI)
                    + "&response_type=code"
                    + "&scope=" + encode(SCOPE)
                    + "&state=" + encode(state)
                    + "&access_type=offline";
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* 
    Đổi authorization code lấy access_token
    Đổi body lấy data dạng JSON dạng String lưu vào responseBody
    Chuyển chuỗi JSON Google trả về thành object Java 
     */
    public static String exchangeCodeForToken(String code) {
        try {
            String body = "code=" + encode(code)
                    + "&client_id=" + encode(CLIENT_ID)
                    + "&client_secret=" + encode(CLIENT_SECRET)
                    + "&redirect_uri=" + encode(REDIRECT_URI)
                    + "&grant_type=authorization_code";

            String responseBody = postRequest(TOKEN_URL, body);
            if (responseBody == null) {
                return null;
            }

            JSONObject json = (JSONObject) new JSONParser().parse(responseBody);
            return (String) json.get("access_token");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* 
    Tạo một đối tượng kết nối HTTP tới URL
    Khai báo để được gửi dữ liệu đi
    Thông báo cho Google biết định dạng dữ liệu mà server sắp gửi trong body
    Đọc toàn bộ dữ liệu bytes từ InputStream và chuyển thành String theo chuẩn UTF-8
     */
    private static String postRequest(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            InputStream is;
            if (conn.getResponseCode() < 400) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    Lấy thông tin user từ Google
    Tạo một đối tượng kết nối HTTP tới URL
    Thông báo cho Google biết định dạng dữ liệu mà server sắp gửi trong body
    */
    public static JSONObject getUserInfo(String accessToken) {
        try {
            URL url = new URL(USER_INFO_URL);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return null;
            }

            String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return (JSONObject) new JSONParser().parse(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Biến đổi dữ liệu thành dạng an toàn để truyền qua URL
    private static String encode(String value) throws java.io.UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

}
