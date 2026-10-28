package gr.demo.clean.upload;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/** HTTP adapter: translates a multipart POST into an {@link UploadService} call. */
public final class UploadHandler implements HttpHandler {

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;
    private static final int METHOD_NOT_ALLOWED = 405;

    private final MultipartParser parser;
    private final UploadService service;

    public UploadHandler(MultipartParser parser, UploadService service) {
        this.parser = parser;
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            respond(exchange, METHOD_NOT_ALLOWED, "method not allowed");
            return;
        }
        Optional<String> boundary = parser.extractBoundary(exchange.getRequestHeaders().getFirst("Content-Type"));
        if (boundary.isEmpty()) {
            respond(exchange, BAD_REQUEST, "expected multipart/form-data with a boundary");
            return;
        }
        List<UploadedPart> parts = parser.parse(exchange.getRequestBody().readAllBytes(), boundary.get());
        respond(exchange, OK, service.upload(parts).toJson());
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
