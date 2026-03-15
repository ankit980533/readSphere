package com.novelplatform.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.novelplatform.app.viewmodel.NovelViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(chapterId: Long?, navController: NavController, viewModel: NovelViewModel = viewModel()) {
    val chapter by viewModel.currentChapter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Follow system dark mode by default
    val systemDarkMode = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Reader Settings
    var fontSize by remember { mutableStateOf(18f) }
    var lineSpacing by remember { mutableStateOf(1.8f) }
    var showControls by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(systemDarkMode) }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Calculate reading progress
    val readingProgress by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) 0f
            else {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                (lastVisibleItem + 1).toFloat() / totalItems.toFloat()
            }
        }
    }
    
    LaunchedEffect(chapterId) {
        chapterId?.let { viewModel.loadChapter(it) }
    }
    
    // Theme colors based on mode
    val backgroundColor = if (isDarkMode) Color(0xFF1A1A1A) else Color(0xFFFAF8F5)
    val textColor = if (isDarkMode) Color(0xFFE8E8E8) else Color(0xFF2D2D2D)
    val secondaryTextColor = if (isDarkMode) Color(0xFF888888) else Color(0xFF666666)
    val surfaceColor = if (isDarkMode) Color(0xFF252525) else Color.White
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading chapter...", color = secondaryTextColor)
                }
            }
        } else {
            chapter?.let { ch ->
                // Main Content
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = if (showControls) 80.dp else 24.dp,
                        bottom = if (showControls) 100.dp else 40.dp,
                        start = 24.dp,
                        end = 24.dp
                    )
                ) {
                    // Chapter Header
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Chapter Number Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    "CHAPTER ${ch.chapterNumber}",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        letterSpacing = 3.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Chapter Title
                            Text(
                                ch.title,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Reading Info
                            ch.wordCount?.let { words ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InfoChip(
                                        icon = Icons.Outlined.Schedule,
                                        text = "${words / 200} min read",
                                        color = secondaryTextColor
                                    )
                                    InfoChip(
                                        icon = Icons.Outlined.TextFields,
                                        text = "$words words",
                                        color = secondaryTextColor
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Decorative Divider
                            Row(
                                modifier = Modifier.width(120.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = secondaryTextColor.copy(alpha = 0.3f)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                )
                                Divider(
                                    modifier = Modifier.weight(1f),
                                    color = secondaryTextColor.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                    
                    // Chapter Content - Split into paragraphs
                    val paragraphs = formatChapterContent(ch.content ?: "")
                    
                    // Skip the first paragraph if it's just the chapter heading (already shown above)
                    val contentParagraphs = paragraphs.filterIndexed { index, text ->
                        !(index == 0 && isChapterHeading(text))
                    }
                    
                    contentParagraphs.forEachIndexed { index, paragraph ->
                        item {
                            if (paragraph.isNotBlank()) {
                                val isHeading = isChapterHeading(paragraph)
                                
                                if (isHeading) {
                                    // Skip duplicate chapter headings in content
                                    if (!paragraph.uppercase().contains("CHAPTER")) {
                                        Text(
                                            text = paragraph,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif,
                                                letterSpacing = 2.sp
                                            ),
                                            color = textColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(
                                                ParagraphStyle(
                                                    textIndent = TextIndent(firstLine = 32.sp),
                                                    lineHeight = (fontSize * lineSpacing).sp
                                                )
                                            ) {
                                                withStyle(SpanStyle(fontSize = fontSize.sp)) {
                                                    append(paragraph)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            letterSpacing = 0.2.sp
                                        ),
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                    
                    // End of Chapter
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Decorative End
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                "End of Chapter ${ch.chapterNumber}",
                                style = MaterialTheme.typography.labelLarge,
                                color = secondaryTextColor
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Navigation Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { navController.popBackStack() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Back to Novel")
                                }
                            }
                        }
                    }
                }
                
                // Top Bar (animated)
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = surfaceColor.copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    ch.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = textColor,
                                    maxLines = 1
                                )
                                Text(
                                    "Chapter ${ch.chapterNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryTextColor
                                )
                            }
                            
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor)
                            }
                        }
                    }
                }
                
                // Bottom Progress Bar (animated)
                AnimatedVisibility(
                    visible = showControls,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = surfaceColor.copy(alpha = 0.95f),
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Reading Progress",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = secondaryTextColor
                                )
                                Text(
                                    "${(readingProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = readingProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
        
        // Settings Bottom Sheet
        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                containerColor = surfaceColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        "Reading Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Font Size
                    Text("Font Size", style = MaterialTheme.typography.labelLarge, color = secondaryTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A", fontSize = 14.sp, color = textColor)
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 14f..28f,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                        )
                        Text("A", fontSize = 24.sp, color = textColor)
                    }
                    Text(
                        "${fontSize.toInt()} sp",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryTextColor,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Line Spacing
                    Text("Line Spacing", style = MaterialTheme.typography.labelLarge, color = secondaryTextColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(1.5f to "Compact", 1.8f to "Normal", 2.2f to "Relaxed").forEach { (value, label) ->
                            FilterChip(
                                selected = lineSpacing == value,
                                onClick = { lineSpacing = value },
                                label = { Text(label) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Theme Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Mode", style = MaterialTheme.typography.labelLarge, color = textColor)
                            Text(
                                "Easier on the eyes at night",
                                style = MaterialTheme.typography.bodySmall,
                                color = secondaryTextColor
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

fun formatChapterContent(content: String): List<String> {
    // First, replace tabs with spaces (common issue from PDF extraction)
    var text = content
        .replace("\t", " ")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
    
    // Fix common OCR/extraction issues
    text = text
        // Multiple spaces to single space
        .replace(Regex(" {2,}"), " ")
        // Fix missing space after punctuation
        .replace(Regex("([.!?])([A-Z])"), "$1 $2")
        .replace(Regex(",([A-Za-z])"), ", $1")
        .replace(Regex(";([A-Za-z])"), "; $1")
        .replace(Regex(":([A-Za-z])"), ": $1")
        // Fix words stuck together (lowercase followed by uppercase)
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
    
    // Split into paragraphs - look for double newlines or single newlines
    // that appear to be paragraph breaks
    val rawParagraphs = text.split(Regex("\n"))
    
    // Merge lines that are part of the same paragraph
    val paragraphs = mutableListOf<String>()
    var currentParagraph = StringBuilder()
    
    for (line in rawParagraphs) {
        val trimmedLine = line.trim()
        
        if (trimmedLine.isEmpty()) {
            // Empty line = paragraph break
            if (currentParagraph.isNotEmpty()) {
                paragraphs.add(currentParagraph.toString().trim())
                currentParagraph = StringBuilder()
            }
        } else if (isChapterHeading(trimmedLine)) {
            // Chapter headings get their own paragraph
            if (currentParagraph.isNotEmpty()) {
                paragraphs.add(currentParagraph.toString().trim())
                currentParagraph = StringBuilder()
            }
            paragraphs.add(trimmedLine)
        } else {
            // Continue the current paragraph
            if (currentParagraph.isNotEmpty()) {
                // Check if previous line ended with sentence-ending punctuation
                val prevText = currentParagraph.toString()
                if (prevText.endsWith(".") || prevText.endsWith("!") || 
                    prevText.endsWith("?") || prevText.endsWith("\"") ||
                    prevText.endsWith("'")) {
                    // Likely a new paragraph
                    paragraphs.add(prevText.trim())
                    currentParagraph = StringBuilder(trimmedLine)
                } else {
                    // Continue same paragraph
                    currentParagraph.append(" ").append(trimmedLine)
                }
            } else {
                currentParagraph.append(trimmedLine)
            }
        }
    }
    
    // Don't forget the last paragraph
    if (currentParagraph.isNotEmpty()) {
        paragraphs.add(currentParagraph.toString().trim())
    }
    
    // Final cleanup of each paragraph
    return paragraphs
        .map { p -> 
            p.replace(Regex(" +"), " ")  // Clean up any remaining multiple spaces
             .trim()
        }
        .filter { it.isNotBlank() }
}

fun isChapterHeading(text: String): Boolean {
    val upper = text.uppercase()
    return (upper == text && text.length < 50 && !text.contains(".")) ||
           text.matches(Regex("^CHAPTER\\s+[IVXLCDM0-9]+.*", RegexOption.IGNORE_CASE)) ||
           text.matches(Regex("^PART\\s+[IVXLCDM0-9]+.*", RegexOption.IGNORE_CASE))
}
