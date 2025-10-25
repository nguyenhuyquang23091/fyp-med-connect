package com.profile.vnpay.util;

import com.profile.vnpay.config.VNPAYConfig;
import com.profile.vnpay.model.PaymentInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnPayUtil {

    VNPAYConfig vnpayConfig;
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String getPaymentURL(PaymentInfo paymentInfo) {
        Map<String, String> params = vnpayConfig.getVNPayConfig();
        ZoneId zone = TimeZone.getTimeZone("GMT+7").toZoneId();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        params.put("vnp_TxnRef", paymentInfo.getReference());
        params.put("vnp_OrderInfo", paymentInfo.getDescription());
        params.put("vnp_Amount", String.valueOf(paymentInfo.getAmount()));
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_CreateDate", formatter.format(paymentInfo.getCreatedAt().atZone(zone).toLocalDateTime()));
        params.put("vnp_ExpireDate", formatter.format(paymentInfo.getExpiredAt().atZone(zone).toLocalDateTime()));
        params.put("vnp_IpAddr", paymentInfo.getIpAddress());

        String queryURL = buildQuery(params, true);
        String hashData = buildQuery(params, false);
        String vnpSecureHash = hmacSHA512(vnpayConfig.getSecretKey(), hashData);
        queryURL += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnpayConfig.getVnp_PayUrl() + "?" + queryURL;
        return paymentUrl;
    }
    private String buildQuery(Map<String, String> params, boolean encodeKey) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Charset charset = StandardCharsets.US_ASCII;
                    String key = encodeKey ? URLEncoder.encode(e.getKey(), charset) : e.getKey();
                    return key + "=" + URLEncoder.encode(e.getValue(), charset);
                })
                .collect(Collectors.joining("&"));
    }


    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = fields.get(key);
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (i < fieldNames.size() - 1) {
                    hashData.append("&");
                }
            }
        }

        return hmacSHA512(vnpayConfig.getSecretKey(), hashData.toString());
    }
}
