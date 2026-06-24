package com.aab.homehub.todo;

import com.aab.homehub.todo.dto.TodoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TodoMapper {

    @Mapping(target = "assignedToName", expression = "java(todoItem.getAssignedTo() != null ? todoItem.getAssignedTo().getFirstName() + \" \" + todoItem.getAssignedTo().getLastName() : null)")
    TodoResponse toResponse(TodoItem todoItem);
}
