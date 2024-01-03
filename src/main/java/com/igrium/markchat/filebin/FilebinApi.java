package com.igrium.markchat.filebin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.util.Util;

public class FilebinApi {
    private final URI url;
    private final HttpClient client;

    private final Gson gson = new GsonBuilder().create();

    public FilebinApi(URI url) {
        this.url = url;
        client = HttpClient.newBuilder()
                .followRedirects(Redirect.ALWAYS)
                .executor(Util.getIoWorkerExecutor())
                .build();
    }

    public FilebinApi(String url) throws URISyntaxException {
        this(new URI(url));
    }

    public HttpClient getClient() {
        return client;
    }

    public URI getUrl() {
        return url;
    }

    public URI getFile(String bin, String filename) {
        return getUrl().resolve(bin + "/" + filename);
    }

    public URI getBin(String bin) {
        return getUrl().resolve(bin);
    }

    public CompletableFuture<FilebinMeta> getBinMeta(String bin) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(getBin(bin))
            .GET()
            .header("accept", "application/json")
            .build();
        
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(res -> {
            if (res.statusCode() >= 400) {
                throw new CompletionException(new IOException("Recieved HTTP status code " + res.statusCode()));
            }
            return gson.fromJson(res.body(), FilebinMeta.class);
        });
    }
}
