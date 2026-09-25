--liquibase formatted sql

--changeset claude:001-create-lobby-tables
CREATE TABLE map_node (
    id UUID PRIMARY KEY,
    name VARCHAR(256) NOT NULL,
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('CAPITAL','BASE','CITY','RESOURCE','FARM','LAIR')),
    max_garrison INT NOT NULL DEFAULT 100,
    base_fortification INT NOT NULL DEFAULT 0,
    description TEXT,
    icon_url VARCHAR(512)
);

CREATE INDEX idx_map_node_type ON map_node (type);

CREATE TABLE map_edge (
    id BIGSERIAL PRIMARY KEY,
    source_node_id UUID NOT NULL REFERENCES map_node(id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES map_node(id) ON DELETE CASCADE,
    length DOUBLE PRECISION NOT NULL CHECK (length > 0),
    is_bidirectional BOOLEAN NOT NULL DEFAULT true,
    travel_modifier DOUBLE PRECISION NOT NULL DEFAULT 1.0 CHECK (travel_modifier > 0),
    CONSTRAINT map_edge_no_self_loop_chk CHECK (source_node_id <> target_node_id),
    CONSTRAINT map_edge_undirected_uniq UNIQUE (
        LEAST(source_node_id, target_node_id),
        GREATEST(source_node_id, target_node_id)
    )
);

CREATE INDEX idx_map_edge_source ON map_edge (source_node_id);
CREATE INDEX idx_map_edge_target ON map_edge (target_node_id);

CREATE TABLE game_session (
    id UUID PRIMARY KEY,
    join_code VARCHAR(8) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('CANCELED','WAITING','IN_PROGRESS','FINISHED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ CHECK (finished_at IS NULL OR started_at IS NULL OR finished_at >= started_at),
    max_players INT NOT NULL DEFAULT 4 CHECK (max_players BETWEEN 2 AND 4),
    current_turn INT NOT NULL DEFAULT 0 CHECK (current_turn >= 0),
    winner_player_id INT,
    map_seed VARCHAR(64)
);

CREATE INDEX idx_game_session_status ON game_session (status);
CREATE INDEX idx_game_session_active ON game_session (created_at DESC) WHERE status IN ('WAITING','IN_PROGRESS');
CREATE INDEX idx_game_session_finished ON game_session (finished_at DESC) WHERE status = 'FINISHED';

CREATE TABLE session_player (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES game_session(id) ON DELETE CASCADE,
    display_player_id INT NOT NULL CHECK (display_player_id > 0),
    display_name VARCHAR(60) NOT NULL,
    seat_order INT NOT NULL,
    color VARCHAR(16),
    is_ai BOOLEAN NOT NULL DEFAULT false,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at TIMESTAMPTZ,
    CONSTRAINT session_player_uniq_per_session UNIQUE (session_id, display_player_id),
    CONSTRAINT session_player_seat_uniq UNIQUE (session_id, seat_order)
);

CREATE INDEX idx_session_player_session ON session_player (session_id);
CREATE INDEX idx_session_player_active ON session_player (session_id) WHERE left_at IS NULL;

CREATE TABLE session_map (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE REFERENCES game_session(id) ON DELETE CASCADE,
    name VARCHAR(80) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    notes TEXT
);

CREATE TABLE session_player_snapshot (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES game_session(id) ON DELETE CASCADE,
    display_player_id INT NOT NULL CHECK (display_player_id > 0),
    display_name VARCHAR(60) NOT NULL,
    final_units INT NOT NULL DEFAULT 0,
    final_resources INT NOT NULL DEFAULT 0,
    final_nodes_controlled INT NOT NULL DEFAULT 0,
    rank INT,
    snapshot_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT session_player_snapshot_uniq UNIQUE (session_id, display_player_id)
);

CREATE INDEX idx_session_player_snapshot_session ON session_player_snapshot (session_id);

CREATE TABLE lobby_event (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES game_session(id) ON DELETE CASCADE,
    event_type VARCHAR(40) NOT NULL CHECK (event_type IN ('SESSION_CREATED','PLAYER_JOINED','PLAYER_LEFT','SESSION_STARTED','SESSION_CANCELED','SESSION_FINISHED')),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    actor_player_id INT,
    correlation_id VARCHAR(64),
    payload JSONB NOT NULL DEFAULT '{}'::JSONB
);

CREATE INDEX idx_lobby_event_session_time ON lobby_event (session_id, occurred_at DESC);
CREATE INDEX idx_lobby_event_type ON lobby_event (event_type);
CREATE INDEX idx_lobby_event_payload_gin ON lobby_event USING GIN (payload JSONB_PATH_OPS);

--rollback
--changeset claude:001-rollback-create-lobby-tables
DROP TABLE IF EXISTS lobby_event CASCADE;
DROP TABLE IF EXISTS session_player_snapshot CASCADE;
DROP TABLE IF EXISTS session_map CASCADE;
DROP TABLE IF EXISTS session_player CASCADE;
DROP TABLE IF EXISTS game_session CASCADE;
DROP TABLE IF EXISTS map_edge CASCADE;
DROP TABLE IF EXISTS map_node CASCADE;