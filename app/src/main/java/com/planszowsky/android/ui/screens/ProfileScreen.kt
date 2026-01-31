package com.planszowsky.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.planszowsky.android.R
import com.planszowsky.android.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val username by viewModel.bggUsername.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
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
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = stringResource(R.string.app_version),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f)
        )
        Text(
            text = stringResource(R.string.bgg_data_source),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
