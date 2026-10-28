package gr.demo.clean.upload;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.logging.Logger;

/** Entry point. Configuration comes from the PORT and UPLOAD_DIR environment variables. */
public final class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        Path uploadDir = Path.of(System.getenv().getOrDefault("UPLOAD_DIR", "uploads"));

        UploadService service = new UploadService(new PdfValidator(), new LocalFileStorage(uploadDir));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/upload", new UploadHandler(new MultipartParser(), service));
        server.start();
        LOG.info(() -> "Listening on port " + port);
    }
}
