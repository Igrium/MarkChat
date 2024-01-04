# MarkChat

## About

When I started my SMP, I noticed that I was consistently using \*markdown syntax* to write chat messages. Although Minecraft doesn't format chat messages sent by players, the client *does* have the functionality to *display* formatted chat messages, should it receive them from the server.

This mod parses chat messages as [Markdown](https://commonmark.org/help/), formats them, and sends the formatted version to clients. This allows players to write in **bold** and *italics* like a boss, with the same syntax used by Discord and various other messaging apps. It also prevents chat reporting, so that's nice, I guess.

## Books

In addition to chat formatting, MarkChat includes an additional module that allows markdown files to be uploaded as written books. To use this, hold a book and quill and type `/book upload <title>`. This will prompt you to upload a markdown file to Filebin, where it will be downloaded by the server. You can also user `/book download <title> (url|filebin) <address>` to download a book directly. Keep in mind that written books permit a small amount of characters per page, and although page breaks are automatically inserted, it's best to keep paragraphs brief.

This module can be disabled in the settings.

## Config

`config/markchat.json`

- `formatting` (object): Various settings regarding format style.
  
  - `ulPrefix` (string): Prefix to use for unordered (bullet) lists.
  
  - `olPrefix` (string): Prefix to use for ordered (numbered) lists. Use `%d` to indicate list index.
  
  - `headingColors` (list): A list of strings, formatted the same way as `linkColor`, indicating the colors to use for each heading tag. Heading tags with an index greater than the length of the list will use the last element in the list.
  
  - `linkColor` (string): The color to use for links. May be one of Minecraft's [color names](https://minecraft.wiki/w/Formatting_codes#Color_codes) or a hex code.

- `allowLinks` (string): Whether links should be allowed in chat and books. One of `NEVER`, `ADMINS` or `ALWAYS`.

- `enableBooks` (boolean): Whether the book upload module should be enabled. If false, this mod's functionality will be restricted to chat only.

- `filebinUrl` (string): The base URL of [Filebin](https://filebin.net/about), used for uploading markdown files.

- `commandPrefix` (string): The prefix of the `book` command. Change this if `/book` conflicts with another mod.

## Supported Markdown Elements

The current implementation uses the [CommonMark specification](https://commonmark.org/).

| Element         | Support                                 |
| --------------- | --------------------------------------- |
| Headings        | Only in books                           |
| Bold            | Yes                                     |
| Italic          | Yes                                     |
| Blockquote      | Only in books                           |
| Code            | No                                      |
| Horizontal rule | Only in books (becomes new page)        |
| Link            | Only if enabled (see [config](#config)) |
| Image           | No                                      |
| Strikethrough   | Planned                                 |