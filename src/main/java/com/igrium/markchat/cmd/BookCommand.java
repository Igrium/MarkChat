package com.igrium.markchat.cmd;

import com.igrium.markchat.MarkChat;
import com.igrium.markchat.book.BookGenerator;
import com.igrium.markchat.book.BookLoader;
import com.igrium.markchat.book.URLBookLoader;
import com.igrium.markchat.config.MarkChatConfig;
import com.igrium.markchat.formatting.TextMarkdownVisitor;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

import java.net.MalformedURLException;
import java.net.URL;

public class BookCommand {

    private static final SimpleCommandExceptionType BAD_URL = new SimpleCommandExceptionType(Text.literal("Invalid URL."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        
        if (!MarkChatConfig.getInstance().enableBooks()) return;

        dispatcher.register(literal(MarkChatConfig.getInstance().getCommandPrefix()).then(
            literal("create").then(
                argument("title", StringArgumentType.string()).then(
                    literal("url").then(
                        argument("url", StringArgumentType.string()).executes(BookCommand::createWithUrl)
                    )
                )
            )
        ));
    }

    public static int createWithUrl(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String urlStr = StringArgumentType.getString(context, "url");
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw BAD_URL.create();
        }

        return create(context, new URLBookLoader(url));
    }

    public static int create(CommandContext<ServerCommandSource> context, BookLoader loader) throws CommandSyntaxException {
        BookGenerator generator = new BookGenerator(() -> TextMarkdownVisitor.create(MarkChatConfig.getInstance(),
                context.getSource().hasPermissionLevel(2), true));

        String title = StringArgumentType.getString(context, "title");
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String author = player.getName().getString();

        context.getSource().sendFeedback(() -> Text.literal("Downloading..."), false);

        generator.writeBook(loader, title, author).whenCompleteAsync((stack, e) -> {
            if (e != null) {
                context.getSource().sendError(Text.literal(e.getMessage()));
                MarkChat.LOGGER.error("Unable to generate book.", e);
                return;
            }

            if (requireWritableBook(context.getSource())) {
                int slotId = player.getInventory().selectedSlot;
                ItemStack prevStack = player.getInventory().getStack(slotId);

                if (!prevStack.isOf(Items.WRITABLE_BOOK)) {
                    context.getSource().sendError(Text.literal("Please hold a book and quill."));
                    return;
                }

                player.getInventory().setStack(slotId, stack);
            } else {
                player.giveItemStack(stack);
            }

        }, context.getSource().getServer());

        return 1;
    }
    
    private static boolean requireWritableBook(ServerCommandSource source) {
        return MarkChatConfig.getInstance().isRequireWritableBook();
    }
}
