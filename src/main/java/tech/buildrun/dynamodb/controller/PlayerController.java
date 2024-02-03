package tech.buildrun.dynamodb.controller;

import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import tech.buildrun.dynamodb.entity.PlayerHistory;

import java.util.List;

@RestController
@RequestMapping("/v1/players")
public class PlayerController {

    private final DynamoDbTemplate dynamoDbTemplate;

    public PlayerController(DynamoDbTemplate dynamoDbTemplate) {
        this.dynamoDbTemplate = dynamoDbTemplate;
    }

    @PostMapping("/{playerId}/games")
    public void create(@PathVariable("playerId") String playerId,
                       @RequestBody ScoreDto scoreDto) {
        var playerHistory = PlayerHistory.fromScore(playerId, scoreDto);
        dynamoDbTemplate.save(playerHistory);
    }

    @GetMapping("/{playerId}/games")
    public ResponseEntity<List<PlayerHistory>> listGames(@PathVariable("playerId") String playerId) {

        var key = Key.builder().partitionValue(playerId).build();

        var conditional = QueryConditional.keyEqualTo(key);

        var playerHistoryList = dynamoDbTemplate.query(QueryEnhancedRequest.builder()
                        .queryConditional(conditional).build(),
                PlayerHistory.class);

        return ResponseEntity.ok(playerHistoryList.items().stream().toList());

    }

    @GetMapping("/{playerId}/games/{gameId}")
    public ResponseEntity<PlayerHistory> getById(@PathVariable("playerId") String playerId,
                                                 @PathVariable("gameId") String gameId) {
        var user = dynamoDbTemplate.load(Key.builder()
                        .partitionValue(playerId)
                        .sortValue(gameId)
                        .build(), PlayerHistory.class);

        return user == null ?
                ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }
    @DeleteMapping("/{playerId}/games/{gameId}")
    public ResponseEntity<Void> delete(@PathVariable("playerId") String playerId,
                                       @PathVariable("gameId") String gameId) {
        var key = Key.builder()
                .partitionValue(playerId)
                .sortValue(gameId)
                .build();

        var player = dynamoDbTemplate.load(key, PlayerHistory.class);

        if (player == null) {
            return ResponseEntity.notFound().build();
        }

        dynamoDbTemplate.delete(key, PlayerHistory.class);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{playerId}/games/{gameId}")
    public ResponseEntity<Void> update(@PathVariable("playerId") String playerId,
                                       @PathVariable("gameId") String gameId,
                                       @RequestBody ScoreDto scoreDto) {
        var key = Key.builder()
                .partitionValue(playerId)
                .sortValue(gameId)
                .build();

        var player = dynamoDbTemplate.load(key, PlayerHistory.class);

        if (player == null) {
            return ResponseEntity.notFound().build();
        }

        player.setScore(scoreDto.score());

        dynamoDbTemplate.save(player);

        return ResponseEntity.noContent().build();
    }
}
