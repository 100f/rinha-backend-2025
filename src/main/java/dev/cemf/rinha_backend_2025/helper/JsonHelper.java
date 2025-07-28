package dev.cemf.rinha_backend_2025.helper;

import dev.cemf.rinha_backend_2025.dto.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class JsonHelper {
    private JsonHelper() {}

    public static String addRequestedAtFieldTo(String paymentJson) {
        final var timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        int lastBraceIndex = paymentJson.lastIndexOf('}');
        if (lastBraceIndex < 0) {
            throw new IllegalArgumentException("Invalid JSON format: " + paymentJson);
        }
        return paymentJson.substring(0, lastBraceIndex)
                + ",\"requestedAt\":\"" + timestamp + "\""
                + paymentJson.substring(lastBraceIndex);
    }


    public static Payment parsePayment(String json) throws IllegalArgumentException {
        json = json
                .trim()
                .substring(1, json.length() - 1)
                .trim();

        Instant requestedAt = null;
        BigDecimal amount = null;
        String correlationId = null;

        String[] pairs = splitToKeyValuePairs(json);
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex == -1)
                throw new IllegalArgumentException("O par " + pair + " não possui o separador no JSON " + json + ".");


            String key = pair.substring(0, colonIndex).trim();
            String value = pair.substring(colonIndex + 1).trim();

            if (key.startsWith("\"") && key.endsWith("\"")) {
                key = key.substring(1, key.length() - 1);
            }

            switch (key) {
                case "requestedAt":
                    requestedAt = Instant.parse(value.substring(1, value.length() - 1));
                    break;
                case "amount":
                    try {
                        amount = new BigDecimal(value);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid amount format: " + value);
                    }
                    break;
                case "correlationId":
                    correlationId = value.substring(1, value.length() - 1);
                    break;
            }
        }

        return new Payment(requestedAt, amount, correlationId);
    }

    private static String[] splitToKeyValuePairs(String json) {
        final var result = new String[3];
        var count = 0;
        var start = 0;
        var inQuotes = false;

        for (int i = 0; i < json.length(); i++) {
            final var currentChar = json.charAt(i);
            if (currentChar == '"') {
                inQuotes = !inQuotes;
            } else if (currentChar == ',' && !inQuotes) {
                result[count++] = json.substring(start, i).trim();
                start = i + 1;
            }
        }
        // Adiciona o último par
        if (start < json.length()) {
            result[count++] = json.substring(start).trim();
        }

        String[] finalResult = new String[count];
        System.arraycopy(result, 0, finalResult, 0, count);
        return finalResult;
    }

}
