package pl.pointblank.planszowsky.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import pl.pointblank.planszowsky.R
import pl.pointblank.planszowsky.domain.model.AppTheme
import pl.pointblank.planszowsky.ui.theme.*
import pl.pointblank.planszowsky.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appTheme: AppTheme = AppTheme.MODERN,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val username by viewModel.bggUsername.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importResult by viewModel.importResult.collectAsState(initial = null)
    val installationId by viewModel.installationId.collectAsState()
    val isRetro = appTheme == AppTheme.PIXEL_ART
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    val context = LocalContext.current

    LaunchedEffect(importResult) {
        importResult?.let { result ->
            val res = context.resources
            val message = when (result) {
                is ProfileViewModel.ImportResult.Success -> {
                    val base = res.getString(R.string.import_success, result.count)
                    if (isRetro) base.uppercase() else base
                }
                is ProfileViewModel.ImportResult.Error -> {
                    val base = res.getString(R.string.import_error)
                    if (isRetro) base.uppercase() else base
                }
            }
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val clipboard = androidx.compose.ui.platform.LocalClipboard.current

    val onCopyId: () -> Unit = {
        installationId?.let { id ->
            coroutineScope.launch {
                val clipData = android.content.ClipData.newPlainText("Installation ID", id)
                clipboard.setClipEntry(androidx.compose.ui.platform.ClipEntry(clipData))
                android.widget.Toast.makeText(context, context.resources.getString(R.string.id_copied), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isRetro) Modifier.retroBackground() else Modifier.background(MaterialTheme.colorScheme.background))
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isRetro) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(RetroElementBackground)
                    .drawBehind { drawRect(RetroBlack, style = Stroke(4.dp.toPx())) },
                contentAlignment = Alignment.Center
            ) {
                PixelProfileIcon(isSelected = true)
            }
        } else {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(40.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.profile_title).let { if(isRetro) it.uppercase() else it },
            style = if (isRetro) MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroGold) 
                    else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Theme Selection Card
        if (isRetro) {
            RetroChunkyBox(
                modifier = Modifier.fillMaxWidth(),
                accentColor = RetroGold
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.theme_selection_title).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThemeButton(
                            text = stringResource(R.string.theme_modern).uppercase(),
                            isSelected = appTheme == AppTheme.MODERN,
                            isRetro = true,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setTheme(AppTheme.MODERN) }
                        )
                        ThemeButton(
                            text = stringResource(R.string.theme_pixel_art).uppercase(),
                            isSelected = appTheme == AppTheme.PIXEL_ART,
                            isRetro = true,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setTheme(AppTheme.PIXEL_ART) }
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.theme_selection_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ThemeButton(
                            text = stringResource(R.string.theme_modern),
                            isSelected = appTheme == AppTheme.MODERN,
                            isRetro = false,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setTheme(AppTheme.MODERN) }
                        )
                        ThemeButton(
                            text = stringResource(R.string.theme_pixel_art),
                            isSelected = appTheme == AppTheme.PIXEL_ART,
                            isRetro = false,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setTheme(AppTheme.PIXEL_ART) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // BGG Import Card
        if (isRetro) {
            RetroChunkyBox(
                modifier = Modifier.fillMaxWidth(),
                accentColor = RetroGrey
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.bgg_import_title).uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = RetroText),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.bgg_import_desc).uppercase(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = RetroText.copy(alpha = 0.7f))
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = { Text(stringResource(R.string.bgg_username_label), color = RetroGold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape,
                        singleLine = true,
                        enabled = !isImporting,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RetroGold,
                            unfocusedBorderColor = RetroText,
                            cursorColor = RetroGold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    RetroSquareButton(
                        text = if(isImporting) stringResource(R.string.wait).uppercase() else stringResource(R.string.import_button_label).uppercase(),
                        color = RetroGreen,
                        onClick = { viewModel.importFromBgg() }
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.bgg_import_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.bgg_import_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.onUsernameChange(it) },
                        label = { Text(stringResource(R.string.bgg_username_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        enabled = !isImporting
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { viewModel.importFromBgg() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isImporting && username.isNotBlank()
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text(stringResource(R.string.import_button))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Firebase Diagnostics Card
        if (isRetro) {
            RetroChunkyBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = installationId != null, onClick = onCopyId),
                accentColor = RetroBlue
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.diagnostics_title).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = RetroBlue, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.installation_id_label, installationId ?: stringResource(R.string.loading)).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, color = RetroText),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = installationId != null, onClick = onCopyId),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.diagnostics_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.installation_id_label, installationId ?: stringResource(R.string.loading)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        val footerStyle = if (isRetro) MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, color = RetroText.copy(alpha = 0.4f))
                          else MaterialTheme.typography.labelSmall
        
        Text(
            text = stringResource(R.string.app_version),
            style = footerStyle,
            color = if (isRetro) RetroText.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.3f)
        )
        Text(
            text = stringResource(R.string.bgg_data_source),
            style = footerStyle,
            color = if (isRetro) RetroText.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.3f)
        )
        Text(
            text = stringResource(R.string.legal_disclaimer),
            style = footerStyle,
            color = if (isRetro) RetroText.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        AsyncImage(
            model = R.drawable.bgg,
            contentDescription = "Powered by BGG",
            modifier = Modifier.height(32.dp),
            contentScale = ContentScale.Fit,
            alpha = if (isRetro) 0.8f else 0.5f,
            filterQuality = if (isRetro) FilterQuality.None else FilterQuality.Low
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ThemeButton(
    text: String,
    isSelected: Boolean,
    isRetro: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (isRetro) {
        Box(
            modifier = modifier
                .height(48.dp)
                .then(if (isSelected) Modifier.drawBehind { drawDitheredShadow(size) } else Modifier)
                .background(if (isSelected) RetroGold else RetroBackground)
                .clickable(onClick = onClick)
                .drawBehind {
                    val stroke = 2.dp.toPx()
                    drawRect(RetroBlack, style = Stroke(stroke * 2f))
                    if (isSelected) {
                        drawRect(Color.White.copy(alpha = 0.3f), Offset(stroke, stroke), Size(size.width - stroke*2, stroke))
                    }
                }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text, 
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace, 
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) RetroBlack else RetroText
                )
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(text = text, fontWeight = FontWeight.Bold)
        }
    }
}
