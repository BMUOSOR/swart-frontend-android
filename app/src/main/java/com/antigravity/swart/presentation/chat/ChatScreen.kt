package com.antigravity.swart.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.antigravity.swart.domain.model.Message
import com.antigravity.swart.presentation.components.SwartLoadingIndicator
import com.antigravity.swart.presentation.theme.ArtistaGradientStart
import com.antigravity.swart.presentation.theme.InteresadoGradientStart

private val ChatNavyBg = Color(0xFF0B0D17)
private val ChatCardBg = Color(0xFF161925)
private val ChatInputBg = Color(0xFF1E2235)
private val ChatNeonPurple = Color(0xFF8B5CF6)
private val ChatTextGray = Color(0xFF8B8FA8)
private val ChatTextLight = Color(0xFFE8E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: Long,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    // Guarantee the ViewModel uses the chatId from navigation, even if SavedStateHandle
    // wasn't populated before the ViewModel was first created.
    LaunchedEffect(chatId) {
        if (chatId > 0L) viewModel.ensureChatId(chatId)
    }
    val messages by viewModel.messages.collectAsState()
    val otherUserNombre by viewModel.otherUserNombre.collectAsState()
    val otherUserAvatar by viewModel.otherUserAvatar.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Colores de burbuja y botón según el rol del usuario actual
    val isInteresado = viewModel.currentUserRole != "artista"
    val myBubbleColor    = if (isInteresado) InteresadoGradientStart else ArtistaGradientStart
    val otherBubbleColor = if (isInteresado) ArtistaGradientStart    else InteresadoGradientStart
    val sendButtonColor  = myBubbleColor

    var typedMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to the bottom when messages load/change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = ChatNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ChatInputBg)
                        ) {
                            if (!otherUserAvatar.isNullOrBlank()) {
                                AsyncImage(
                                    model = otherUserAvatar,
                                    contentDescription = otherUserNombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = ChatTextGray,
                                    modifier = Modifier.size(24.dp).align(Alignment.Center)
                                )
                            }
                        }
                        
                        // Name
                        Column {
                            Text(
                                text = otherUserNombre,
                                color = ChatTextLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "En línea",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = ChatTextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ChatCardBg,
                    titleContentColor = ChatTextLight
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isLoading) {
                    SwartLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (messages.isEmpty()) {
                    Text(
                        text = "No hay mensajes en esta conversación.\n¡Escribe algo para empezar!",
                        color = ChatTextGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            val isOutgoing = message.idSender == viewModel.currentUserId
                            MessageBubble(
                                message = message,
                                isOutgoing = isOutgoing,
                                outgoingColor = myBubbleColor,
                                incomingColor = otherBubbleColor
                            )
                        }
                    }
                }
            }

            // Input field and send button
            Surface(
                color = ChatCardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = typedMessage,
                        onValueChange = { typedMessage = it },
                        placeholder = { Text("Escribe un mensaje...", color = ChatTextGray) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChatInputBg,
                            unfocusedContainerColor = ChatInputBg,
                            focusedBorderColor = sendButtonColor.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = ChatTextLight,
                            unfocusedTextColor = ChatTextLight
                        ),
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (typedMessage.isNotBlank()) {
                                viewModel.sendMessage(typedMessage)
                                typedMessage = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(sendButtonColor, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isOutgoing: Boolean,
    outgoingColor: Color = ChatNeonPurple,
    incomingColor: Color = ChatCardBg
) {
    val alignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleBgColor = if (isOutgoing) outgoingColor else incomingColor
    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleBgColor)
                .padding(12.dp)
        ) {
            // Artwork Image inquiry
            if (!message.urlImagenObra.isNullOrBlank()) {
                AsyncImage(
                    model = message.urlImagenObra,
                    contentDescription = "Obra interesada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Message text content
            Text(
                text = message.contenido,
                color = ChatTextLight,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Time sent representation
            Text(
                text = formatMessageTime(message.fechaCreacion),
                color = ChatTextGray.copy(alpha = 0.7f),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

fun formatMessageTime(dateTimeStr: String): String {
    return try {
        val parts = dateTimeStr.split(" ")
        if (parts.size >= 2) {
            val timeParts = parts[1].split(":")
            if (timeParts.size >= 2) {
                "${timeParts[0]}:${timeParts[1]}"
            } else parts[1]
        } else {
            dateTimeStr
        }
    } catch (e: Exception) {
        dateTimeStr
    }
}
