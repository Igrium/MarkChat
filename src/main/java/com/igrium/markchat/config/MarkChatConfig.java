package com.igrium.markchat.config;

import java.io.Reader;
import java.net.MalformedURLException;

import java.net.URL;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.igrium.markchat.MarkChat;
import com.igrium.markchat.util.TextColorJsonAdapter;

import net.minecraft.text.TextColor;

public class MarkChatConfig {

    /**
     * Shortcut for <code>MarkChat.getInstance().getConfig();</code>
     */
    public static MarkChatConfig getInstance() {
        return MarkChat.getInstance().getConfig();
    }

    public static enum LinkPermissionLevel {
        NEVER, ADMINS, ALWAYS
    }

    private FormatSettings formatting = new FormatSettings();

    public FormatSettings getFormatting() {
        return formatting;
    }

    public void setFormatting(FormatSettings formatting) {
        this.formatting = Objects.requireNonNull(formatting);
    }

    private LinkPermissionLevel allowLinks = LinkPermissionLevel.ADMINS;

    public LinkPermissionLevel getAllowLinks() {
        return allowLinks;
    }
    
    public void setAllowLinks(LinkPermissionLevel allowLinks) {
        this.allowLinks = allowLinks;
    }

    private boolean enableBooks = true;

    public boolean enableBooks() {
        return enableBooks;
    }

    public void setEnableBooks(boolean enableBooks) {
        this.enableBooks = enableBooks;
    }
    
    private boolean requireWritableBook = true;

    public boolean isRequireWritableBook() {
        return requireWritableBook;
    }

    public void setRequireWritableBook(boolean requireWritableBook) {
        this.requireWritableBook = requireWritableBook;
    }

    private URL filebinUrl = getUrlUnchecked("https://filebin.net");

    public URL getFilebinUrl() {
        return filebinUrl;
    }

    public void setFilebinUrl(URL filebinUrl) {
        this.filebinUrl = Objects.requireNonNull(filebinUrl);
    }
    
    private String commandPrefix = "book";

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.commandPrefix = Objects.requireNonNull(commandPrefix);
    }

    public void copyFrom(MarkChatConfig other) {
        this.formatting.copyFrom(other.getFormatting());
        this.allowLinks = other.allowLinks;
        this.requireWritableBook = other.requireWritableBook;
        this.filebinUrl = other.filebinUrl;
    }

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(TextColor.class, new TextColorJsonAdapter())
            .create();
    
    public String toJson() {
        return GSON.toJson(this);
    }

    public static MarkChatConfig fromJson(String json) {
        return GSON.fromJson(json, MarkChatConfig.class);
    }

    public static MarkChatConfig fromJson(Reader reader) {
        return GSON.fromJson(reader, MarkChatConfig.class);
    }

    public static MarkChatConfig fromJson(JsonReader reader) {
        return GSON.fromJson(reader, MarkChatConfig.class);
    }

    private static URL getUrlUnchecked(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
