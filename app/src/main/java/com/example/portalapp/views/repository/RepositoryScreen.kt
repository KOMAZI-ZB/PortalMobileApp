package com.example.portalapp.views.repository

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.portalapp.BuildConfig
import com.example.portalapp.R
import com.example.portalapp.models.Document
import com.example.portalapp.models.RepositoryLink
import com.example.portalapp.viewmodels.RepositoryViewModel
import androidx.compose.foundation.layout.WindowInsets // for contentWindowInsets

// Masonry (Pinterest) grid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items

// For internal list like ModuleDocuments
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryScreen(
    vm: RepositoryViewModel = hiltViewModel()
) {
    val state by vm.ui.collectAsState()
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // Match Notifications: no internal TopAppBar here and remove default insets
    Scaffold(
        topBar = {},
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Column(Modifier.fillMaxSize()) {

            // ── Tabs bar (edge-to-edge, like Notifications) ──
            val blue = Color(0xFF0D6EFD)
            val lightBlue = Color(0xFFCAF5F6)
            val grey = Color(0xFF6B7280)

            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth(),   // spans full width
                containerColor = lightBlue,
                contentColor = blue,
                indicator = { positions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(positions[selectedTab])
                            .height(2.dp),
                        color = blue
                    )
                },
                divider = {} // no extra divider
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "External",
                            fontWeight = if (selectedTab == 0) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 0) blue else grey
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Internal",
                            fontWeight = if (selectedTab == 1) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == 1) blue else grey
                        )
                    }
                )
            }

            // No spacer here → no gap between TopAppBar and tabs bar

            when (selectedTab) {
                // ── External: Masonry/Pinterest two-column cards (unchanged) ──
                0 -> {
                    if (state.external.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("No external repositories yet.") }
                    } else {
                        ExternalRepoMasonry(
                            items = state.external,
                            onOpen = { repo ->
                                val uri = Uri.parse(repo.linkUrl)
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        )
                    }
                }

                // ── Internal: mirror ModuleDocuments UI ──
                1 -> {
                    if (state.internal.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("No internal documents yet.") }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.internal, key = { it.id }) { doc ->
                                InternalDocRowModuleStyle(
                                    doc = doc,
                                    onDownload = {
                                        val url = buildAbsoluteUrl(doc.fileUrl)
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

/* -------------------- External (unchanged masonry) -------------------- */

@Composable
private fun ExternalRepoMasonry(
    items: List<RepositoryLink>,
    onOpen: (RepositoryLink) -> Unit
) {
    // EXACTLY 2 columns with variable-height tiles
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.label }) { repo ->
            ExternalRepoMasonryCard(repo) { onOpen(repo) }
        }
    }
}

@Composable
private fun ExternalRepoMasonryCard(
    item: RepositoryLink,
    onOpen: () -> Unit
) {
    // Vary the image height for a Pinterest look
    val imageHeight = remember(item.label) { masonryHeightFor(item.label) }

    ElevatedCard(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 30.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            // Top image (kept as in your previous code)
            val imageRes = repoImageFor(item.label)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = item.label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Translucent scrim (kept from your previous version)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x55000000))
                )
            }

            // Bottom content area (white)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Open website ⟶",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun masonryHeightFor(label: String): Dp {
    // Simple deterministic height mix for variety (short/medium/tall)
    val base = 110.dp
    return when ((label.hashCode() and 0x7fffffff) % 3) {
        0 -> base
        1 -> base + 40.dp
        else -> base + 80.dp
    }
}

/* -------------------- Internal (ModuleDocuments-style) -------------------- */

@Composable
private fun InternalDocRowModuleStyle(
    doc: Document,
    onDownload: () -> Unit
) {
    val iconRes = remember(doc) { iconResFor(doc) }
    val dateOnly = remember(doc.uploadedAt) { formatDateOnly(doc.uploadedAt) }
    val sizeLabel = remember(doc) { extractSizeLabelOrNull(doc) }
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

            // Middle: title + meta
            Column(modifier = Modifier.weight(1f)) {
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

            // Right: download
            IconButton(onClick = onDownload) {
                Image(
                    painter = painterResource(id = R.drawable.download),
                    contentDescription = "Download ${doc.title}",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/* -------------------------- Helpers (copied from ModuleDocuments) -------------------------- */

@SuppressLint("NewApi") // safe with coreLibraryDesugaring
private fun formatDateOnly(iso: String): String = try {
    val ldt = LocalDateTime.parse(iso) // e.g. 2025-08-15T18:14:32.5021051
    ldt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
} catch (_: Throwable) {
    try {
        val odt = OffsetDateTime.parse(iso)
        odt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
    } catch (_: Throwable) {
        try {
            val ld = LocalDate.parse(iso)
            ld.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()))
        } catch (_: Throwable) {
            iso
        }
    }
}

/** Map a document to the appropriate drawable based on extension. */
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
    val clean = nameOrUrl.substringBefore('?')
    val lastSegment = clean.substringAfterLast('/')
    val dotIndex = lastSegment.lastIndexOf('.')
    return if (dotIndex != -1 && dotIndex < lastSegment.length - 1) {
        lastSegment.substring(dotIndex + 1)
    } else null
}

/** Try to surface a size label if your model exposes it; otherwise null. */
private fun extractSizeLabelOrNull(doc: Document): String? {
    val byteCount: Long? = tryGetNumberField(doc, "sizeBytes")
        ?: tryGetNumberField(doc, "fileSizeBytes")
        ?: tryGetNumberField(doc, "bytes")
    if (byteCount != null) return humanReadableBytes(byteCount)

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

/* -------------------------- Misc -------------------------- */

private fun buildAbsoluteUrl(url: String): String {
    // If server returned a relative path, prefix with base; if absolute, pass through.
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        val base = BuildConfig.API_BASE_URL // ends with '/'
        if (url.startsWith("/")) base + url.drop(1) else base + url
    }
}

// Map repository label -> drawable image in /res/drawable (external logos)
private fun repoImageFor(label: String): Int {
    val key = label.trim().lowercase()
    return when {
        key == "springerlink" -> R.drawable.springerlink
        key == "scopus" -> R.drawable.scopus
        key == "database" -> R.drawable.database
        key == "archive" -> R.drawable.archive
        key == "jove" -> R.drawable.jove
        key == "proquest" -> R.drawable.proquest
        key == "sciencedirect" -> R.drawable.sciencedirect
        key == "springernature" -> R.drawable.springernature

        // fallbacks
        "springerlink" in key -> R.drawable.springerlink
        "springer" in key && "nature" in key -> R.drawable.springernature
        "scopus" in key -> R.drawable.scopus
        "proquest" in key -> R.drawable.proquest
        "science" in key && "direct" in key -> R.drawable.sciencedirect
        "jove" in key -> R.drawable.jove
        "archive" in key -> R.drawable.archive
        else -> R.drawable.database
    }
}
