package com.igrium.markchat.formatting;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Stack;
import java.util.function.UnaryOperator;

import net.minecraft.text.Style;

/**
 * A stack-like structure that holds text styles.
 */
public class StyleStack extends AbstractList<Style> {
    private final Stack<Style> styles = new Stack<>();

    public StyleStack(Style root) {
        styles.add(root);
    }

    public StyleStack() {
        this(Style.EMPTY);
    }

    public Style push(UnaryOperator<Style> modifier) {
        return styles.push(modifier.apply(styles.peek()));
    }

    public Style peek() {
        return styles.peek();
    }

    public Style pop() {
        if (styles.size() <= 1) {
            throw new IllegalStateException("Cannot pop root style.");
        }
        return styles.pop();
    }

    @Override
    public int size() {
        return styles.size();
    }

    @Override
    public Style get(int index) {
        return styles.get(index);
    }

    @Override
    public Iterator<Style> iterator() {
        return styles.iterator();
    }

    @Override
    public ListIterator<Style> listIterator(int index) {
        return styles.listIterator(index);
    }
}
