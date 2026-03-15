package com.novelplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiService {
    
    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public BookAnalysisResult analyzeBook(String fullText) {
        log.info("Analyzing book with {} characters", fullText.length());
        
        // Step 1: Get metadata from first part
        String firstPart = fullText.substring(0, Math.min(fullText.length(), 8000));
        String metadataPrompt = """
            Extract from this book:
            - title: The book title
            - author: Author name
            - genre: One of (Romance, Fantasy, Mystery, Thriller, Horror, Sci-Fi, Adventure, Historical, Fiction)
            - summary: 2-3 sentence summary
            
            Return JSON only: {"title":"...","author":"...","genre":"...","summary":"..."}
            
            Text:
            %s
            """.formatted(firstPart);
        
        String title = "Unknown", author = "Unknown", genre = "Fiction", summary = "An engaging story.";
        try {
            String metaResp = callOpenAI(metadataPrompt, 300);
            JsonNode meta = objectMapper.readTree(extractJson(metaResp));
            title = meta.path("title").asText("Unknown");
            author = meta.path("author").asText("Unknown");
            genre = meta.path("genre").asText("Fiction");
            summary = meta.path("summary").asText("An engaging story.");
            log.info("Metadata: title='{}', author='{}'", title, author);
        } catch (Exception e) {
            log.error("Metadata extraction failed: {}", e.getMessage());
        }
        
        // Step 2: Extract all short lines (potential chapter markers) with line numbers
        List<LineInfo> shortLines = extractShortLines(fullText);
        
        // Step 3: Send to AI to identify which are actual chapter starts
        List<ChapterInfo> chapters = identifyChaptersWithAI(shortLines, fullText);
        
        // Step 4: If AI-based detection failed, use direct regex scan as final fallback
        if (chapters.isEmpty() || chapters.size() == 1) {
            log.info("AI detection returned {} chapters, trying direct regex fallback", chapters.size());
            List<ChapterInfo> regexChapters = detectChaptersByDirectRegex(fullText);
            if (regexChapters.size() >= 2) {
                log.info("Direct regex fallback found {} chapters", regexChapters.size());
                chapters = regexChapters;
            }
        }
        
        if (chapters.isEmpty()) {
            chapters.add(new ChapterInfo(1, "Full Text", fullText));
        }
        
        return new BookAnalysisResult(title, author, genre, summary, chapters);
    }
    
    private static class LineInfo {
        int lineNum;
        int charPos;
        String text;
        LineInfo(int lineNum, int charPos, String text) {
            this.lineNum = lineNum;
            this.charPos = charPos;
            this.text = text;
        }
    }
    
    private List<LineInfo> extractShortLines(String fullText) {
        List<LineInfo> shortLines = new ArrayList<>();
        String[] lines = fullText.split("\n");
        int charPos = 0;
        
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            // Short lines that could be chapter markers (2-80 chars)
            if (trimmed.length() >= 2 && trimmed.length() <= 80) {
                shortLines.add(new LineInfo(i + 1, charPos, trimmed));
            }
            charPos += lines[i].length() + 1;
        }
        
        return shortLines;
    }
    
    private List<ChapterInfo> identifyChaptersWithAI(List<LineInfo> shortLines, String fullText) {
        log.info("Total short lines found: {}", shortLines.size());
        
        // APPROACH 1: Try to find and parse Table of Contents from the actual text
        TocResult tocResult = findTableOfContentsInText(fullText);
        if (tocResult != null && !tocResult.entries.isEmpty()) {
            log.info("Found Table of Contents with {} entries at position {}", 
                tocResult.entries.size(), tocResult.tocEndPosition);
            
            List<ChapterInfo> chaptersFromToc = findChaptersUsingTOC(tocResult, fullText);
            if (chaptersFromToc.size() >= 2) {
                log.info("Successfully detected {} chapters using TOC", chaptersFromToc.size());
                return chaptersFromToc;
            }
        }
        
        // APPROACH 2: Pattern-based detection for common chapter formats
        List<LineInfo> patternMatches = findChaptersByPattern(shortLines);
        
        if (patternMatches.size() >= 2) {
            log.info("Pattern matching found {} chapters, using pattern-based detection", patternMatches.size());
            return createChaptersFromLines(patternMatches, fullText);
        }
        
        // APPROACH 3: Fall back to AI detection - process ALL short lines in batches
        List<Integer> allSelectedIndices = new ArrayList<>();
        int batchSize = 300;
        int totalBatches = (shortLines.size() + batchSize - 1) / batchSize;
        
        for (int batch = 0; batch < totalBatches; batch++) {
            int startIdx = batch * batchSize;
            int endIdx = Math.min(startIdx + batchSize, shortLines.size());
            
            StringBuilder sb = new StringBuilder();
            for (int i = startIdx; i < endIdx; i++) {
                LineInfo li = shortLines.get(i);
                sb.append(i + 1).append(". ").append(li.text).append("\n");
            }
            
            String prompt = """
                Below is a numbered list of short lines from a book (batch %d of %d).
                
                Identify which lines are ACTUAL CHAPTER STARTS in the main content.
                
                INCLUDE: Chapter 1, Chapter I, CHAPTER ONE, Part One, Prologue, Epilogue, etc.
                EXCLUDE: Table of Contents entries, page numbers, copyright, About Author
                
                IMPORTANT: If you see the same chapter title twice (e.g., "CHAPTER I" in TOC and later in text), 
                pick the LATER occurrence which is the actual chapter start.
                
                Return ONLY a JSON array of the INDEX numbers: [5, 12, 25]
                If no chapters found in this batch, return: []
                
                Lines:
                %s
                """.formatted(batch + 1, totalBatches, sb.toString());
            
            try {
                String response = callOpenAI(prompt, 400);
                log.info("AI batch {} response: {}", batch + 1, response);
                List<Integer> batchIndices = parseIntArray(response);
                allSelectedIndices.addAll(batchIndices);
            } catch (Exception e) {
                log.error("AI batch {} failed: {}", batch + 1, e.getMessage());
            }
        }
        
        log.info("AI selected total indices: {}", allSelectedIndices);
        
        if (allSelectedIndices.isEmpty()) {
            // Last resort: use pattern matching with relaxed rules
            return createChaptersFromLines(findChaptersByPatternRelaxed(shortLines), fullText);
        }
        
        // Get selected lines
        List<LineInfo> selectedLines = new ArrayList<>();
        for (int idx : allSelectedIndices) {
            if (idx >= 1 && idx <= shortLines.size()) {
                selectedLines.add(shortLines.get(idx - 1));
            }
        }
        
        return createChaptersFromLines(selectedLines, fullText);
    }
    
    private List<LineInfo> findChaptersByPattern(List<LineInfo> shortLines) {
        // Common chapter patterns
        String[] patterns = {
            "(?i)^CHAPTER\\s+[IVXLCDM]+\\.?\\s*$",           // CHAPTER I, CHAPTER II
            "(?i)^CHAPTER\\s+[IVXLCDM]+[:\\.].*",            // CHAPTER I: Title
            "(?i)^CHAPTER\\s+\\d+\\.?\\s*$",                  // CHAPTER 1, CHAPTER 2
            "(?i)^CHAPTER\\s+\\d+[:\\.].*",                   // CHAPTER 1: Title
            "(?i)^CHAPTER\\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|SEVEN|EIGHT|NINE|TEN|ELEVEN|TWELVE|THIRTEEN|FOURTEEN|FIFTEEN|SIXTEEN|SEVENTEEN|EIGHTEEN|NINETEEN|TWENTY).*",
            "(?i)^PART\\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|[IVXLCDM]+|\\d+)\\s*$",
            "(?i)^PART\\s+(ONE|TWO|THREE|FOUR|FIVE|SIX|[IVXLCDM]+|\\d+)[:\\.].*",
            "(?i)^BOOK\\s+(ONE|TWO|THREE|[IVXLCDM]+|\\d+).*",
            "(?i)^PROLOGUE\\.?\\s*$",
            "(?i)^EPILOGUE\\.?\\s*$",
            "(?i)^INTRODUCTION\\.?\\s*$",
            "(?i)^PREFACE\\.?\\s*$"
        };
        
        // First pass: find ALL matches with their positions and indices
        Map<String, List<Integer>> titleIndices = new LinkedHashMap<>();
        
        for (int i = 0; i < shortLines.size(); i++) {
            LineInfo li = shortLines.get(i);
            String text = li.text.trim();
            
            for (String pattern : patterns) {
                if (text.matches(pattern)) {
                    String normalized = text.toUpperCase().replaceAll("\\s+", " ").trim();
                    titleIndices.computeIfAbsent(normalized, k -> new ArrayList<>()).add(i);
                    break;
                }
            }
        }
        
        // Detect TOC: Only mark entries as TOC if the same title appears BOTH in an early
        // cluster AND later in the document. If a title only appears once, it's a real chapter heading.
        Set<Integer> tocIndices = new HashSet<>();
        List<Integer> allMatchIndices = new ArrayList<>();
        for (List<Integer> indices : titleIndices.values()) {
            allMatchIndices.addAll(indices);
        }
        Collections.sort(allMatchIndices);
        
        // Use character position (not short line index) for TOC boundary — first 10% of text
        int maxCharPos = shortLines.isEmpty() ? 0 : shortLines.get(shortLines.size() - 1).charPos;
        int tocCharBoundary = maxCharPos / 10;
        
        // Find early cluster: entries whose charPos is in the first 10% of the document
        List<Integer> earlyMatches = new ArrayList<>();
        for (int idx : allMatchIndices) {
            if (shortLines.get(idx).charPos < tocCharBoundary) {
                earlyMatches.add(idx);
            }
        }
        
        // Only treat early matches as TOC if:
        // 1. There are 3+ clustered entries in the early section
        // 2. The cluster is tight (spans < 3000 chars)
        // 3. Each title in the cluster also appears LATER in the document (i.e., has a duplicate)
        if (earlyMatches.size() >= 3) {
            int firstCharPos = shortLines.get(earlyMatches.get(0)).charPos;
            int lastCharPos = shortLines.get(earlyMatches.get(earlyMatches.size() - 1)).charPos;
            
            if (lastCharPos - firstCharPos < 3000) {
                // Only mark as TOC if the title has a duplicate occurrence later
                for (int earlyIdx : earlyMatches) {
                    String earlyText = shortLines.get(earlyIdx).text.toUpperCase().replaceAll("\\s+", " ").trim();
                    List<Integer> allOccurrences = titleIndices.getOrDefault(earlyText, Collections.emptyList());
                    
                    // Check if there's at least one occurrence OUTSIDE the early cluster
                    boolean hasDuplicate = allOccurrences.stream()
                            .anyMatch(idx -> !earlyMatches.contains(idx));
                    
                    if (hasDuplicate) {
                        tocIndices.add(earlyIdx);
                    }
                }
                log.info("Detected TOC cluster: {} entries marked as TOC (out of {} early matches)", 
                    tocIndices.size(), earlyMatches.size());
            }
        }
        
        log.info("Detected {} TOC entries out of {} total matches", tocIndices.size(), allMatchIndices.size());
        
        // Second pass: for each title, pick the occurrence that's NOT in TOC
        List<LineInfo> matches = new ArrayList<>();
        
        for (Map.Entry<String, List<Integer>> entry : titleIndices.entrySet()) {
            List<Integer> indices = entry.getValue();
            
            // Find the first occurrence that's NOT in TOC
            Integer selectedIdx = null;
            for (Integer idx : indices) {
                if (!tocIndices.contains(idx)) {
                    selectedIdx = idx;
                    break;
                }
            }
            
            // If ALL occurrences are in TOC, this chapter title only exists in TOC
            if (selectedIdx == null) {
                log.info("Skipping '{}' - only appears in TOC, no actual chapter heading", entry.getKey());
                continue;
            }
            
            LineInfo li = shortLines.get(selectedIdx);
            matches.add(li);
            log.info("Chapter '{}': selected index {} at position {}", 
                entry.getKey(), selectedIdx, li.charPos);
        }
        
        // Sort by position in text
        matches.sort(Comparator.comparingInt(l -> l.charPos));
        
        log.info("Pattern matching found {} chapters", matches.size());
        return matches;
    }
    
    private List<LineInfo> findChaptersByPatternRelaxed(List<LineInfo> shortLines) {
        // More relaxed patterns
        List<LineInfo> matches = new ArrayList<>();
        
        for (LineInfo li : shortLines) {
            String text = li.text.trim();
            if (text.matches("(?i).*CHAPTER.*") || 
                text.matches("(?i)^PART\\s+.*") ||
                text.matches("(?i)^[IVXLCDM]+\\.?\\s*$") ||  // Just Roman numerals
                text.matches("(?i)^\\d+\\.?\\s*$")) {         // Just numbers
                matches.add(li);
            }
        }
        
        // Use character position to skip TOC instead of naive half-split.
        // If there are many matches, check if the first cluster is a TOC by looking
        // for a gap in character positions (TOC entries are close together, then there's
        // a big gap before actual chapter content starts).
        if (matches.size() > 6) {
            int maxGap = 0;
            int gapIndex = 0;
            for (int i = 1; i < matches.size(); i++) {
                int gap = matches.get(i).charPos - matches.get(i - 1).charPos;
                if (gap > maxGap) {
                    maxGap = gap;
                    gapIndex = i;
                }
            }
            // If the biggest gap separates roughly equal halves, split there (skip TOC)
            if (gapIndex > 0 && gapIndex <= matches.size() / 2) {
                matches = new ArrayList<>(matches.subList(gapIndex, matches.size()));
            }
        }
        
        return matches;
    }
    
    // ============ NEW TOC-Based Detection with AI ============
    
    private static class TocResult {
        String tocText;           // The raw TOC text
        int tocEndPosition;       // Where TOC ends in the full text
        List<String> entries;     // Chapter names extracted by AI
        
        TocResult(String tocText, int tocEndPosition, List<String> entries) {
            this.tocText = tocText;
            this.tocEndPosition = tocEndPosition;
            this.entries = entries;
        }
    }
    
    /**
     * Step 1: Find "Contents" section in the PDF text and extract it
     */
    private TocResult findTableOfContentsInText(String fullText) {
        // Look for "Contents" or "Table of Contents" header
        String[] tocHeaders = {
            "(?i)^\\s*Contents\\s*$",
            "(?i)^\\s*Table of Contents\\s*$",
            "(?i)^\\s*CONTENTS\\s*$"
        };
        
        String[] lines = fullText.split("\n");
        int tocStartLine = -1;
        int tocStartPos = 0;
        int charPos = 0;
        
        // Find TOC header in first 20% of the document
        int searchLimit = lines.length / 5;
        for (int i = 0; i < Math.min(searchLimit, lines.length); i++) {
            String line = lines[i].trim();
            for (String pattern : tocHeaders) {
                if (line.matches(pattern)) {
                    tocStartLine = i;
                    tocStartPos = charPos;
                    log.info("Found TOC header '{}' at line {}", line, i);
                    break;
                }
            }
            if (tocStartLine >= 0) break;
            charPos += lines[i].length() + 1;
        }
        
        if (tocStartLine == -1) {
            log.info("No Table of Contents found in document");
            return null;
        }
        
        // Extract TOC section (usually 20-50 lines after "Contents")
        StringBuilder tocText = new StringBuilder();
        int tocEndLine = tocStartLine;
        int tocEndPos = tocStartPos;
        charPos = tocStartPos;
        
        for (int i = tocStartLine; i < Math.min(tocStartLine + 60, lines.length); i++) {
            String line = lines[i].trim();
            tocText.append(line).append("\n");
            charPos += lines[i].length() + 1;
            
            // Stop if we hit clear end markers
            if (line.toLowerCase().matches("(?i)^(about the author|copyright|back ads|about the publisher).*")) {
                tocEndLine = i;
                tocEndPos = charPos;
                break;
            }
            
            // Also stop if we see a very long line (actual content started)
            if (i > tocStartLine + 5 && line.length() > 100) {
                tocEndLine = i - 1;
                tocEndPos = charPos - lines[i].length() - 1;
                break;
            }
            
            tocEndLine = i;
            tocEndPos = charPos;
        }
        
        log.info("Extracted TOC from line {} to {}, {} chars", tocStartLine, tocEndLine, tocText.length());
        
        // Step 2: Use AI to extract chapter names from TOC
        List<String> chapterNames = extractChapterNamesWithAI(tocText.toString());
        
        if (chapterNames.isEmpty()) {
            log.info("AI could not extract chapter names from TOC");
            return null;
        }
        
        return new TocResult(tocText.toString(), tocEndPos, chapterNames);
    }
    
    /**
     * Step 2: Ask AI to identify actual chapter names from TOC text
     */
    private List<String> extractChapterNamesWithAI(String tocText) {
        String prompt = """
            Below is the Table of Contents from a book.
            
            Extract ONLY the actual chapter/section names that contain story content.
            
            INCLUDE: Prologue, Part One, Part Two, Chapter 1, Epilogue, Introduction, etc.
            EXCLUDE: About the Author, Copyright, Back Ads, About the Publisher, Acknowledgments, Also by Author, Preview sections
            
            Return a JSON array of chapter names in ORDER as they appear:
            ["Prologue", "Part One", "Part Two", "Epilogue"]
            
            Table of Contents:
            %s
            """.formatted(tocText);
        
        try {
            String response = callOpenAI(prompt, 500);
            log.info("AI extracted chapters from TOC: {}", response);
            
            String json = extractJson(response);
            List<String> chapters = new ArrayList<>();
            JsonNode arr = objectMapper.readTree(json);
            for (JsonNode n : arr) {
                chapters.add(n.asText());
            }
            
            log.info("Extracted {} chapter names: {}", chapters.size(), chapters);
            return chapters;
        } catch (Exception e) {
            log.error("Failed to extract chapters from TOC: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * Step 3: Find each chapter name in the actual content (after TOC) and extract content
     * If some chapters are images (not text), use AI to find split points
     */
    private List<ChapterInfo> findChaptersUsingTOC(TocResult tocResult, String fullText) {
        // Search for chapters only AFTER the TOC section
        String contentAfterToc = fullText.substring(tocResult.tocEndPosition);
        int baseOffset = tocResult.tocEndPosition;
        
        log.info("Searching for chapters in content after position {}", baseOffset);
        
        // Track found and missing chapters
        List<FoundChapter> foundChapters = new ArrayList<>();
        List<Integer> missingIndices = new ArrayList<>();
        
        for (int i = 0; i < tocResult.entries.size(); i++) {
            String chapterName = tocResult.entries.get(i);
            String searchName = chapterName.trim();
            
            log.info("Looking for chapter '{}' in content", chapterName);
            
            // Search line by line with flexible matching
            String[] lines = contentAfterToc.split("\n");
            int charPos = 0;
            int foundPos = -1;
            String foundLine = null;
            
            for (String line : lines) {
                String trimmedLine = line.trim();
                String upperLine = trimmedLine.toUpperCase().replaceAll("\\s+", " ");
                String upperSearch = searchName.toUpperCase().replaceAll("\\s+", " ");
                
                // Flexible matching
                if (upperLine.equals(upperSearch) || 
                    trimmedLine.equalsIgnoreCase(searchName) ||
                    upperLine.matches("(?i)^" + Pattern.quote(upperSearch) + "\\s*$")) {
                    foundPos = charPos;
                    foundLine = trimmedLine;
                    break;
                }
                charPos += line.length() + 1;
            }
            
            if (foundPos >= 0) {
                foundChapters.add(new FoundChapter(i, chapterName, baseOffset + foundPos, foundLine));
                log.info("Found chapter '{}' at position {}", chapterName, baseOffset + foundPos);
            } else {
                missingIndices.add(i);
                log.warn("Chapter '{}' not found (might be an image in PDF)", chapterName);
            }
        }
        
        // Sort found chapters by position
        foundChapters.sort(Comparator.comparingInt(c -> c.position));
        
        log.info("Found {} of {} chapters from TOC, missing: {}", 
            foundChapters.size(), tocResult.entries.size(), missingIndices.size());
        
        // If we found at least 2 chapters, try to fill in missing ones
        if (foundChapters.size() < 2) {
            log.warn("Not enough chapters found, falling back to other methods");
            return Collections.emptyList();
        }
        
        // Find back matter position
        int backMatterPos = findBackMatter(fullText);
        
        // If there are missing chapters, use AI to find split points
        if (!missingIndices.isEmpty()) {
            foundChapters = fillMissingChaptersWithAI(foundChapters, missingIndices, tocResult, fullText, backMatterPos);
        }
        
        // Sort again after filling
        foundChapters.sort(Comparator.comparingInt(c -> c.position));
        
        // Create final chapters
        List<ChapterInfo> chapters = new ArrayList<>();
        for (int i = 0; i < foundChapters.size(); i++) {
            FoundChapter fc = foundChapters.get(i);
            int start = fc.position;
            int end;
            
            if (i < foundChapters.size() - 1) {
                end = foundChapters.get(i + 1).position;
            } else {
                end = Math.min(backMatterPos, fullText.length());
            }
            
            if (end <= start) {
                log.warn("Invalid chapter range: start={}, end={}", start, end);
                continue;
            }
            
            String content = fullText.substring(start, end);
            int wordCount = content.split("\\s+").length;
            
            chapters.add(new ChapterInfo(chapters.size() + 1, fc.foundLine != null ? fc.foundLine : fc.name, content));
            log.info("Chapter {}: '{}' - {} words", chapters.size(), fc.name, wordCount);
        }
        
        return chapters;
    }
    
    /**
     * Use AI to find split points for missing chapters
     */
    private List<FoundChapter> fillMissingChaptersWithAI(
            List<FoundChapter> foundChapters, 
            List<Integer> missingIndices,
            TocResult tocResult,
            String fullText,
            int backMatterPos) {
        
        List<FoundChapter> result = new ArrayList<>(foundChapters);
        
        // For each gap between found chapters, check if missing chapters should go there
        for (int i = 0; i < foundChapters.size(); i++) {
            FoundChapter current = foundChapters.get(i);
            int gapStart = current.position;
            int gapEnd;
            
            if (i < foundChapters.size() - 1) {
                gapEnd = foundChapters.get(i + 1).position;
            } else {
                gapEnd = Math.min(backMatterPos, fullText.length());
            }
            
            // Find missing chapters that should be between current and next
            List<Integer> missingInGap = new ArrayList<>();
            for (int missingIdx : missingIndices) {
                // Check if this missing chapter's TOC index is between current and next found chapter
                int nextFoundTocIdx = (i < foundChapters.size() - 1) ? 
                    foundChapters.get(i + 1).tocIndex : tocResult.entries.size();
                
                if (missingIdx > current.tocIndex && missingIdx < nextFoundTocIdx) {
                    missingInGap.add(missingIdx);
                }
            }
            
            if (missingInGap.isEmpty()) continue;
            
            log.info("Found {} missing chapters between '{}' and next chapter", 
                missingInGap.size(), current.name);
            
            // Get the gap content
            String gapContent = fullText.substring(gapStart, gapEnd);
            int gapWords = gapContent.split("\\s+").length;
            
            // Only try to split if gap is large enough (>5000 words per missing chapter)
            if (gapWords < missingInGap.size() * 3000) {
                log.info("Gap too small ({} words) to split into {} chapters", gapWords, missingInGap.size());
                continue;
            }
            
            // Use AI to find split points
            List<Integer> splitPoints = findSplitPointsWithAI(gapContent, missingInGap.size(), 
                missingInGap.stream().map(idx -> tocResult.entries.get(idx)).collect(Collectors.toList()));
            
            // Add missing chapters at split points
            for (int j = 0; j < splitPoints.size() && j < missingInGap.size(); j++) {
                int splitPos = gapStart + splitPoints.get(j);
                int missingIdx = missingInGap.get(j);
                String missingName = tocResult.entries.get(missingIdx);
                
                result.add(new FoundChapter(missingIdx, missingName, splitPos, missingName));
                log.info("Added missing chapter '{}' at position {} (AI split)", missingName, splitPos);
            }
        }
        
        return result;
    }
    
    /**
     * Ask AI to find the best split points in content
     */
    private List<Integer> findSplitPointsWithAI(String content, int numSplits, List<String> chapterNames) {
        List<Integer> splitPoints = new ArrayList<>();
        
        // Split content into paragraphs
        String[] paragraphs = content.split("\n\n+");
        if (paragraphs.length < numSplits * 2) {
            // Not enough paragraphs, use equal division
            int chunkSize = content.length() / (numSplits + 1);
            for (int i = 1; i <= numSplits; i++) {
                int approxPos = chunkSize * i;
                // Find nearest paragraph break
                int nearestBreak = content.indexOf("\n\n", approxPos - 500);
                if (nearestBreak > 0 && nearestBreak < approxPos + 500) {
                    splitPoints.add(nearestBreak + 2);
                } else {
                    splitPoints.add(approxPos);
                }
            }
            return splitPoints;
        }
        
        // Create paragraph index with positions
        List<int[]> paraPositions = new ArrayList<>(); // [index, startPos, endPos]
        int pos = 0;
        for (int i = 0; i < paragraphs.length; i++) {
            int start = content.indexOf(paragraphs[i], pos);
            if (start >= 0) {
                paraPositions.add(new int[]{i, start, start + paragraphs[i].length()});
                pos = start + paragraphs[i].length();
            }
        }
        
        // Sample paragraphs for AI (every Nth paragraph to keep prompt small)
        int sampleRate = Math.max(1, paragraphs.length / 50);
        StringBuilder sampleText = new StringBuilder();
        List<Integer> sampledIndices = new ArrayList<>();
        
        for (int i = 0; i < paragraphs.length; i += sampleRate) {
            String para = paragraphs[i];
            if (para.length() > 200) {
                para = para.substring(0, 200) + "...";
            }
            sampleText.append("[").append(i).append("] ").append(para.replace("\n", " ")).append("\n\n");
            sampledIndices.add(i);
        }
        
        String prompt = """
            I need to split this book content into %d parts for these chapters: %s
            
            Below are sampled paragraphs with their index numbers [N].
            Find the best paragraph indices where each new chapter should START.
            Look for: scene changes, time skips, location changes, or natural story breaks.
            
            Return ONLY a JSON array of %d paragraph indices in order: [15, 42]
            
            Sampled paragraphs:
            %s
            """.formatted(numSplits, chapterNames, numSplits, sampleText.toString());
        
        try {
            String response = callOpenAI(prompt, 200);
            log.info("AI split response: {}", response);
            
            List<Integer> paraIndices = parseIntArray(response);
            
            // Convert paragraph indices to character positions
            for (int paraIdx : paraIndices) {
                // Find the closest sampled paragraph
                int actualIdx = paraIdx;
                if (actualIdx < paraPositions.size()) {
                    splitPoints.add(paraPositions.get(actualIdx)[1]);
                }
            }
            
        } catch (Exception e) {
            log.error("AI split failed: {}, using equal division", e.getMessage());
            // Fallback to equal division
            int chunkSize = content.length() / (numSplits + 1);
            for (int i = 1; i <= numSplits; i++) {
                splitPoints.add(chunkSize * i);
            }
        }
        
        // Sort and ensure we have the right number
        Collections.sort(splitPoints);
        while (splitPoints.size() > numSplits) {
            splitPoints.remove(splitPoints.size() - 1);
        }
        
        return splitPoints;
    }
    
    private static class FoundChapter {
        int tocIndex;
        String name;
        int position;
        String foundLine;
        
        FoundChapter(int tocIndex, String name, int position, String foundLine) {
            this.tocIndex = tocIndex;
            this.name = name;
            this.position = position;
            this.foundLine = foundLine;
        }
    }
    
    // ============ End TOC Methods ============
    
    private List<ChapterInfo> createChaptersFromLines(List<LineInfo> selectedLines, String fullText) {
        if (selectedLines.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Create a new list to avoid modifying the original
        List<LineInfo> sortedLines = new ArrayList<>(selectedLines);
        
        // Sort by character position in text
        sortedLines.sort(Comparator.comparingInt(l -> l.charPos));
        
        // Remove duplicates and invalid positions
        List<LineInfo> uniqueLines = new ArrayList<>();
        int lastPos = -1;
        for (LineInfo li : sortedLines) {
            // Skip invalid positions
            if (li.charPos < 0 || li.charPos >= fullText.length()) {
                log.warn("Skipping invalid position {} for '{}'", li.charPos, li.text);
                continue;
            }
            // Skip duplicates (within 100 chars of each other)
            if (lastPos >= 0 && Math.abs(li.charPos - lastPos) < 100) {
                continue;
            }
            uniqueLines.add(li);
            lastPos = li.charPos;
        }
        
        log.info("After dedup: {} unique chapter markers", uniqueLines.size());
        
        if (uniqueLines.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Find back matter
        int backMatterPos = findBackMatter(fullText);
        
        // Create chapters
        List<ChapterInfo> chapters = new ArrayList<>();
        for (int i = 0; i < uniqueLines.size(); i++) {
            LineInfo li = uniqueLines.get(i);
            int start = li.charPos;
            int end;
            
            if (i < uniqueLines.size() - 1) {
                end = uniqueLines.get(i + 1).charPos;
            } else {
                end = Math.min(backMatterPos, fullText.length());
            }
            
            // Safety check: end must be after start
            if (end <= start) {
                log.warn("Invalid range for '{}': start={}, end={}, skipping", li.text, start, end);
                continue;
            }
            
            // Skip very short sections (< 200 chars) unless it's the last chapter
            if (end - start < 200 && i < uniqueLines.size() - 1) {
                log.info("Skipping very short section '{}' ({} chars)", li.text, end - start);
                continue;
            }
            
            String content = fullText.substring(start, end);
            chapters.add(new ChapterInfo(chapters.size() + 1, li.text, content));
            log.info("Chapter {}: '{}' - {} words", chapters.size(), li.text, content.split("\\s+").length);
        }
        
        return chapters;
    }

    /**
     * Direct regex-based chapter detection that scans the full text line by line.
     * This is a robust fallback that doesn't depend on the short lines extraction or AI.
     * It finds "CHAPTER X" headings, skips TOC entries (clustered short lines at the start),
     * and splits content at each heading.
     */
    private List<ChapterInfo> detectChaptersByDirectRegex(String fullText) {
        String[] lines = fullText.split("\n");
        
        // Regex patterns for chapter headings
        java.util.regex.Pattern chapterPattern = java.util.regex.Pattern.compile(
            "(?i)^\\s*(CHAPTER|PROLOGUE|EPILOGUE)\\s*[\\dIVXLCDM]*(\\s.*)?$"
        );
        
        // First pass: find all chapter heading positions
        List<int[]> allMatches = new ArrayList<>(); // [charPos, lineIndex]
        int charPos = 0;
        for (int i = 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (trimmed.length() >= 2 && trimmed.length() <= 80 && chapterPattern.matcher(trimmed).matches()) {
                allMatches.add(new int[]{charPos, i});
            }
            charPos += lines[i].length() + 1;
        }
        
        if (allMatches.size() < 2) {
            return Collections.emptyList();
        }
        
        log.info("Direct regex found {} chapter markers", allMatches.size());
        
        // Detect TOC cluster: look for a group of headings where consecutive entries
        // are very close together (< 200 chars apart), followed by a big gap
        int tocEndIndex = -1;
        for (int i = 0; i < allMatches.size() - 1; i++) {
            int gap = allMatches.get(i + 1)[0] - allMatches.get(i)[0];
            if (gap > 1000) {
                // Big gap found — everything before this might be TOC
                // But only if the entries before were tightly clustered
                boolean isTocCluster = true;
                for (int j = 0; j < i; j++) {
                    if (allMatches.get(j + 1)[0] - allMatches.get(j)[0] > 200) {
                        isTocCluster = false;
                        break;
                    }
                }
                if (isTocCluster && i >= 2) {
                    tocEndIndex = i;
                    log.info("Direct regex: detected TOC cluster of {} entries, skipping", tocEndIndex + 1);
                }
                break;
            }
        }
        
        // Build chapter list, skipping TOC entries
        int startFrom = tocEndIndex >= 0 ? tocEndIndex + 1 : 0;
        List<ChapterInfo> chapters = new ArrayList<>();
        int backMatterPos = findBackMatter(fullText);
        
        for (int i = startFrom; i < allMatches.size(); i++) {
            int start = allMatches.get(i)[0];
            int end;
            if (i < allMatches.size() - 1) {
                end = allMatches.get(i + 1)[0];
            } else {
                end = Math.min(backMatterPos, fullText.length());
            }
            
            if (end <= start) continue;
            
            // Skip very short sections (< 200 chars) unless it's the last
            if (end - start < 200 && i < allMatches.size() - 1) continue;
            
            String content = fullText.substring(start, end);
            String heading = lines[allMatches.get(i)[1]].trim();
            chapters.add(new ChapterInfo(chapters.size() + 1, heading, content));
            log.info("Direct regex chapter {}: '{}' - {} words", chapters.size(), heading, content.split("\\s+").length);
        }
        
        return chapters;
    }

    private int findBackMatter(String fullText) {
        // Look for common back matter indicators
        String[] backMatterPatterns = {
            "(?i)\\bABOUT THE AUTHOR\\b",
            "(?i)\\bACKNOWLEDGMENTS\\b",
            "(?i)\\bACKNOWLEDGEMENTS\\b",
            "(?i)\\bABOUT THE BOOK\\b",
            "(?i)\\bALSO BY\\b",
            "(?i)\\bOTHER BOOKS BY\\b",
            "(?i)\\bBIBLIOGRAPHY\\b",
            "(?i)\\bGLOSSARY\\b",
            "(?i)\\bINDEX\\b",
            "(?i)\\bNOTES\\b",
            "(?i)\\bAPPENDIX\\b"
        };
        
        int minPos = fullText.length();
        for (String pattern : backMatterPatterns) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(fullText);
            // Find the LAST occurrence (back matter is at the end)
            int lastMatch = -1;
            while (m.find()) {
                // Only consider if it's in the last 20% of the book
                if (m.start() > fullText.length() * 0.8) {
                    lastMatch = m.start();
                }
            }
            if (lastMatch > 0 && lastMatch < minPos) {
                minPos = lastMatch;
            }
        }
        return minPos;
    }
    
    private List<Integer> parseIntArray(String response) {
        List<Integer> result = new ArrayList<>();
        try {
            // Extract JSON array from response
            String json = extractJson(response);
            if (json.startsWith("[")) {
                JsonNode arr = objectMapper.readTree(json);
                for (JsonNode n : arr) {
                    result.add(n.asInt());
                }
            }
        } catch (Exception e) {
            // Try regex fallback
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(response);
            while (m.find()) {
                result.add(Integer.parseInt(m.group()));
            }
        }
        return result;
    }
    
    private String callOpenAI(String prompt, int maxTokens) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o-mini");
            body.put("max_tokens", maxTokens);
            body.put("temperature", 0.3);
            body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
            ));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                openaiApiUrl, HttpMethod.POST, request, String.class
            );
            
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("OpenAI call failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage());
        }
    }
    
    private String extractJson(String text) {
        // Find JSON object or array in text
        int start = -1, end = -1;
        char openChar = 0;
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (start == -1 && (c == '{' || c == '[')) {
                start = i;
                openChar = c;
            }
        }
        
        if (start == -1) return text.trim();
        
        char closeChar = openChar == '{' ? '}' : ']';
        int depth = 0;
        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == openChar) depth++;
            else if (c == closeChar) {
                depth--;
                if (depth == 0) {
                    end = i + 1;
                    break;
                }
            }
        }
        
        if (end > start) {
            return text.substring(start, end);
        }
        return text.trim();
    }
    
    // ============ Result Classes ============
    
    public static class BookAnalysisResult {
        public final String title;
        public final String author;
        public final String genre;
        public final String summary;
        public final List<ChapterInfo> chapters;
        
        public BookAnalysisResult(String title, String author, String genre, String summary, List<ChapterInfo> chapters) {
            this.title = title;
            this.author = author;
            this.genre = genre;
            this.summary = summary;
            this.chapters = chapters;
        }
    }
    
    public static class ChapterInfo {
        public final int number;
        public final String title;
        public final String content;
        
        public ChapterInfo(int number, String title, String content) {
            this.number = number;
            this.title = title;
            this.content = content;
        }
    }
    
    public static class ContentModerationResult {
        private final boolean appropriate;
        private final List<String> issues;
        private final String riskLevel;
        
        public ContentModerationResult(boolean appropriate, List<String> issues, String riskLevel) {
            this.appropriate = appropriate;
            this.issues = issues;
            this.riskLevel = riskLevel;
        }
        
        public boolean isAppropriate() { return appropriate; }
        public List<String> getIssues() { return issues; }
        public String getRiskLevel() { return riskLevel; }
    }
    
    public static class ChapterDetectionResult {
        private final int chapterNumber;
        private final String title;
        private final int startPosition;
        private final int endPosition;
        
        public ChapterDetectionResult(int chapterNumber, String title, int startPosition, int endPosition) {
            this.chapterNumber = chapterNumber;
            this.title = title;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }
        
        public int getChapterNumber() { return chapterNumber; }
        public String getTitle() { return title; }
        public int getStartPosition() { return startPosition; }
        public int getEndPosition() { return endPosition; }
    }
    
    // ============ Legacy Methods for Compatibility ============
    
    public List<ChapterDetectionResult> detectChapters(String text) {
        BookAnalysisResult result = analyzeBook(text);
        List<ChapterDetectionResult> chapters = new ArrayList<>();
        
        int pos = 0;
        for (ChapterInfo ch : result.chapters) {
            // Search for chapter content starting from current position to avoid
            // matching TOC or earlier occurrences
            String searchStr = ch.content.substring(0, Math.min(100, ch.content.length()));
            int start = text.indexOf(searchStr, pos);
            if (start == -1) start = pos;
            int end = start + ch.content.length();
            chapters.add(new ChapterDetectionResult(ch.number, ch.title, start, end));
            pos = end;
        }
        
        return chapters;
    }
    
    public String detectGenre(String title, String description, String text) {
        String sample = text.substring(0, Math.min(text.length(), 3000));
        String prompt = """
            Based on this book excerpt, determine the genre.
            Return ONLY one of: Romance, Fantasy, Mystery, Thriller, Horror, Sci-Fi, Adventure, Historical, Fiction
            
            Title: %s
            Description: %s
            Text: %s
            """.formatted(title, description, sample);
        
        try {
            String response = callOpenAI(prompt, 50);
            return response.trim().replaceAll("[^a-zA-Z-]", "");
        } catch (Exception e) {
            return "Fiction";
        }
    }
    
    public String generateSummary(String text) {
        String sample = text.substring(0, Math.min(text.length(), 4000));
        String prompt = "Write a 2-3 sentence summary of this book:\n\n" + sample;
        
        try {
            return callOpenAI(prompt, 150);
        } catch (Exception e) {
            return "An engaging story.";
        }
    }
    
    public ContentModerationResult moderateContent(String text) {
        String sample = text.substring(0, Math.min(text.length(), 3000));
        String prompt = """
            Analyze this text for content moderation.
            Return JSON: {"appropriate": true/false, "issues": ["issue1"], "riskLevel": "low/medium/high"}
            
            Text: %s
            """.formatted(sample);
        
        try {
            String response = callOpenAI(prompt, 100);
            JsonNode json = objectMapper.readTree(extractJson(response));
            boolean appropriate = json.path("appropriate").asBoolean(true);
            List<String> issues = new ArrayList<>();
            json.path("issues").forEach(n -> issues.add(n.asText()));
            String risk = json.path("riskLevel").asText("low");
            return new ContentModerationResult(appropriate, issues, risk);
        } catch (Exception e) {
            return new ContentModerationResult(true, Collections.emptyList(), "low");
        }
    }
}
