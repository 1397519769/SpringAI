package com.example.springai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.example.springai.entity.po.SupervisionHandleRecord;
import com.example.springai.entity.po.SupervisionRecord;
import com.example.springai.entity.query.SupervisionRecordQuery;
import com.example.springai.service.ISupervisionHandleRecordService;
import com.example.springai.service.ISupervisionRecordService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 督查记录工具类
 */
@Component
public class SupervisionTools {

    @Autowired
    private ISupervisionRecordService supervisionRecordService;

    @Autowired
    private ISupervisionHandleRecordService handleRecordService;

    @Tool(description = "根据条件查询督查记录")
    public List<SupervisionRecord> querySupervisionRecord(
            @ToolParam(description = "查询条件", required = false) SupervisionRecordQuery query) {
        if (query == null) {
            return supervisionRecordService.list();
        }

        QueryChainWrapper<SupervisionRecord> wrapper = supervisionRecordService.query()
                .eq(query.getSupervisionId() != null, "SUPERVISION_ID", query.getSupervisionId())
                .eq(query.getViolationId() != null, "VIOLATION_ID", query.getViolationId())
                .eq(query.getAttentionLevelId() != null, "ATTENTION_LEVEL_ID", query.getAttentionLevelId())
                .eq(query.getSupervisionOrgId() != null, "SUPERVISION_ORG_ID", query.getSupervisionOrgId())
                .eq(query.getSupervisedOrgId() != null, "SUPERVISED_ORG_ID", query.getSupervisedOrgId())
                .eq(query.getTaskId() != null, "TASK_ID", query.getTaskId())
                .eq(query.getStatusType() != null, "STATUS_TYPE", query.getStatusType())
                .eq(query.getSupervisionUserId() != null, "SUPERVISION_USER_ID", query.getSupervisionUserId());

        if (query.getSorts() != null && !query.getSorts().isEmpty()) {
            for (SupervisionRecordQuery.Sort sort : query.getSorts()) {
                wrapper.orderBy(true, sort.getAsc(), sort.getField());
            }
        }

        return wrapper.list();
    }

    @Tool(description = "根据督查记录 ID 查询对应的督查处理记录")
    public List<SupervisionHandleRecord> queryHandleRecordBySupervisionId(
            @ToolParam(description = "督查记录 ID", required = true) String supervisionRecordId) {
        return handleRecordService.query()
                .eq("SUPERVISION_RECORD_ID", supervisionRecordId)
                .list();
    }

    @Tool(description = "新增督查处理记录")
    public String createHandleRecord(
            @ToolParam(description = "督查记录 ID", required = true) String supervisionRecordId,
            @ToolParam(description = "处理类型：0 抓拍，1 下发，2 反馈，3 审核，4 转发，5 协同，6 催办", required = true) Integer handleType,
            @ToolParam(description = "处理类型名称", required = false) String handleTypeName,
            @ToolParam(description = "督查单位 ID", required = false) String supervisionOrgId,
            @ToolParam(description = "被督查单位 ID", required = false) String supervisedOrgId,
            @ToolParam(description = "协同单位 ID", required = false) String assistOrgId,
            @ToolParam(description = "督查用户 ID", required = false) String supervisionUserId,
            @ToolParam(description = "处理问题用户 ID", required = false) String handleUserId,
            @ToolParam(description = "处理问题描述", required = false) String handleDescription,
            @ToolParam(description = "审核结果：3-已办结，4-已驳回", required = false) Integer auditResult,
            @ToolParam(description = "协同附件 URL", required = false) String attachmentFileUrl,
            @ToolParam(description = "协同附件名称", required = false) String attachmentFileName,
            @ToolParam(description = "创建人", required = false) String createdBy) {

        SupervisionHandleRecord record = new SupervisionHandleRecord();
        record.setSupervisionRecordId(supervisionRecordId);
        record.setHandleType(handleType);
        record.setHandleTypeName(handleTypeName);
        record.setSupervisionOrgId(supervisionOrgId);
        record.setSupervisedOrgId(supervisedOrgId);
        record.setAssistOrgId(assistOrgId);
        record.setSupervisionUserId(supervisionUserId);
        record.setHandleUserId(handleUserId);
        record.setHandleDescription(handleDescription);
        record.setHandleTime(LocalDateTime.now());
        record.setAuditResult(auditResult);
        record.setAttachmentFileUrl(attachmentFileUrl);
        record.setAttachmentFileName(attachmentFileName);
        record.setCreatedBy(createdBy);
        record.setCreatedTime(LocalDateTime.now());

        handleRecordService.save(record);
        return record.getId();
    }
}
