package com.Ash_Conversion.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "share_tokens")
public class ShareToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "file_job_id", nullable = false)
    private FileJob fileJob;
    
    @Column(unique = true, nullable = false, length = 64)
    private String token;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "access_count")
    private Integer accessCount;
    
    @Column(name = "max_access")
    private Integer maxAccess;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public ShareToken() {
        this.accessCount = 0;
        this.maxAccess = 10;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public FileJob getFileJob() {
        return fileJob;
    }
    
    public void setFileJob(FileJob fileJob) {
        this.fileJob = fileJob;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public Integer getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }
    
    public Integer getMaxAccess() {
        return maxAccess;
    }
    
    public void setMaxAccess(Integer maxAccess) {
        this.maxAccess = maxAccess;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

