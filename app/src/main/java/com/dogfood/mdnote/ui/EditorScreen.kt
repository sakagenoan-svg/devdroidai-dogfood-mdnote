package com.dogfood.mdnote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogfood.mdnote.parser.Block
import com.dogfood.mdnote.parser.Inline
import com.dogfood.mdnote.parser.Markdown

@Composable
fun EditorScreen() {
    var source by remember {
        mutableStateOf(
            "# Notes\n\nType **markdown** here.\n\n- supports *italic*\n- and `code`\n",
        )
    }
    val blocks = remember(source) { Markdown.parse(source) }

    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("source") },
            minLines = 4,
        )
        Text("preview", style = MaterialTheme.typography.labelMedium)
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            for (block in blocks) {
                BlockView(block)
            }
        }
    }
}

@Composable
private fun BlockView(block: Block) {
    when (block) {
        is Block.Heading -> Text(
            renderInlines(block.inlines),
            fontSize = (24 - (block.level - 1) * 2).sp,
            fontWeight = FontWeight.Bold,
        )

        is Block.Paragraph -> Text(renderInlines(block.inlines))

        is Block.Bullet -> Column {
            for (item in block.items) {
                Text(buildAnnotatedString {
                    append("• ")
                    append(renderInlines(item))
                })
            }
        }

        is Block.CodeBlock -> Text(
            block.text,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
        )

        is Block.Blockquote -> Text(
            renderInlines(block.inlines),
            modifier = Modifier.padding(start = 16.dp),
            fontStyle = FontStyle.Italic,
        )
    }
}

private fun renderInlines(inlines: List<Inline>): AnnotatedString = buildAnnotatedString {
    fun emit(node: Inline) {
        when (node) {
            is Inline.Text -> append(node.value)
            is Inline.Bold -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { node.children.forEach { emit(it) } }
            is Inline.Italic -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { node.children.forEach { emit(it) } }
            is Inline.Code -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) { append(node.value) }
        }
    }
    inlines.forEach { emit(it) }
}
