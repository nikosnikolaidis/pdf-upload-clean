package gr.demo.clean.upload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UploadServiceTest {

    @TempDir
    Path tempDir;

    private static UploadedPart part(String name, String content) {
        return new UploadedPart(name, content.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    void storesValidFilesAndRejectsInvalidOnes() throws IOException {
        UploadService service = new UploadService(new PdfValidator(), new LocalFileStorage(tempDir));

        UploadResult result = service.upload(Arrays.asList(
                part("good.pdf", "%PDF-1.7"),
                part("bad.txt", "text"),
                part("other.pdf", "%PDF-2.0")));

        assertEquals(2, result.getStored().size());
        assertEquals(1, result.getRejected().size());
        assertEquals("not a .pdf file", result.getRejected().get("bad.txt"));
        try (var files = Files.list(tempDir)) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void neverUsesTheClientFileNameOnDisk() throws IOException {
        UploadService service = new UploadService(new PdfValidator(), new LocalFileStorage(tempDir));

        UploadResult result = service.upload(Arrays.asList(part("../../evil.pdf", "%PDF-1.7")));

        String id = result.getStored().get("../../evil.pdf");
        assertTrue(Files.exists(tempDir.resolve(id)));
        assertTrue(!id.contains("evil") && !id.contains(".."));
    }

    @Test
    void reportsStorageFailureWithoutAbortingTheBatch() {
        FileStorage failing = content -> {
            throw new IOException("disk full");
        };
        UploadService service = new UploadService(new PdfValidator(), failing);

        UploadResult result = service.upload(Arrays.asList(part("a.pdf", "%PDF-1.7")));

        assertTrue(result.getStored().isEmpty());
        assertEquals("storage failure", result.getRejected().get("a.pdf"));
    }

    @Test
    void serialisesResultAsJson() {
        UploadService service = new UploadService(new PdfValidator(), content -> "id-1.pdf");

        String json = service.upload(Arrays.asList(part("a.pdf", "%PDF-1.7"), part("b.txt", "x"))).toJson();

        assertEquals("{\"stored\":{\"a.pdf\":\"id-1.pdf\"},\"rejected\":{\"b.txt\":\"not a .pdf file\"}}", json);
    }
}
