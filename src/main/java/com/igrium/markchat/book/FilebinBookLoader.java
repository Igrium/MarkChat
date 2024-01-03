package com.igrium.markchat.book;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.igrium.markchat.filebin.FilebinApi;
import com.igrium.markchat.filebin.FilebinMeta.FilebinFileMeta;

public class FilebinBookLoader implements BookLoader {
    private final String binId;
    private final FilebinApi api;

    public FilebinBookLoader(String binId, FilebinApi api) {
        this.binId = binId;
        this.api = api;
    }

    @Override
    public CompletableFuture<String> load() {
        return api.getBinMeta(binId).thenApply(meta -> {
            if (meta.files == null || meta.files.isEmpty()) {
                throw new IllegalStateException("Bin did not contain any files.");
            }
            for (FilebinFileMeta f : meta.files) {
                if (f.filename.endsWith(".md") && f.contentType.contains("text")) {
                    return f;
                }
            }

            for (FilebinFileMeta f : meta.files) {
                if (f.contentType.contains("text")) {
                    return f;
                }
            }

            throw new IllegalStateException("No eligable files were found.");
        }).thenApplyAsync(meta -> {
            try {
                return URLBookLoader.loadUrl(api.getFile(binId, meta.filename).toURL());
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

}
