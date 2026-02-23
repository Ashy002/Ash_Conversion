package com.Ash_Conversion.model.entity;

import com.Ash_Conversion.model.enums.ConversionStatus;
import com.Ash_Conversion.model.enums.ConversionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_jobs")
public class FileJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "conversion_type", nullable = false, length = 20)
    private ConversionType conversionType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversionStatus status;
    
    @Column(name = "output_filename", length = 255)
    private String outputFilename;
    
    @Column(name = "output_path", length = 500)
    private String outputPath;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    public FileJob() {
        this.status = ConversionStatus.UPLOADED;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getOriginalFilename() {
        return originalFilename;
    }
    
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
    public String getStoredFilename() {
        return storedFilename;
    }
    
    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public ConversionType getConversionType() {
        return conversionType;
    }
    
    public void setConversionType(ConversionType conversionType) {
        this.conversionType = conversionType;
    }
    
    public ConversionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ConversionStatus status) {
        this.status = status;
    }
    
    public String getOutputFilename() {
        return outputFilename;
    }
    
    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }
    
    public String getOutputPath() {
        return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
    
    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}

