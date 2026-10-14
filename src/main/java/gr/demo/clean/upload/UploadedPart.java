package gr.demo.clean.upload;

import java.util.Objects;

/** A file received in a multipart request. */
public final class UploadedPart {

    private final String fileName;
    private final byte[] content;

    public UploadedPart(String fileName, byte[] content) {
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.content = Objects.requireNonNull(content, "content").clone();
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content.clone();
    }

    int size() {
        return content.length;
    }
}
