package com.bluenet.web.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "分页响应DTO")
public class PageDTO<T> {

    @Schema(description = "内容列表")
    private final List<T> content;

    @Schema(description = "总元素数")
    private final long totalElements;

    @Schema(description = "总页数")
    private final int totalPages;

    @Schema(description = "当前页码(从0开始)")
    private final int number;

    @Schema(description = "每页大小")
    private final int size;

    @Schema(description = "当前页元素数")
    private final int numberOfElements;

    @Schema(description = "是否第一页")
    private final boolean first;

    @Schema(description = "是否最后一页")
    private final boolean last;

    @Schema(description = "是否为空")
    private final boolean empty;

    @JsonCreator
    public PageDTO(
            @JsonProperty("content") List<T> content,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("numberOfElements") int numberOfElements,
            @JsonProperty("first") boolean first,
            @JsonProperty("last") boolean last,
            @JsonProperty("empty") boolean empty) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
        this.numberOfElements = numberOfElements;
        this.first = first;
        this.last = last;
        this.empty = empty;
    }

    public static <T> PageDTO<T> from(Page<T> page) {
        return new PageDTO<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty());
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isEmpty() {
        return empty;
    }
}
