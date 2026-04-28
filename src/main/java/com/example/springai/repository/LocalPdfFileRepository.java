package com.example.springai.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

/**
 * 基于 Redis Hash 的 PDF 文件映射仓库。
 * Redis Key: chat_pdf_mapping
 * Field: chatId -> filename (本地文件路径)
 */
@Slf4j
@Component
public class LocalPdfFileRepository implements FileRepository {

    private static final String KEY = "chat_pdf_mapping";
    private final StringRedisTemplate stringRedisTemplate;

    public LocalPdfFileRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean save(String chatId, Resource resource) {
        // 1.保存到本地磁盘
        String filename = resource.getFilename();
        File target = new File(Objects.requireNonNull(filename));
        if (!target.exists()) {
            try {
                Files.copy(resource.getInputStream(), target.toPath());
            } catch (IOException e) {
                log.error("Failed to save PDF resource.", e);
                return false;
            }
        }
        // 2.保存映射关系到 Redis
        stringRedisTemplate.opsForHash().put(KEY, chatId, filename);
        log.info("Saved PDF mapping: chatId={}, file={}", chatId, filename);
        return true;
    }

    @Override
    public Resource getFile(String chatId) {
        Object filename = stringRedisTemplate.opsForHash().get(KEY, chatId);
        if (filename == null) {
            log.warn("No PDF mapping found for chatId={}", chatId);
            return new FileSystemResource(new File(""));
        }
        return new FileSystemResource((String) filename);
    }
}