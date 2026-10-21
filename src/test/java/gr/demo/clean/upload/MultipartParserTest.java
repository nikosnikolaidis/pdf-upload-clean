package gr.demo.clean.upload;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MultipartParserTest {

    private final MultipartParser parser = new MultipartParser();

    private static String filePart(String boundary, String name, String content) {
        return "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"files\"; filename=\"" + name + "\"\r\n"
                + "Content-Type: application/pdf\r\n\r\n"
                + content + "\r\n";
    }

    @Test
    void parsesMultipleFiles() {
        String body = filePart("XYZ", "a.pdf", "%PDF-one") + filePart("XYZ", "b.pdf", "%PDF-two") + "--XYZ--\r\n";

        List<UploadedPart> parts = parser.parse(body.getBytes(StandardCharsets.ISO_8859_1), "XYZ");

        assertEquals(2, parts.size());
        assertEquals("a.pdf", parts.get(0).getFileName());
        assertArrayEquals("%PDF-one".getBytes(StandardCharsets.ISO_8859_1), parts.get(0).getContent());
        assertEquals("b.pdf", parts.get(1).getFileName());
    }

    @Test
    void ignoresFieldsWithoutFilename() {
        String body = "--XYZ\r\nContent-Disposition: form-data; name=\"note\"\r\n\r\nhello\r\n--XYZ--\r\n";

        assertTrue(parser.parse(body.getBytes(StandardCharsets.ISO_8859_1), "XYZ").isEmpty());
    }

    @Test
    void extractsBoundary() {
        assertEquals(Optional.of("abc123"), parser.extractBoundary("multipart/form-data; boundary=abc123"));
        assertEquals(Optional.of("abc123"), parser.extractBoundary("multipart/form-data; boundary=\"abc123\""));
    }

    @Test
    void rejectsNonMultipartContentType() {
        assertTrue(parser.extractBoundary("application/json").isEmpty());
        assertTrue(parser.extractBoundary(null).isEmpty());
        assertTrue(parser.extractBoundary("multipart/form-data").isEmpty());
    }
}
