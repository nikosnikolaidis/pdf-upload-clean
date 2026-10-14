package gr.demo.clean.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PdfValidatorTest {

    private final PdfValidator validator = new PdfValidator();

    private static UploadedPart part(String name, String content) {
        return new UploadedPart(name, content.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void acceptsValidPdf() {
        assertTrue(validator.validate(part("a.pdf", "%PDF-1.7 body")).isEmpty());
    }

    @Test
    void acceptsUpperCaseExtension() {
        assertTrue(validator.validate(part("A.PDF", "%PDF-1.7")).isEmpty());
    }

    @Test
    void rejectsWrongExtension() {
        assertEquals(Optional.of("not a .pdf file"), validator.validate(part("a.txt", "%PDF-")));
    }

    @Test
    void rejectsEmptyFile() {
        assertEquals(Optional.of("file is empty"), validator.validate(part("a.pdf", "")));
    }

    @Test
    void rejectsMissingSignature() {
        assertEquals(Optional.of("missing PDF signature"), validator.validate(part("a.pdf", "hello")));
    }

    @Test
    void rejectsTooShortContent() {
        assertEquals(Optional.of("missing PDF signature"), validator.validate(part("a.pdf", "%P")));
    }

    @Test
    void rejectsOversizedFile() {
        byte[] big = new byte[PdfValidator.MAX_SIZE_BYTES + 1];
        big[0] = '%';
        big[1] = 'P';
        big[2] = 'D';
        big[3] = 'F';
        assertTrue(validator.validate(new UploadedPart("a.pdf", big)).get().startsWith("file exceeds"));
    }
}
