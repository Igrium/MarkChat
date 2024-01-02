package com.igrium.markchat.config;

import com.google.gson.annotations.JsonAdapter;
import com.igrium.markchat.util.TextColorJsonAdapter;

import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class FormatSettings {

    // For gson
    public FormatSettings() {};

    private String ulPrefix = "• ";

    public String getUlPrefix() {
        return ulPrefix;
    }

    public void setUlPrefix(String ulPrefix) {
        this.ulPrefix = ulPrefix;
    }

    private String olPrefix = "%d. ";

    public String getOlPrefix() {
        return olPrefix;
    }

    public void setOlPrefix(String olPrefix) {
        this.olPrefix = olPrefix;
    }

    private TextColor[] headingColors = new TextColor[] { TextColor.fromFormatting(Formatting.DARK_RED) };

    public TextColor[] getHeadingColors() {
        return headingColors;
    }

    public void setHeadingColors(TextColor[] headingColors) {
        this.headingColors = headingColors;
    }

    public TextColor getHeadingColor(int index) {
        if (headingColors.length == 0) return TextColor.fromFormatting(Formatting.LIGHT_PURPLE);
        return index >= headingColors.length ? headingColors[headingColors.length - 1] : headingColors[index];
    }

    @JsonAdapter(TextColorJsonAdapter.class)
    private TextColor linkColor = TextColor.fromFormatting(Formatting.BLUE);

    public TextColor getLinkColor() {
        return linkColor;
    }

    public void setLinkColor(TextColor linkColor) {
        this.linkColor = linkColor;
    }

    public void copyFrom(FormatSettings other) {
        ulPrefix = other.ulPrefix;
        olPrefix = other.olPrefix;

        headingColors = other.headingColors.clone();
        linkColor = other.linkColor;
    }
}
