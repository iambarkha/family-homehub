package com.aab.homehub.todo;

import com.aab.homehub.todo.dto.TodoRequest;
import com.aab.homehub.todo.dto.TodoResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    //Create todo
    @PostMapping
    public ResponseEntity<TodoResponse> save(@Valid @RequestBody TodoRequest todoRequest) {
        TodoResponse savedTodo = todoService.createTodo(todoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTodo);
    }

    //Get all todos for a family
    @GetMapping("/family/{familyId}")
    public ResponseEntity<List<TodoResponse>> getTodosByFamilyId(@PathVariable UUID familyId) {
        return ResponseEntity.ok(todoService.getTodosByFamilyId(familyId));
    }

    //Filter Todos by scope
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getTodosByFamilyIdAndScope(@RequestParam UUID familyId, @RequestParam TodoScope scope) {
        return ResponseEntity.ok(todoService.getTodosByFamilyIdAndScope(familyId, scope));
    }

    //Mark complete or incomplete a todo item
    @PatchMapping("/id/{id}/toggle/{toggle}")
    public ResponseEntity<TodoResponse> updateCompletion(@PathVariable UUID id, @PathVariable boolean toggle) {
        return ResponseEntity.ok(todoService.updateCompletion(id, toggle));
    }

    //Change priority
    @PatchMapping("/id/{id}/priority/{priority}")
    public ResponseEntity<TodoResponse> updatePriority(@PathVariable UUID id, @PathVariable Priority priority) {
        return ResponseEntity.ok(todoService.updatePriority(id, priority));
    }

    //delete a todo
    @DeleteMapping
    public ResponseEntity<Void> deleteTodo(@PathVariable UUID id) {
        todoService.deleteTodo(id);
        return ResponseEntity.noContent().build();
    }

}
