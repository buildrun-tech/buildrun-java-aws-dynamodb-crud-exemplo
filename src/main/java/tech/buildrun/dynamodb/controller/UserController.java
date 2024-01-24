package tech.buildrun.dynamodb.controller;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import tech.buildrun.dynamodb.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final DynamoDbTemplate dynamoDbTemplate;

    public UserController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @GetMapping
    public ResponseEntity<List<User>> listAll() {

        var users = dynamoDbTemplate.scanAll(User.class).items().stream().toList();

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public void create(@RequestBody User user) {
        user.setId(UUID.randomUUID());
        dynamoDbTemplate.save(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") UUID id) {
        var user = dynamoDbTemplate.load(Key.builder()
                        .partitionValue(id.toString())
                        .build(), User.class);

        return user == null ?
                ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }
    @PutMapping("/{id}")
    public void update(@PathVariable("id") UUID id,
                       @RequestBody User user) {
        user.setId(id);
        dynamoDbTemplate.update(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        var key = Key.builder()
                .partitionValue(id.toString())
                .build();

        var user = dynamoDbTemplate.load(key, User.class);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        dynamoDbTemplate.delete(key, User.class);

        return ResponseEntity.noContent().build();
    }
}
