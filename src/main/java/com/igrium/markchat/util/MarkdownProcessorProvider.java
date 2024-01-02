package com.igrium.markchat.util;

import com.igrium.markchat.chat.MarkdownProcessor;

public interface MarkdownProcessorProvider {
    MarkdownProcessor getMarkdownProcessor();
}
