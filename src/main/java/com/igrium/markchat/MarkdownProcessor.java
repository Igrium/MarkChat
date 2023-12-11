package com.igrium.markchat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class MarkdownProcessor implements MessageDecorator {

    @Override
    public Text decorate(@Nullable ServerPlayerEntity player, Text message) {
        MutableText base = Text.literal("You said: ");
        return base.append(message);
    }
    
}
