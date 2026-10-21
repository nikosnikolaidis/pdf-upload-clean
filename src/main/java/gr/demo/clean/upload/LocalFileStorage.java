package gr.demo.clean.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Stores files on the local disk under a server-generated name. */
public final class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(Path root) {
        this.root = root;
    }

    @Override
    public String store(byte[] content) throws IOException {
        Files.createDirectories(root);
        String id = UUID.randomUUID() + ".pdf";
        Files.write(root.resolve(id), content);
        return id;
    }
}
