package gr.demo.clean.upload;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts the file parts of a multipart/form-data body. */
public final class MultipartParser {

    private static final String HEADER_END = "\r\n\r\n";
    private static final String LINE_END = "\r\n";
    private static final String BOUNDARY_KEY = "boundary=";
    private static final Pattern FILE_NAME = Pattern.compile("filename=\"([^\"]*)\"");

    public List<UploadedPart> parse(byte[] body, String boundary) {
        String raw = new String(body, StandardCharsets.ISO_8859_1);
        List<UploadedPart> parts = new ArrayList<>();
        for (String section : raw.split(Pattern.quote("--" + boundary))) {
            parseSection(section).ifPresent(parts::add);
        }
        return parts;
    }

    public Optional<String> extractBoundary(String contentType) {
        if (contentType == null || !contentType.startsWith("multipart/form-data")) {
            return Optional.empty();
        }
        int start = contentType.indexOf(BOUNDARY_KEY);
        if (start < 0) {
            return Optional.empty();
        }
        String boundary = contentType.substring(start + BOUNDARY_KEY.length()).replace("\"", "");
        return boundary.isBlank() ? Optional.empty() : Optional.of(boundary);
    }

    private Optional<UploadedPart> parseSection(String section) {
        int headerEnd = section.indexOf(HEADER_END);
        if (headerEnd < 0) {
            return Optional.empty();
        }
        Matcher name = FILE_NAME.matcher(section.substring(0, headerEnd));
        if (!name.find()) {
            return Optional.empty();
        }
        String content = stripTrailingLineEnd(section.substring(headerEnd + HEADER_END.length()));
        return Optional.of(new UploadedPart(name.group(1), content.getBytes(StandardCharsets.ISO_8859_1)));
    }

    private String stripTrailingLineEnd(String content) {
        return content.endsWith(LINE_END) ? content.substring(0, content.length() - LINE_END.length()) : content;
    }
}
