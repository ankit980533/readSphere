package com.novelplatform.service;

import com.novelplatform.model.Chapter;
import com.novelplatform.model.Genre;
import com.novelplatform.model.Novel;
import com.novelplatform.model.User;
import com.novelplatform.repository.ChapterRepository;
import com.novelplatform.repository.GenreRepository;
import com.novelplatform.repository.NovelRepository;
import com.novelplatform.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfProcessingService {
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired(required = false)
    private AiService aiService;
    
    @Autowired(required = false)
    private OllamaAiService ollamaAiService;
    
    @Autowired(required = false)
    private HuggingFaceAiService huggingFaceAiService;
    
    @Value("${ai.enabled:true}")
    private boolean aiEnabled;
    
    @Value("${ai.provider:openai}")
    private String aiProvider;
    
    public Novel processPdfUpload(MultipartFile file, String title, String authorName, 
                                  Long genreId, String description) throws IOException {
        
        String text = extractTextFromPdf(file);
        
        // AI-powered chapter detection
        List<AiService.ChapterDetectionResult> aiChapters = aiEnabled 
            ? detectChaptersWithAI(text)
            : fallbackChapterDetection(text);
        
        // AI-powered content moderation
        if (aiEnabled) {
            AiService.ContentModerationResult moderation = moderateContentWithAI(text);
            if (!moderation.isAppropriate()) {
                throw new RuntimeException("Content moderation failed: " + String.join(", ", moderation.getIssues()));
            }
        }
        
        // AI-powered genre detection (if not provided)
        Genre genre;
        if (genreId == null && aiEnabled) {
            String detectedGenre = detectGenreWithAI(title, description, text);
            genre = genreRepository.findByName(detectedGenre)
                    .orElseGet(() -> {
                        Genre newGenre = new Genre();
                        newGenre.setName(detectedGenre);
                        return genreRepository.save(newGenre);
                    });
        } else {
            genre = genreRepository.findById(genreId)
                    .orElseThrow(() -> new RuntimeException("Genre not found"));
        }
        
        // AI-powered summary generation (if description is empty)
        if ((description == null || description.isEmpty()) && aiEnabled) {
            description = generateSummaryWithAI(text);
        }
        
        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));
        
        Novel novel = new Novel();
        novel.setTitle(title);
        novel.setDescription(description);
        novel.setAuthor(adminUser);
        novel.setGenre(genre);
        novel.setSourceType(Novel.SourceType.ADMIN);
        novel.setStatus(Novel.Status.REVIEW);
        novel.setCreatedAt(LocalDateTime.now());
        
        novel = novelRepository.save(novel);
        
        // Save chapters
        for (AiService.ChapterDetectionResult chapterData : aiChapters) {
            String content = text.substring(
                chapterData.getStartPosition(), 
                Math.min(chapterData.getEndPosition(), text.length())
            );
            
            Chapter chapter = new Chapter();
            chapter.setNovel(novel);
            chapter.setChapterNumber(chapterData.getChapterNumber());
            chapter.setTitle(chapterData.getTitle());
            chapter.setContent(content);
            chapter.setWordCount(content.split("\\s+").length);
            chapter.setCreatedAt(LocalDateTime.now());
            chapterRepository.save(chapter);
        }
        
        return novel;
    }
    
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private List<AiService.ChapterDetectionResult> fallbackChapterDetection(String text) {
        List<AiService.ChapterDetectionResult> chapters = new ArrayList<>();
        String[] lines = text.split("\n");
        int currentPos = 0;
        int chapterNum = 1;
        List<Integer> positions = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        
        for (String line : lines) {
            if (line.trim().matches("(?i)^(Chapter|CHAPTER)\\s+\\d+.*")) {
                positions.add(currentPos);
                titles.add(line.trim());
            }
            currentPos += line.length() + 1;
        }
        
        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = (i < positions.size() - 1) ? positions.get(i + 1) : text.length();
            chapters.add(new AiService.ChapterDetectionResult(chapterNum++, titles.get(i), start, end));
        }
        
        if (chapters.isEmpty()) {
            chapters.add(new AiService.ChapterDetectionResult(1, "Chapter 1", 0, text.length()));
        }
        
        return chapters;
    }
    
    private List<AiService.ChapterDetectionResult> detectChaptersWithAI(String text) {
        switch (aiProvider.toLowerCase()) {
            case "ollama":
                return ollamaAiService != null ? ollamaAiService.detectChapters(text) : fallbackChapterDetection(text);
            case "openai":
                return aiService != null ? aiService.detectChapters(text) : fallbackChapterDetection(text);
            default:
                return fallbackChapterDetection(text);
        }
    }
    
    private String detectGenreWithAI(String title, String description, String text) {
        switch (aiProvider.toLowerCase()) {
            case "ollama":
                return ollamaAiService != null ? ollamaAiService.detectGenre(title, description, text) : "Fiction";
            case "huggingface":
                return huggingFaceAiService != null ? huggingFaceAiService.detectGenre(title, description, text) : "Fiction";
            case "openai":
                return aiService != null ? aiService.detectGenre(title, description, text) : "Fiction";
            default:
                return "Fiction";
        }
    }
    
    private String generateSummaryWithAI(String text) {
        switch (aiProvider.toLowerCase()) {
            case "ollama":
                return ollamaAiService != null ? ollamaAiService.generateSummary(text) : "An engaging story.";
            case "huggingface":
                return huggingFaceAiService != null ? huggingFaceAiService.generateSummary(text) : "An engaging story.";
            case "openai":
                return aiService != null ? aiService.generateSummary(text) : "An engaging story.";
            default:
                return "An engaging story.";
        }
    }
    
    private AiService.ContentModerationResult moderateContentWithAI(String text) {
        switch (aiProvider.toLowerCase()) {
            case "ollama":
                return ollamaAiService != null ? ollamaAiService.moderateContent(text) 
                    : new AiService.ContentModerationResult(true, java.util.Collections.emptyList(), "low");
            case "huggingface":
                return huggingFaceAiService != null ? huggingFaceAiService.moderateContent(text)
                    : new AiService.ContentModerationResult(true, java.util.Collections.emptyList(), "low");
            case "openai":
                return aiService != null ? aiService.moderateContent(text)
                    : new AiService.ContentModerationResult(true, java.util.Collections.emptyList(), "low");
            default:
                return new AiService.ContentModerationResult(true, java.util.Collections.emptyList(), "low");
        }
    }


    /**
     * Process novel PDF with single AI call for all metadata + chapters
     */
    public Novel processNovelPdf(MultipartFile file, String title, String authorName,
                                 String description) throws IOException {

        String text = extractTextFromPdf(file);

        // Single AI call to analyze everything
        String finalTitle = title;
        String finalAuthor = authorName;
        String finalGenre = "Fiction";
        String finalSummary = description;
        List<AiService.ChapterInfo> chapters = new ArrayList<>();
        
        if (aiEnabled && aiService != null) {
            AiService.BookAnalysisResult analysis = aiService.analyzeBook(text);
            
            // Use AI-detected values if not provided by user
            if (finalTitle == null || finalTitle.trim().isEmpty()) {
                finalTitle = analysis.title;
            }
            if (finalAuthor == null || finalAuthor.trim().isEmpty()) {
                finalAuthor = analysis.author;
            }
            finalGenre = analysis.genre;
            if (finalSummary == null || finalSummary.trim().isEmpty()) {
                finalSummary = analysis.summary;
            }
            chapters = analysis.chapters;
        }
        
        // Fallbacks
        if (finalTitle == null || finalTitle.trim().isEmpty()) finalTitle = "Untitled Novel";
        if (finalAuthor == null || finalAuthor.trim().isEmpty()) finalAuthor = "Unknown Author";
        if (finalSummary == null || finalSummary.trim().isEmpty()) finalSummary = "An engaging story.";
        if (chapters.isEmpty()) {
            chapters.add(new AiService.ChapterInfo(1, "Full Text", text));
        }

        // Find or create genre
        Genre genre = genreRepository.findByName(finalGenre)
                .orElseGet(() -> {
                    Genre newGenre = new Genre();
                    newGenre.setName(finalGenre);
                    return genreRepository.save(newGenre);
                });

        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        Novel novel = new Novel();
        novel.setTitle(finalTitle);
        novel.setDescription(description != null ? description : "");
        novel.setSummary(finalSummary);
        novel.setAuthor(adminUser);
        novel.setGenre(genre);
        novel.setSourceType(Novel.SourceType.ADMIN);
        novel.setStatus(Novel.Status.PUBLISHED);
        novel.setCreatedAt(LocalDateTime.now());

        novel = novelRepository.save(novel);

        // Save chapters from AI analysis
        List<Chapter> savedChapters = new ArrayList<>();
        for (AiService.ChapterInfo chapterInfo : chapters) {
            Chapter chapter = new Chapter();
            chapter.setNovel(novel);
            chapter.setChapterNumber(chapterInfo.number);
            chapter.setTitle(chapterInfo.title);
            chapter.setContent(chapterInfo.content);
            chapter.setWordCount(chapterInfo.content.split("\\s+").length);
            chapter.setCreatedAt(LocalDateTime.now());
            chapterRepository.save(chapter);
            savedChapters.add(chapter);
        }

        novel.setChapters(savedChapters);
        return novel;
    }
}
