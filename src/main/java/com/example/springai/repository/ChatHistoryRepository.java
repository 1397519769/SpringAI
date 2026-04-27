package com.example.springai.repository;

import java.util.List;

/**
 * 聊天会话记录仓库接口
 * 用于按类型（chat/service/pdf）管理会话 ID 列表。
 */
public interface ChatHistoryRepository {
    /** 保存会话 ID，按类型分组 */
    void save(String type, String chatId);

    /** 查询指定类型的所有会话 ID */
    List<String> getChatIds(String type);
}
