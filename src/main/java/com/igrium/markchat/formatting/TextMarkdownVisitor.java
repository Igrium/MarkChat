package com.igrium.markchat.formatting;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.BulletList;
import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Heading;
import org.commonmark.node.Link;
import org.commonmark.node.ListItem;
import org.commonmark.node.OrderedList;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.ThematicBreak;

import com.igrium.markchat.config.FormatSettings;
import com.igrium.markchat.config.MarkChatConfig;
import com.igrium.markchat.util.StringUtils;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextMarkdownVisitor extends AbstractVisitor {
    
    private static class ListWriter {
        ListWriter(String prefix) {
            this.prefix = prefix;
        }
        
        final String prefix;
        int index = 1;
    }

    private final LinkedList<MutableText> pages = new LinkedList<>();
    public TextMarkdownVisitor() {
        pages.add(Text.empty());
    }

    private FormatSettings formatSettings = new FormatSettings();
    
    public FormatSettings getFormatSettings() {
        return formatSettings;
    }

    public void setFormatSettings(FormatSettings formatSettings) {
        this.formatSettings = Objects.requireNonNull(formatSettings);
    }

    private final StyleStack styles = new StyleStack();
    private Stack<ListWriter> lists = new Stack<>();

    private boolean allowLinks = false;

    public boolean allowLinks() {
        return allowLinks;
    }

    public void setAllowLinks(boolean allowLinks) {
        this.allowLinks = allowLinks;
    }

    // SPLIT PAGES
    private boolean splitPages;

    public boolean isSplitPages() {
        return splitPages;
    }

    public void setSplitPages(boolean splitPages) {
        this.splitPages = splitPages;
    }

    private int pageWidth = 20;

    public int getPageWidth() {
        return pageWidth;
    }
    public void setPageWidth(int pageWidth) {
        if (pageWidth <= 0) {
            throw new IllegalArgumentException("Page width must be positive.");
        }
        this.pageWidth = pageWidth;
    }

    private int pageHeight = 14;

    public int getPageHeight() {
        return pageHeight;
    }
    
    public void setPageHeight(int pageHeight) {
        if (pageHeight <= 0) {
            throw new IllegalArgumentException("Page height must be positive.");
        }
        this.pageHeight = pageHeight;
    }

    private int currentLine = 0;

    public void appendLiteral(String literal) {
        appendLiteral(literal, Style.EMPTY);
    }

    public void appendLiteral(String literal, Style style) {
        if (literal.isEmpty()) return;
        String[] strPages;

        if (splitPages) {
            int[] pageBreaks = StringUtils.identifyPageBreaks(literal, pageWidth, pageHeight, currentLine, true);
            strPages = StringUtils.splitString(literal, pageBreaks);
        } else {
            strPages = new String[] { literal };
        }
        if (strPages.length == 0) return;

        for (int i = 0; i < strPages.length; i++) {
            String pageContents = strPages[i];
            pages.getLast().append(Text.literal(pageContents).setStyle(style));

            // If there's another page to add.
            if (i + 1 < strPages.length) {
                pages.add(Text.empty());
            }
        }

        // We went onto a new page, so the current line is reset.
        if (strPages.length > 1) {
            currentLine = 0;
        }

        currentLine += StringUtils.identifyLineBreaks(strPages[strPages.length - 1], pageWidth, false).length;
    }

    @Override
    public void visit(org.commonmark.node.Text text) {
        appendLiteral(text.getLiteral(), styles.peek());
    }

    @Override
    public void visit(Code code) {
        appendLiteral(code.getLiteral(), Style.EMPTY.withColor(Formatting.DARK_GRAY));
    }

    @Override
    public void visit(Emphasis emphasis) {
        styles.push(style -> style.withItalic(true));
        visitChildren(emphasis);
        styles.pop();
    }

    @Override
    public void visit(StrongEmphasis strongEmphasis) {
        styles.push(style -> style.withBold(true));
        visitChildren(strongEmphasis);
        styles.pop();
    }

    @Override
    public void visit(Link link) {
        if (allowLinks()) {
            styles.push(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link.getDestination()))
                    .withColor(formatSettings.getLinkColor())
                    .withUnderline(true)
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(link.getDestination()))));
        } else {
            styles.push(style -> style);
        }

        visitChildren(link);
        styles.pop();
    }

    @Override
    public void visit(SoftLineBreak softLineBreak) {
        appendLiteral("\n");
    }

    @Override
    public void visit(HardLineBreak hardLineBreak) {
        appendLiteral("\n");
    }
    
    @Override
    public void visit(Paragraph paragraph) {
        visitChildren(paragraph);
        if (splitPages)
            appendLiteral("\n\n", Style.EMPTY);
    }

    @Override
    public void visit(ListItem listItem) {
        if (lists.isEmpty()) {
            visitChildren(listItem);
            return;
        }

        ListWriter currentList = lists.peek();
        appendLiteral(currentList.prefix.formatted(currentList.index), Style.EMPTY);
        visitChildren(listItem);
        currentList.index++;
    }

    @Override
    public void visit(OrderedList orderedList) {
        lists.push(new ListWriter(formatSettings.getOlPrefix()));
        visitChildren(orderedList);
        lists.pop();
    }

    @Override
    public void visit(BulletList bulletList) {
        lists.push(new ListWriter(formatSettings.getUlPrefix()));
        visitChildren(bulletList);
        lists.pop();
    }

    @Override
    public void visit(Heading heading) {
        styles.push(style -> style.withBold(true)
                .withColor(formatSettings.getHeadingColor(heading.getLevel())));
        visitChildren(heading);
        styles.pop();
        if (splitPages)
            appendLiteral("\n\n", Style.EMPTY);
    }
    
    @Override
    public void visit(ThematicBreak thematicBreak) {
        if (splitPages) {
            pages.add(Text.empty());
        }
    }

    public LinkedList<MutableText> getPages() {
        return pages;
    }

    public static TextMarkdownVisitor create(MarkChatConfig config, boolean isAdmin, boolean splitPages) {
        TextMarkdownVisitor visitor = new TextMarkdownVisitor();
        switch (config.getAllowLinks()) {
            case ALWAYS:
                visitor.setAllowLinks(true);
                break;
            case ADMINS:
                visitor.setAllowLinks(isAdmin);
                break;
            case NEVER:
                visitor.setAllowLinks(false);
                break;
        }

        visitor.setFormatSettings(config.getFormatting());
        visitor.setSplitPages(splitPages);

        return visitor;
    }
}
