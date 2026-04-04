package com.Zorvyn.Finance_Dashboard.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiDocumentationController {

    private final ResourceLoader resourceLoader;

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public String openApiDocument() throws IOException {
        return readClasspathResource("classpath:docs/openapi.json");
    }

    @GetMapping(value = "/swagger-ui/index.html", produces = MediaType.TEXT_HTML_VALUE)
    public String swaggerUi() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>Finance Dashboard API Docs</title>
                  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui.css" />
                  <style>
                    body { margin: 0; background: #faf7f2; }
                  </style>
                </head>
                <body>
                  <div id="swagger-ui"></div>
                  <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                  <script>
                    window.ui = SwaggerUIBundle({
                      url: '/v3/api-docs',
                      dom_id: '#swagger-ui',
                      deepLinking: true,
                      persistAuthorization: true
                    });
                  </script>
                </body>
                </html>
                """;
    }

    private String readClasspathResource(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
