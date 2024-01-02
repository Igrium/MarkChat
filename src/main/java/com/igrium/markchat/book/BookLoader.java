package com.igrium.markchat.book;

import java.util.concurrent.CompletableFuture;

/**
 * Loads the contents of a book from an external source.
 */
public interface BookLoader {
    
    /**
     * Load the book's contents.
     * @return Raw markdown contents.
     */
    public CompletableFuture<String> load();

}
