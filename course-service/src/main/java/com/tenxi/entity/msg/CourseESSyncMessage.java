package com.tenxi.entity.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseESSyncMessage {
    private Long courseId;
    private Operation operation;

    public enum Operation {
        CREATE, UPDATE, DELETE
    }
}
