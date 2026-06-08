package com.aab.homehub.todo;

import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.family.FamilyRepository;
import com.aab.homehub.todo.dto.TodoRequest;
import com.aab.homehub.todo.dto.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoMapper todoMapper;
    private final FamilyRepository familyRepository;

    public TodoResponse createTodo(TodoRequest todoRequest) {
        /*if(!userRepository.existsById(todoRequest.assignedToUserId())) {
            throw new IllegalArgumentException("User not found with id: " + todoRequest.assignedToUserId());
        }*/
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        FamilyGroup familyGroup = currentUser.getFamilyGroup();
        if (familyGroup == null) {
            throw new IllegalStateException("User does not belong to a family");
        }

       // User user = userRepository.findById(todoRequest.assignedToUserId()).get();
           TodoItem todoItem = TodoItem.builder()
                   .title(todoRequest.title())
                   .description(todoRequest.description())
                   .scope(todoRequest.scope())
                   .priority(todoRequest.priority())
                   .dueDate(todoRequest.dueDate())
                   .assignedTo(currentUser)
                   .familyGroup(familyGroup)
                   .build();
           TodoItem savedTodo = todoRepository.save(todoItem);
           return todoMapper.toResponse(savedTodo);
    }

    public List<TodoResponse> getTodosByFamilyId(UUID familyId) {
        if(familyRepository.findById(familyId).isEmpty()){
            throw new IllegalArgumentException("Family not found with id: " + familyId);
        }
        List<TodoItem> todoItemList = todoRepository.findAllByFamilyId(familyId);
        return todoItemList.stream()
                .map(todoMapper::toResponse)
                .toList();
    }

    public List<TodoResponse> getTodosByFamilyIdAndScope(UUID familyId, TodoScope scope) {
        if(familyRepository.findById(familyId).isEmpty()){
            throw new IllegalArgumentException("Family not found with id: " + familyId);
        }
        List<TodoItem> todoItemList = todoRepository.findAllByFamilyIdAndScope(familyId, scope);
        return todoItemList.stream()
                .map(todoMapper::toResponse)
                .toList();
    }


    public TodoResponse updateCompletion(UUID id, boolean toogle) {
        TodoItem todoItem = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id));
        todoItem.setCompleted(toogle);
        todoRepository.save(todoItem);
        return todoMapper.toResponse(todoItem);
    }

    public TodoResponse updatePriority(UUID id, Priority priority) {
        TodoItem todoItem = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id));
        todoItem.setPriority(priority);
        todoRepository.save(todoItem);
        return todoMapper.toResponse(todoItem);
    }

    public void deleteTodo(UUID id) {
        TodoItem todo = todoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found with id: " + id));

        todoRepository.delete(todo);
    }
}
