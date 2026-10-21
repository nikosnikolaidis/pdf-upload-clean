package gr.demo.clean.upload;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Outcome of an upload: stored files (original name to storage id) and rejected files (name to reason). */
public final class UploadResult {

    private final Map<String, String> stored;
    private final Map<String, String> rejected;

    UploadResult(Map<String, String> stored, Map<String, String> rejected) {
        this.stored = Collections.unmodifiableMap(new LinkedHashMap<>(stored));
        this.rejected = Collections.unmodifiableMap(new LinkedHashMap<>(rejected));
    }

    public Map<String, String> getStored() {
        return stored;
    }

    public Map<String, String> getRejected() {
        return rejected;
    }

    public String toJson() {
        return "{\"stored\":" + toJsonObject(stored) + ",\"rejected\":" + toJsonObject(rejected) + "}";
    }

    private static String toJsonObject(Map<String, String> map) {
        List<String> entries = map.entrySet().stream()
                .map(e -> quote(e.getKey()) + ":" + quote(e.getValue()))
                .collect(Collectors.toList());
        return "{" + String.join(",", entries) + "}";
    }

    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
