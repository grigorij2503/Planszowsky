package pl.pointblank.planszowsky.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.data.local.PlayerScore
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.SessionViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    viewModel: SessionViewModel = hiltViewModel(),
    onDiceClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART

    var showChat by remember { mutableStateOf(false) }

    // Simple Timer State
    var elapsedTime by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            while (true) {
                elapsedTime = System.currentTimeMillis() - activeSession!!.startTime
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    if (showChat && activeSession != null) {
        ExpertChatBottomSheet(
            gameTitle = activeSession!!.gameTitle,
            onDismiss = { showChat = false }
        )
    }

    Scaffold(
        modifier = Modifier.then(if (isRetro) Modifier.retroBackground() else Modifier),
        containerColor = if (isRetro) Color.Transparent else MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = (activeSession?.gameTitle ?: stringResource(R.string.active_session)).let { if(isRetro) it.uppercase() else it },
                        style = if(isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = onDiceClick) {
                        Icon(Icons.Default.Casino, contentDescription = "Dice")
                    }
                    IconButton(onClick = { if (activeSession != null) showChat = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Expert")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = if(isRetro) RetroText else MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (activeSession == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_active_session), color = if(isRetro) RetroText else Color.Unspecified)
            }
        } else {
            val session = activeSession!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Timer Card
                SessionTimerCard(elapsedTime, isRetro)

                Spacer(modifier = Modifier.height(24.dp))

                // Players List
                Text(
                    text = stringResource(R.string.players_label).let { if(isRetro) it.uppercase() else it },
                    style = if(isRetro) MaterialTheme.typography.labelLarge.copy(color = RetroGold) else MaterialTheme.typography.labelLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(session.players) { player ->
                        PlayerScoreCard(
                            player = player,
                            isRetro = isRetro,
                            onUpdateScore = { delta -> viewModel.updateScore(player.id, delta) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes Section
                SessionNotesSection(
                    notes = session.notes,
                    isRetro = isRetro,
                    onNotesChange = { viewModel.updateNotes(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.endSession(); onBackClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(isRetro) RetroRed else MaterialTheme.colorScheme.errorContainer,
                        contentColor = if(isRetro) Color.White else MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = if(isRetro) RectangleShape else RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.end_session).let { if(isRetro) it.uppercase() else it })
                }
            }
        }
    }
}

@Composable
fun SessionTimerCard(elapsedTime: Long, isRetro: Boolean) {
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
    val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = if (isRetro) RectangleShape else RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRetro) RetroElementBackground else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = if(isRetro) RetroGold else MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = timeText,
                style = if(isRetro) MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.headlineMedium,
                color = if(isRetro) RetroText else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PlayerScoreCard(
    player: PlayerScore,
    isRetro: Boolean,
    onUpdateScore: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = if (isRetro) RectangleShape else RoundedCornerShape(12.dp),
        color = if (isRetro) RetroElementBackground else MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = if (isRetro) androidx.compose.foundation.BorderStroke(2.dp, RetroBlack) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(player.color), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = player.name.let { if(isRetro) it.uppercase() else it },
                style = if(isRetro) MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = if(isRetro) RetroText else Color.Unspecified
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                ScoreButton("-", isRetro) { onUpdateScore(-1) }
                
                Text(
                    text = player.score.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = if(isRetro) MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.titleLarge,
                    color = if(isRetro) RetroGold else MaterialTheme.colorScheme.primary
                )

                ScoreButton("+", isRetro) { onUpdateScore(1) }
            }
        }
    }
}

@Composable
fun ScoreButton(label: String, isRetro: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .then(if (isRetro) Modifier.background(RetroBlack) else Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if(isRetro) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SessionNotesSection(
    notes: String,
    isRetro: Boolean,
    onNotesChange: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.notes_label).let { if(isRetro) it.uppercase() else it },
            style = if(isRetro) MaterialTheme.typography.labelLarge.copy(color = RetroGold) else MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            placeholder = { Text(stringResource(R.string.tap_to_scribble)) },
            shape = if(isRetro) RectangleShape else RoundedCornerShape(12.dp),
            colors = if(isRetro) OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RetroGold,
                unfocusedBorderColor = RetroText,
                cursorColor = RetroGold
            ) else OutlinedTextFieldDefaults.colors()
        )
    }
}
