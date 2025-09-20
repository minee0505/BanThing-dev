package com.nathing.banthing.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentCreateDto {
    private String content;

    public void setMeetingId(Long meetingId) {

    }
}