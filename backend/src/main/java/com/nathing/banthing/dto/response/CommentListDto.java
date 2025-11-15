package com.nathing.banthing.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CommentListDto {
    private List<CommentReadDto> comments;
    private Integer totalCount;
}