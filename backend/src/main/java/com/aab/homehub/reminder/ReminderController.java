package com.aab.homehub.reminder;

import com.aab.homehub.reminder.dto.ReminderRequest;
import com.aab.homehub.reminder.dto.ReminderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/reminder")
public class ReminderController {

    private final ReminderService reminderService;

    //reminders — trigger 2 mins from now 201, sent: false
    @PostMapping
    public ResponseEntity<ReminderResponse> postReminder(@Valid @RequestBody ReminderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.postReminder(request));
    }
    /*
    #CallExpected
    2 POST /reminders — recurring DAILY201, recurrencePattern: DAILY
    5 POST /reminders — trigger in the past400, validation error
    8 Wait for scheduler — check logsreminder processed, sent: true

     */
    //3- GET /reminders/upcomingboth reminders
    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getReminders(){
        return ResponseEntity.ok(reminderService.getAllReminders());
    }
    //  9 GET /reminders/upcomingempty — all sent
    @GetMapping("/upcoming")
    public ResponseEntity<List<ReminderResponse>> getUpcomingReminders() {
        return ResponseEntity.ok(reminderService.getUpcomingReminders());
    }

     //4- PUT /reminders/{id} — change title200, updated
    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> updateReminder(@PathVariable UUID id, @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.updateReminder(id, request));
    }

    //5- POST - /reminders - trigger in the past 400, validation error
   /* @PostMapping
    public ResponseEntity<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.createReminder(request));
    }*/

    //6 - DElETE / reminders/{id}
    @DeleteMapping
    public ResponseEntity<ReminderResponse> deleteReminder(@PathVariable UUID id){
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }

}
