package gr.demo.clean.upload;

import java.util.Locale;
import java.util.Optional;

/** Validates that an uploaded part is a non-empty, reasonably sized PDF. */
public final class PdfValidator {

    static final int MAX_SIZE_BYTES = 10 * 1024 * 1024;
    private static final byte[] PDF_SIGNATURE = {'%', 'P', 'D', 'F'};

    /** @return an error message, or empty when the part is valid */
    public Optional<String> validate(UploadedPart part) {
        if (!part.getFileName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            return Optional.of("not a .pdf file");
        }
        if (part.size() == 0) {
            return Optional.of("file is empty");
        }
        if (part.size() > MAX_SIZE_BYTES) {
            return Optional.of("file exceeds " + MAX_SIZE_BYTES + " bytes");
        }
        if (!startsWithPdfSignature(part.getContent())) {
            return Optional.of("missing PDF signature");
        }
        return Optional.empty();
    }

    private boolean startsWithPdfSignature(byte[] content) {
        if (content.length < PDF_SIGNATURE.length) {
            return false;
        }
        for (int i = 0; i < PDF_SIGNATURE.length; i++) {
            if (content[i] != PDF_SIGNATURE[i]) {
                return false;
            }
        }
        return true;
    }
}
