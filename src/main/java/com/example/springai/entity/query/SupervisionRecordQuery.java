package com.example.springai.entity.query;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

@Data
public class SupervisionRecordQuery {

    @ToolParam(required = false, description = "督查类型 ID")
    private String supervisionId;

    @ToolParam(required = false, description = "违规类型 ID")
    private String violationId;

    @ToolParam(required = false, description = "关注等级 ID")
    private String attentionLevelId;

    @ToolParam(required = false, description = "督查单位 ID")
    private String supervisionOrgId;

    @ToolParam(required = false, description = "被督查单位 ID")
    private String supervisedOrgId;

    @ToolParam(required = false, description = "任务 ID")
    private String taskId;

    @ToolParam(required = false, description = "状态类型：0 待下发，1 待整改，2 已整改待审核，3 已驳回")
    private Integer statusType;

    @ToolParam(required = false, description = "督查用户 ID")
    private String supervisionUserId;

    @ToolParam(required = false, description = "排序方式")
    private List<Sort> sorts;

    @Data
    public static class Sort {
        @ToolParam(required = false, description = "排序字段")
        private String field;
        @ToolParam(required = false, description = "是否是升序：true/false")
        private Boolean asc;
    }
}
