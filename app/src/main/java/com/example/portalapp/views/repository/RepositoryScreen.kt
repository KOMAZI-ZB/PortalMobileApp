package com.example.portalapp.views.repository

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.BuildConfig
import com.example.portalapp.models.Document
import com.example.portalapp.models.RepositoryLink
import com.example.portalapp.viewmodels.RepositoryViewModel

@Composable
fun RepositoryScreen(
    vm: RepositoryViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current

    Box(Modifier.fillMaxSize()) {
        when {
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.error != null -> ErrorBox(state.error!!, onRetry = vm::refresh)
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "External Repositories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.external.forEach { repo ->
                            ExternalRepoCard(repo) {
                                val uri = Uri.parse(repo.linkUrl)
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(top = 4.dp))

                    Text(
                        "Internal Repository",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (state.internal.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("No internal documents yet.") }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.internal.forEach { doc ->
                                InternalDocRow(
                                    doc,
                                    onDownload = {
                                        val url = buildAbsoluteUrl(doc.fileUrl) // ← fixed
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExternalRepoCard(item: RepositoryLink, onOpen: () -> Unit) {
    ElevatedCard(
        onClick = onOpen,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .width(220.dp)
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Open website ⟶", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun InternalDocRow(doc: Document, onDownload: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(doc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val meta = buildString {
                    if (!doc.uploadedBy.isNullOrBlank()) append("By ${doc.uploadedBy}  •  ")
                    append(doc.uploadedAt.take(19).replace('T', ' '))
                }
                Text(meta, style = MaterialTheme.typography.labelMedium)
            }
            Button(onClick = onDownload) { Text("Download") }
        }
    }
}

@Composable
private fun ErrorBox(msg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(msg, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

private fun buildAbsoluteUrl(url: String): String {
    // If server returned a relative path, prefix with base; if absolute, pass through.
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        val base = BuildConfig.API_BASE_URL // ends with '/'
        if (url.startsWith("/")) base + url.drop(1) else base + url
    }
}
