package com.example.portalapp.views.modules

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.R
import com.example.portalapp.models.Document
import com.example.portalapp.viewmodels.ModuleDocumentsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ModuleDocumentsScreen(
    moduleId: Int,
    moduleTitle: String,
    vm: ModuleDocumentsViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()

    LaunchedEffect(moduleId) { vm.load(moduleId) }

    // Match Notifications/Modules light-blue header bar
    val lightBlue = Color(0xFFCAF5F6)

    // Show only the module code (first token from the title)
    val moduleCode = remember(moduleTitle) { moduleTitle.trim().takeWhile { !it.isWhitespace() } }

    Column(Modifier.fillMaxSize()) {
        // Light blue header bar with centered, black module code
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(lightBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = moduleCode,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            )
        }

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
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,      // spacing from the blue bar
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

    // Determine the best icon based on file extension
    val iconRes = remember(doc) { iconResFor(doc) }

    // Build the secondary line: date only, optionally " • size" if we can detect it
    val dateOnly = remember(doc.uploadedAt) { formatDateOnly(doc.uploadedAt) }
    val sizeLabel = remember(doc) { extractSizeLabelOrNull(doc) } // null if unknown
    val metaLine = remember(dateOnly, sizeLabel) {
        if (sizeLabel.isNullOrBlank()) "Posted on: $dateOnly" else "Posted on: $dateOnly • $sizeLabel"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: file-type icon
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .padding(end = 10.dp)
            )

            // Middle: title + date/size
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = metaLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalContentColor.current.copy(alpha = 0.8f)
                )
            }

            // Right: download image icon button
            IconButton(
                onClick = {
                    // Keep existing behavior: open the file URL so it downloads/opens
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(doc.fileUrl))
                    ctx.startActivity(intent)
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.download),
                    contentDescription = "Download ${doc.title}",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/* -------------------------- Helpers -------------------------- */

@SuppressLint("NewApi") // safe with coreLibraryDesugaring
private fun formatDateOnly(iso: String): String = try {
    // Most backend values have no offset: 2025-08-15T18:14:32.5021051
    val ldt = LocalDateTime.parse(iso)
    ldt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
} catch (_: Throwable) {
    try {
        // Fallback if an offset/Z is present
        val odt = OffsetDateTime.parse(iso)
        odt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
    } catch (_: Throwable) {
        try {
            // If it's already a date
            val ld = LocalDate.parse(iso)
            ld.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
        } catch (_: Throwable) {
            iso // as-is if we can't parse
        }
    }
}

/**
 * Map a document to the appropriate drawable based on extension.
 * Falls back to 'folderfile' if unknown.
 */
private fun iconResFor(doc: Document): Int {
    val fromUrl = extFrom(doc.fileUrl)
    val fromTitle = extFrom(doc.title)

    val ext = (fromUrl ?: fromTitle)?.lowercase(Locale.getDefault()) ?: ""

    return when (ext) {
        "pdf" -> R.drawable.pdffile

        "xls", "xlsx", "csv" -> R.drawable.excelfile

        "ppt", "pptx" -> R.drawable.pptfile

        "doc", "docx" -> R.drawable.docxfile

        "txt" -> R.drawable.txtfile

        "png", "jpg", "jpeg", "gif", "bmp", "webp", "heic" -> R.drawable.imgfile

        else -> R.drawable.folderfile
    }
}

private fun extFrom(nameOrUrl: String?): String? {
    if (nameOrUrl.isNullOrBlank()) return null
    // strip query params
    val clean = nameOrUrl.substringBefore('?')
    val lastSegment = clean.substringAfterLast('/')
    val dotIndex = lastSegment.lastIndexOf('.')
    return if (dotIndex != -1 && dotIndex < lastSegment.length - 1) {
        lastSegment.substring(dotIndex + 1)
    } else null
}

/**
 * If your backend/model provides a size field (e.g., sizeBytes / fileSizeBytes),
 * we'll display it; otherwise we show only the date.
 * Uses reflection so we don't break builds if the property doesn't exist.
 */
private fun extractSizeLabelOrNull(doc: Document): String? {
    // Try common numeric fields first
    val byteCount: Long? = tryGetNumberField(doc, "sizeBytes")
        ?: tryGetNumberField(doc, "fileSizeBytes")
        ?: tryGetNumberField(doc, "bytes")
    if (byteCount != null) return humanReadableBytes(byteCount)

    // Try a ready-made label if present (String)
    val label: String? = tryGetStringField(doc, "sizeLabel")
        ?: tryGetStringField(doc, "fileSize")
        ?: tryGetStringField(doc, "size")
    return label?.takeIf { it.isNotBlank() }
}

private fun tryGetNumberField(target: Any, fieldName: String): Long? = runCatching {
    val f = target.javaClass.getDeclaredField(fieldName)
    f.isAccessible = true
    (f.get(target) as? Number)?.toLong()
}.getOrNull()

private fun tryGetStringField(target: Any, fieldName: String): String? = runCatching {
    val f = target.javaClass.getDeclaredField(fieldName)
    f.isAccessible = true
    f.get(target) as? String
}.getOrNull()

private fun humanReadableBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.getDefault(), "%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format(Locale.getDefault(), "%.1f GB", gb)
}
