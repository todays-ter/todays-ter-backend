package com.umc.todayter.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.enums.RecordType;

import java.util.Map;

public record RecordIdResponse(
        @JsonIgnore Long id,
        @JsonIgnore RecordType type
) {
    // 명세상 id 필드명이 type에 따라 recordId/reviewId로 달라져야 해서, 고정 필드 대신 동적 키로 내려줌
    @JsonAnyGetter
    public Map<String, Object> idField() {
        String key = type == RecordType.REVIEW ? "reviewId" : "recordId";
        return Map.of(key, id);
    }

    public static RecordIdResponse from(VisitRecord visitRecord) {
        return new RecordIdResponse(visitRecord.getId(), visitRecord.getType());
    }
}
