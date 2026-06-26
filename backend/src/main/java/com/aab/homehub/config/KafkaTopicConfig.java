package com.aab.homehub.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String MEAL_PLANNED         = "meal.planned";
    public static final String REMINDER_TRIGGERED   = "reminder.triggered";
    public static final String AI_REQUESTED         = "ai.requested";
    public static final String TODO_COMPLETED       = "todo.completed";
    public static final String NUTRITION_ALERT      = "nutrition.alert";
    public static final String PANTRY_LOW           = "pantry.low";

    @Bean
    public NewTopic mealPlanned() {
        return TopicBuilder.name(MEAL_PLANNED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic reminderTriggered() {
        return TopicBuilder.name(REMINDER_TRIGGERED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic aiRequested() {
        return TopicBuilder.name(AI_REQUESTED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic todoCompleted() {
        return TopicBuilder.name(TODO_COMPLETED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic nutritionAlert() {
        return TopicBuilder.name(NUTRITION_ALERT).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic pantryLow() {
        return TopicBuilder.name(PANTRY_LOW).partitions(1).replicas(1).build();
    }
}
