package dev.cemf.rinha_backend_2025.helper;

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
}
