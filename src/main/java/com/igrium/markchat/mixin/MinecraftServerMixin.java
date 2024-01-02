package com.igrium.markchat.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.igrium.markchat.chat.MarkdownProcessor;
import com.igrium.markchat.util.MarkdownProcessorProvider;

import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements MarkdownProcessorProvider {

    @Unique
    private MarkdownProcessor processor = new MarkdownProcessor();

    @Inject(method = "getMessageDecorator", at = @At("HEAD"), cancellable = true)
    public void markchat$getMessageDecorator(CallbackInfoReturnable<MessageDecorator> cir) {
        cir.setReturnValue(processor);
    }

    @Override
    public MarkdownProcessor getMarkdownProcessor() {
        return processor;
    }
}
