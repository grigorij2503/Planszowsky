package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.ChatMessage
import pl.pointblank.planszowsky.ui.viewmodel.ExpertViewModel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpertChatBottomSheet(
    gameTitle: String,
    onDismiss: () -> Unit,
    viewModel: ExpertViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART
    
    // Initialize session when opened
    LaunchedEffect(gameTitle) {
        viewModel.initialize(gameTitle)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (isRetro) RetroBackground else MaterialTheme.colorScheme.surface,
        shape = if (isRetro) RectangleShape else RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { 
            if (isRetro) {
                Box(modifier = Modifier.padding(16.dp).size(40.dp, 8.dp).background(RetroBlack))
            } else {
                BottomSheetDefaults.DragHandle()
            }
        },
        tonalElevation = if (isRetro) 0.dp else BottomSheetDefaults.Elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
                .then(if (isRetro) Modifier.drawBehind { 
                    val stroke = 4.dp.toPx()
                    drawRect(RetroBlack, style = Stroke(stroke))
                } else Modifier)
        ) {
            // Header
            Text(
                text = stringResource(R.string.expert_chat_title, gameTitle).let { if(isRetro) it.uppercase() else it },
                style = if (isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.ExtraBold, color = RetroText)
                        else MaterialTheme.typography.titleLarge,
                color = if (isRetro) RetroText else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
            )
            
            if (!isRetro) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(RetroBlack))
            }

            // Chat History
            val listState = rememberLazyListState()
            
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message, isRetro)
                }
                
                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            if (isRetro) {
                                Text("...", style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText))
                            } else {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            // Input Area
            var inputText by remember { mutableStateOf("") }
            val focusManager = LocalFocusManager.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isRetro) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(RetroElementBackground)
                            .drawBehind {
                                drawRect(RetroBlack, style = Stroke(3.dp.toPx()))
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                    focusManager.clearFocus()
                                }
                            }),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (inputText.isEmpty()) {
                                        Text(
                                            text = "ASK EXPERT...",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText.copy(alpha = 0.5f))
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    RetroSquareIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        },
                        color = RetroBlue
                    ) {
                        PixelSendIcon()
                    }
                } else {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text(stringResource(R.string.expert_hint)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        })
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                                focusManager.clearFocus()
                            }
                        },
                        enabled = !isLoading && inputText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Wyślij")
                    }
                }
            }
            
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isRetro: Boolean) {
    val bubbleColor = if (isRetro) {
        if (message.isError) RetroRed else if (message.isUser) RetroBlue else RetroElementBackground
    } else {
        if (message.isError) MaterialTheme.colorScheme.errorContainer
        else if (message.isUser) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = if (isRetro) {
        RetroText
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val shape = if (isRetro) RectangleShape else {
        if (message.isUser) RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        else RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (isRetro) {
            RetroChunkyBox(
                modifier = Modifier.widthIn(max = 280.dp),
                backgroundColor = bubbleColor,
                accentColor = if (message.isUser) RetroGold else RetroBlack
            ) {
                MarkdownText(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = textColor),
                    isRetro = true
                )
            }
        } else {
            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 2.dp,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                MarkdownText(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        val labelText = if (message.isUser) stringResource(R.string.expert_label_user) 
                        else if (message.isError) stringResource(R.string.expert_label_system) 
                        else stringResource(R.string.expert_label_ai)

        Text(
            text = if (isRetro) labelText.uppercase() else labelText,
            style = if (isRetro) MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = RetroText)
                    else MaterialTheme.typography.labelSmall,
            color = if (isRetro) RetroText else Color.Gray,
            modifier = Modifier.padding(top = 4.dp, start = if (message.isUser) 0.dp else 8.dp, end = if (message.isUser) 8.dp else 0.dp)
        )
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    isRetro: Boolean = false
) {
    val annotatedString = remember(text) {
        parseMarkdown(text)
    }
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style
    )
}

fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val boldRegex = Regex("""\*\*(.*?)\*\*""")
        val italicRegex = Regex("""\*(.*?)\*""")
        
        var currentText = text
        val boldMatches = boldRegex.findAll(currentText).toList()
        
        var lastIndex = 0
        for (match in boldMatches) {
            append(currentText.substring(lastIndex, match.range.first))
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                // Nested italic check
                val content = match.groupValues[1]
                val nestedItalic = italicRegex.find(content)
                if (nestedItalic != null) {
                    append(content.substring(0, nestedItalic.range.first))
                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(nestedItalic.groupValues[1])
                    }
                    append(content.substring(nestedItalic.range.last + 1))
                } else {
                    append(content)
                }
            }
            lastIndex = match.range.last + 1
        }
        val remainingText = currentText.substring(lastIndex)
        
        // Final pass for italics in remaining text
        val finalItalicMatches = italicRegex.findAll(remainingText).toList()
        var finalLastIndex = 0
        for (match in finalItalicMatches) {
            append(remainingText.substring(finalLastIndex, match.range.first))
            withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                append(match.groupValues[1])
            }
            finalLastIndex = match.range.last + 1
        }
        append(remainingText.substring(finalLastIndex))
    }
}
