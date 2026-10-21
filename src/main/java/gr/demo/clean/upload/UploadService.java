package gr.demo.clean.upload;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Validates and stores a batch of uploaded files; one bad file never blocks the others. */
public final class UploadService {

    private final PdfValidator validator;
    private final FileStorage storage;

    public UploadService(PdfValidator validator, FileStorage storage) {
        this.validator = validator;
        this.storage = storage;
    }

    public UploadResult upload(List<UploadedPart> parts) {
        Map<String, String> stored = new LinkedHashMap<>();
        Map<String, String> rejected = new LinkedHashMap<>();
        for (UploadedPart part : parts) {
            Optional<String> error = validator.validate(part);
            if (error.isPresent()) {
                rejected.put(part.getFileName(), error.get());
            } else {
                storeInto(part, stored, rejected);
            }
        }
        return new UploadResult(stored, rejected);
    }

    private void storeInto(UploadedPart part, Map<String, String> stored, Map<String, String> rejected) {
        try {
            stored.put(part.getFileName(), storage.store(part.getContent()));
        } catch (IOException e) {
            rejected.put(part.getFileName(), "storage failure");
        }
    }
}
