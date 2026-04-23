package com.example.springai.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 督查处理记录表
 * </p>
 *
 * @author huge
 * @since 2025-03-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("SUPERVISION_HANDLE_RECORD")
public class SupervisionHandleRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 id
     */
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 督查类型 id-关联督查记录表
     */
    private String supervisionRecordId;

    /**
     * 状态类型：0 抓拍，1 下发，2 反馈，3 审核，4 转发，5 协同，6 催办
     */
    private Integer handleType;

    /**
     * 处理类型名称
     */
    private String handleTypeName;

    /**
     * 督查单位 id，关联 chat_org 表 id
     */
    private String supervisionOrgId;

    /**
     * 被督查单位 id，关联 chat_org 表 id
     */
    private String supervisedOrgId;

    /**
     * 协同单位 id-关联 chat_user 表 id
     */
    private String assistOrgId;

    /**
     * 督查用户 id，关联 chat_user 表 id
     */
    private String supervisionUserId;

    /**
     * 处理问题用户 id，关联 chat_user 表 id
     */
    private String handleUserId;

    /**
     * 处理问题描述
     */
    private String handleDescription;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 审核结果：3-已办结，4-已驳回（仅审核时有效）
     */
    private Integer auditResult;

    /**
     * 协同附件 URL
     */
    private String attachmentFileUrl;

    /**
     * 协同附件名称
     */
    private String attachmentFileName;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 更新人
     */
    private String updatedBy;

}
