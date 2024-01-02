package com.igrium.markchat.book;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import com.igrium.markchat.formatting.TextMarkdownVisitor;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

public class BookGenerator {
    private final Parser parser = Parser.builder().build();
    private final Supplier<TextMarkdownVisitor> textGenerator;

    public BookGenerator(Supplier<TextMarkdownVisitor> textGenerator) {
        this.textGenerator = textGenerator;
    }

    public BookGenerator() {
        this(TextMarkdownVisitor::new);
    }

    public CompletableFuture<ItemStack> writeBook(BookLoader loader, String title, String author) {
        // We shouldn't be messing with an existing item off-thread.
        return loader.load().thenApply(
                md -> writeBookMarkdown(new ItemStack(Items.WRITTEN_BOOK), md, title, author));
    }

    public ItemStack writeBookMarkdown(ItemStack stack, String markdown, String title, String author) {
        markdown = markdown.replaceAll("\\R", "\n"); // Normalize line endings.
        TextMarkdownVisitor visitor = textGenerator.get();
        Node document = parser.parse(markdown);
        document.accept(visitor);

        return writeBookNbt(stack, visitor.getPages(), title, author);
    }

    public ItemStack writeBookNbt(ItemStack stack, List<? extends Text> pages, String title, String author) {
        if (!stack.isOf(Items.WRITTEN_BOOK)) return stack;

        stack.setSubNbt("author", NbtString.of(author));
        stack.setSubNbt("title", NbtString.of(title));

        NbtList pageList = new NbtList();
        for (Text page : pages) {
            pageList.add(NbtString.of(Text.Serializer.toJson(page)));
        }

        stack.setSubNbt("pages", pageList);
        return stack;
    }
}
