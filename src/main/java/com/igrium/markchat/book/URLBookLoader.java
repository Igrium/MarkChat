package com.igrium.markchat.book;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;


import net.minecraft.util.Util;

public class URLBookLoader implements BookLoader {
    public static final int MAX_BYTES = 0x400000; // 4096 kibibytes

    private final URL url;

    public URLBookLoader(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public CompletableFuture<String> load() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadUrl(url);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, Util.getIoWorkerExecutor());
    }

    public static String loadUrl(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.connect();

        if (connection.getContentLength() > MAX_BYTES) {
            throw new IOException("Supplied file is too long.");
        }

        try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
            return new String(in.readAllBytes());
        }
    }
}
