package com.igrium.markchat.chat;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.jetbrains.annotations.Nullable;

import com.igrium.markchat.formatting.TextMarkdownVisitor;
import com.igrium.markchat.util.MarkdownProcessorProvider;

import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MarkdownProcessor implements MessageDecorator {

    private final Parser parser = Parser.builder().build();

    public Parser getParser() {
        return parser;
    }

    @Override
    public Text decorate(@Nullable ServerPlayerEntity player, Text message) {
        return processMarkdown(message.getString());
    }

    public Text processMarkdown(String message) {
        Node document = parser.parse(message);
        // MutableText text = Text.empty();

        // TextMarkdownVisitor visitor = new TextMarkdownVisitor(text);
        // document.accept(visitor);
        TextMarkdownVisitor visitor = new TextMarkdownVisitor();
        document.accept(visitor);

        return visitor.getPages().getFirst();
    }
    
    public static MarkdownProcessor get(MinecraftServer server) {
        return ((MarkdownProcessorProvider) server).getMarkdownProcessor();
    }
}
