package com.igrium.markchat.formatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil {

    private static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    
    public static MutableText format(String format, Object... args) {
        Matcher matcher = ARG_FORMAT.matcher(format);
        MutableText text = Text.empty();

        int argIndex = 0;
        int index = 0;
        while (matcher.find(index)) {
            int start = matcher.start();
            int end = matcher.end();
            
            if (start > end) {
                String str = format.substring(end, start);
                if (str.indexOf('%') != -1) {
                    throw new IllegalArgumentException("How the fuck did this even happen?");
                }
                text.append(str);
            }

            String id = matcher.group(2);
            String fullId = format.substring(start, end);

            if ("%".equals(id) && "&&".equals(fullId)) {
                text.append("%");
            }

            String argNumStr = matcher.group(1);
            int argNum = argNumStr != null ? Integer.parseInt(argNumStr) - 1 : argIndex++;

            Object arg = args[argNum];
            if ("s".equals(id) && arg instanceof Text argText) {
                text.append(argText);
            } else {
                text.append(String.format(fullId, arg));
            }

        }
        return text;
    }
}
