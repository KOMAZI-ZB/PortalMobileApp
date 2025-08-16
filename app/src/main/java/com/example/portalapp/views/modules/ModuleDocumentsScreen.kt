package com.example.portalapp.views.modules

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.models.Document
import com.example.portalapp.viewmodels.ModuleDocumentsViewModel
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ModuleDocumentsScreen(
    moduleId: Int,
    moduleTitle: String,
    vm: ModuleDocumentsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(moduleId) { vm.load(moduleId) }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = "Module Documents",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
        )
        Text(
            text = moduleTitle,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = LocalContentColor.current.copy(alpha = 0.8f)
        )
        Divider(Modifier.padding(top = 8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            state.documents.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No documents uploaded for this module yet.")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.documents, key = { it.id }) { doc ->
                        DocumentRow(doc)
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentRow(doc: Document) {
    val ctx = LocalContext.current
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    doc.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Uploaded by ${doc.uploadedBy} • ${formatDateTime(doc.uploadedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.8f)
                )
            }
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.fileUrl))
                    ctx.startActivity(intent)
                }
            ) { Text("Download") }
        }
    }
}

@SuppressLint("NewApi") // safe with coreLibraryDesugaring
private fun formatDateTime(iso: String): String = try {
    // Most backend values have no offset: 2025-08-15T18:14:32.5021051
    val ldt = LocalDateTime.parse(iso)
    ldt.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a"))
} catch (_: Throwable) {
    try {
        // Fallback if an offset/Z is present
        val odt = OffsetDateTime.parse(iso)
        odt.format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a"))
    } catch (_: Throwable) {
        iso
    }
}
