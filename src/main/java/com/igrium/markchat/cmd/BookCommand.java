package com.igrium.markchat.cmd;

import com.igrium.markchat.MarkChat;
import com.igrium.markchat.book.BookGenerator;
import com.igrium.markchat.book.BookLoader;
import com.igrium.markchat.book.FilebinBookLoader;
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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class BookCommand {

    private static final SimpleCommandExceptionType BAD_URL = new SimpleCommandExceptionType(Text.literal("Invalid URL."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
            RegistrationEnvironment environment) {
        
        if (!MarkChatConfig.getInstance().enableBooks()) return;

        dispatcher.register(literal(MarkChatConfig.getInstance().getCommandPrefix()).then(
            literal("upload").then(
                argument("title", StringArgumentType.greedyString()).executes(BookCommand::prompt)
            )
        ).then(
            literal("download").then(
                argument("title", StringArgumentType.string()).then(
                    literal("url").then(
                        argument("url", StringArgumentType.string()).executes(BookCommand::createWithUrl)
                    )
                ).then(
                    literal("filebin").then(
                        argument("binId", StringArgumentType.string()).executes(BookCommand::createWithFilebin)
                    )
                )
            )
        ));
    }

    public static int createWithFilebin(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String binId = StringArgumentType.getString(context, "binId");
        return create(context, new FilebinBookLoader(binId, MarkChat.getInstance().getFilebin()));
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

    private static int prompt(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String title = StringArgumentType.getString(context, "title");

        UUID uuid = UUID.randomUUID();
        String uploadUrl = MarkChat.getInstance().getFilebin().getBin(uuid.toString()).toString();

        Text msg = Text.literal("Click ")
                .append(createUploadLink("this link", uploadUrl, "Click to open."))
                .append(" and upload your text file. ")
                .append("\n")
                .append("Once you've uploaded the file, hold a book and quill and click ")
                .append(createConfirmLink("here.", uuid.toString(), title, "Click to confirm upload."));
        
        context.getSource().sendFeedback(() -> msg, false);
        return 1;
    }

    private static Text createUploadLink(String text, String url, String hover) {
        return Text.literal(text).styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover))))
                .formatted(Formatting.GREEN);

    }

    private static Text createConfirmLink(String text, String id, String title, String hover) {
        return Text.literal(text).styled(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/book download \"%s\" filebin \"%s\"".formatted(title, id)))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover))))
                .formatted(Formatting.GREEN);
    }

    private static boolean requireWritableBook(ServerCommandSource source) {
        return MarkChatConfig.getInstance().isRequireWritableBook();
    }
}
