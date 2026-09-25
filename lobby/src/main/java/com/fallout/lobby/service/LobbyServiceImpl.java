package com.fallout.lobby.service;

import com.fallout.core.enums.GameStatus;
import com.fallout.lobby.entity.SessionEntity;
import com.fallout.lobby.entity.SessionPlayerEntity;
import com.fallout.lobby.repository.GameSessionRepository;
import com.fallout.lobby.repository.SessionPlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LobbyServiceImpl implements LobbyService {

    private final GameSessionRepository sessionRepository;
    private final SessionPlayerRepository playerRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final BroadcastClient broadcastClient;
    private final Random random = new Random();

    @Override
    @Transactional
    public Mono<SessionEntity> createSession() {
        log.info("Creating new game session");
        SessionEntity session = SessionEntity.builder()
                .id(UUID.randomUUID())
                .joinCode(generateJoinCode())
                .status(GameStatus.WAITING.name())
                .maxPlayers(4)
                .currentTurn(0)
                .createdAt(OffsetDateTime.now())
                .build();

        return sessionRepository.save(session)
                .doOnSuccess(s -> log.info("Session created with code: {}", s.getJoinCode()));
    }

    @Override
    @Transactional
    public Mono<SessionPlayerEntity> joinSession(UUID sessionId, SessionPlayerEntity player) {
        log.info("Player {} attempting to join session {}", player.getDisplayName(), sessionId);

        return sessionRepository.findById(sessionId)
                .filter(s -> "WAITING".equals(s.getStatus()))
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found or not in WAITING state")))
                .flatMap(session -> {
                    player.setSessionId(sessionId);
                    return playerRepository.save(player);
                })
                .flatMap(savedPlayer -> playerRepository.countBySessionId(sessionId)
                        .flatMap(count -> {
                            log.info("Session {} has now {}/{} players", sessionId, count, 4);
                            if (count >= 4) {
                                return startGame(sessionId).then(Mono.just(savedPlayer));
                            }
                            return Mono.just(savedPlayer);
                        })
                );
    }

    @Override
    @Transactional
    public Mono<Void> startGame(UUID sessionId) {
        log.info("Triggering game start for session {}", sessionId);

        return sessionRepository.findById(sessionId)
                .flatMap(session -> {
                    session.setStatus(GameStatus.IN_PROGRESS.name());
                    session.setStartedAt(OffsetDateTime.now());
                    return sessionRepository.save(session);
                })
                .flatMap(this::initializeRedisState)
                .then(broadcastClient.notifySessionStart(sessionId))
                .doOnSuccess(v -> log.info("Game session {} successfully started and initialized in Redis", sessionId));
    }

    @Override
    public Mono<SessionEntity> getSession(UUID sessionId) {
        log.info("Fetching session info for ID: {}", sessionId);
        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Session not found")));
    }

    @Override
    public Flux<SessionEntity> getWaitingSessions() {
        log.info("Fetching all sessions in WAITING state");
        return sessionRepository.findByStatus(GameStatus.WAITING.name());
    }

    @Override
    @Transactional
    public Mono<SessionPlayerEntity> disconnectFromSession(SessionPlayerEntity playerEntity) {

        log.info("Triggering game disconnection for player {}", playerEntity.getId());

        UUID sessionId = playerEntity.getSessionId();
        if (sessionId == null) {
            return Mono.error(new IllegalArgumentException("Player is not associated with any session"));
        }

        return this.sessionRepository.findById(sessionId)
                .filter(s -> "IN_PROGRESS".equals(s.getStatus()))
                .switchIfEmpty(Mono.error(new RuntimeException("Active session not found")))
                .flatMap(sessionEntity -> {
                    playerEntity.setLeftAt(OffsetDateTime.now());
                    return playerRepository.save(playerEntity);
                });
    }

    private Mono<Void> initializeRedisState(SessionEntity session) {
        String sessionId = session.getId().toString();

        // 1. Initialize Meta
        Map<String, String> meta = Map.of(
                "status", GameStatus.SESSION_STARTED.name(),
                "turn", "0",
                "startedAt", OffsetDateTime.now().toString()
        );

        Mono<Void> metaMono = redisTemplate.opsForHash()
                .putAll("game:" + sessionId + ":meta", meta)
                .then();

        // 2. Initialize Players
        return playerRepository.findBySessionId(session.getId())
                .flatMap(player -> {
                    Map<String, String> playerState = Map.of(
                            "name", player.getDisplayName(),
                            "resources", "100",
                            "totalUnits", "10"
                    );
                    return redisTemplate.opsForHash()
                            .putAll("game:" + sessionId + ":player:" + player.getDisplayPlayerId(), playerState);
                })
                .then(metaMono);
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
