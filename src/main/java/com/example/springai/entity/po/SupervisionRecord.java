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
 * 督查记录表
 * </p>
 *
 * @author huge
 * @since 2025-03-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("SUPERVISION_RECORD")
public class SupervisionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 督查类型 id-关联 SUPERVISION_TYPE 表
     */
    private String supervisionId;

    /**
     * 违规类型 id-关联 VIOLATION_TYPE 表
     */
    private String violationId;

    /**
     * 关注等级 - 关联 ACS_DICTINFO 表
     */
    private String attentionLevelId;

    /**
     * 督查单位 id-关联 chat_org 表
     */
    private String supervisionOrgId;

    /**
     * 被督查单位 id-关联 chat_org 表
     */
    private String supervisedOrgId;

    /**
     * 协同单位 id-关联 chat_org 表
     */
    private String assistOrgId;

    /**
     * 任务 id
     */
    private String taskId;

    /**
     * 状态类型：0 待下发，1 待整改，2 已整改待审核，3 已驳回
     */
    private Integer statusType;

    /**
     * 下发时间
     */
    private LocalDateTime sendTime;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 抓拍摄像机 id
     */
    private String cameraId;

    /**
     * 抓拍摄像机通道号
     */
    private String cameraChannel;

    /**
     * 抓拍摄像机通道类型
     */
    private String cameraChannelType;

    /**
     * 抓拍摄像机名称
     */
    private String cameraName;

    /**
     * 督察时间
     */
    private LocalDateTime superviseTime;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 督查用户 ID-关联 openvone.usr_infotab 表
     */
    private String supervisionUserId;

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
