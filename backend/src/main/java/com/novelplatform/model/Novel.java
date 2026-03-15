package com.novelplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "novels")
public class Novel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 4000)
    private String summary;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;
    
    @ManyToOne
    @JoinColumn(name = "genre_id")
    @JsonIgnore
    private Genre genre;
    
    private String coverImage;
    
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;
    
    @Enumerated(EnumType.STRING)
    private Status status;
    
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "novel", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Chapter> chapters;
    
    public enum SourceType {
        AUTHOR, ADMIN, EXTERNAL
    }
    
    public enum Status {
        DRAFT, REVIEW, PUBLISHED
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public List<Chapter> getChapters() { return chapters; }
    public void setChapters(List<Chapter> chapters) { this.chapters = chapters; }
}
