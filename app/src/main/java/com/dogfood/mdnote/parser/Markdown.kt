package com.dogfood.mdnote.parser

/** Inline markdown AST nodes. */
sealed interface Inline {
    data class Text(val value: String) : Inline
    data class Bold(val children: List<Inline>) : Inline
    data class Italic(val children: List<Inline>) : Inline
    data class Code(val value: String) : Inline
}

/** Block-level markdown AST nodes. */
sealed interface Block {
    data class Heading(val level: Int, val inlines: List<Inline>) : Block
    data class Paragraph(val inlines: List<Inline>) : Block
    data class Blockquote(val inlines: List<Inline>) : Block
    data class Bullet(val items: List<List<Inline>>) : Block
    data class CodeBlock(val text: String) : Block
}

/**
 * A small, dependency-free Markdown subset parser supporting headings (#..######), bullet
 * lists (- ), fenced code (```), blockquotes (>), and inline bold (**), italic (*) and code (`).
 * The inline parser is recursive so emphasis can nest.
 */
object Markdown {

    fun parse(source: String): List<Block> {
        val lines = source.replace("\r\n", "\n").split("\n")
        val blocks = mutableListOf<Block>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                line.isBlank() -> i++

                line.startsWith("```") -> {
                    val sb = StringBuilder()
                    i++
                    while (i < lines.size && !lines[i].startsWith("```")) {
                        sb.appendLine(lines[i])
                        i++
                    }
                    if (i < lines.size) i++ // consume closing fence
                    blocks += Block.CodeBlock(sb.toString().trimEnd('\n'))
                }

                line.startsWith("#") -> {
                    val level = line.takeWhile { it == '#' }.length.coerceAtMost(6)
                    val content = line.drop(level).trim()
                    blocks += Block.Heading(level, parseInline(content))
                    i++
                }

                line.trimStart().startsWith(">") -> {
                    val sb = StringBuilder()
                    while (i < lines.size && lines[i].trimStart().startsWith(">")) {
                        val content = lines[i].trimStart().removePrefix(">").trim()
                        if (sb.isNotEmpty()) sb.append(' ')
                        sb.append(content)
                        i++
                    }
                    blocks += Block.Blockquote(parseInline(sb.toString()))
                }

                line.trimStart().startsWith("- ") -> {
                    val items = mutableListOf<List<Inline>>()
                    while (i < lines.size && lines[i].trimStart().startsWith("- ")) {
                        items += parseInline(lines[i].trimStart().removePrefix("- ").trim())
                        i++
                    }
                    blocks += Block.Bullet(items)
                }

                else -> {
                    val sb = StringBuilder()
                    while (i < lines.size && lines[i].isNotBlank() &&
                        !lines[i].startsWith("#") &&
                        !lines[i].startsWith("```") &&
                        !lines[i].trimStart().startsWith(">") &&
                        !lines[i].trimStart().startsWith("- ")
                    ) {
                        if (sb.isNotEmpty()) sb.append(' ')
                        sb.append(lines[i].trim())
                        i++
                    }
                    blocks += Block.Paragraph(parseInline(sb.toString()))
                }
            }
        }
        return blocks
    }

    /** Recursive-descent inline parser over a single string segment. */
    fun parseInline(text: String): List<Inline> {
        val out = mutableListOf<Inline>()
        val buffer = StringBuilder()
        var i = 0

        fun flush() {
            if (buffer.isNotEmpty()) {
                out += Inline.Text(buffer.toString())
                buffer.clear()
            }
        }

        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end >= 0) {
                        flush()
                        out += Inline.Bold(parseInline(text.substring(i + 2, end)))
                        i = end + 2
                    } else {
                        buffer.append(text[i]); i++
                    }
                }

                text[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end >= 0) {
                        flush()
                        out += Inline.Italic(parseInline(text.substring(i + 1, end)))
                        i = end + 1
                    } else {
                        buffer.append(text[i]); i++
                    }
                }

                text[i] == '`' -> {
                    val end = text.indexOf('`', i + 1)
                    if (end >= 0) {
                        flush()
                        out += Inline.Code(text.substring(i + 1, end))
                        i = end + 1
                    } else {
                        buffer.append(text[i]); i++
                    }
                }

                else -> {
                    buffer.append(text[i]); i++
                }
            }
        }
        flush()
        return out
    }
}
