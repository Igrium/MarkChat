package com.igrium.markchat;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Link;
import org.commonmark.node.StrongEmphasis;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class TextMarkdownVisitor extends AbstractVisitor {
    private final MutableText text;
    private Style currentStyle = Style.EMPTY;

    public TextMarkdownVisitor(MutableText text) {
        this.text = text;
    }

    public MutableText getText() {
        return text;
    }

    @Override
    public void visit(org.commonmark.node.Text node) {
        text.append(Text.literal(node.getLiteral()).setStyle(currentStyle));
    }

    @Override
    public void visit(Emphasis node) {
        currentStyle = currentStyle.withItalic(true);
        visitChildren(node);
        currentStyle = currentStyle.withItalic(null);
    }

    @Override
    public void visit(StrongEmphasis node) {
        currentStyle = currentStyle.withBold(true);
        visitChildren(node);
        currentStyle = currentStyle.withBold(null);
    }
    
    @Override
    public void visit(Link node) {
        currentStyle = currentStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, node.getDestination()));
        visitChildren(node);
        currentStyle = currentStyle.withClickEvent(null);
    }
}
