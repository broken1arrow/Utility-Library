package org.broken.arrow.library.chunk.tracking.utility;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.broken.arrow.library.chunk.tracking.handlers.ChunkChangeHandler;
import org.bukkit.ChunkSnapshot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkState {
    private final ChunkKey key;
    private final ChunkSnapshot snapshot;
    private final ChunkStatus state;
    private final ChunkChangeHandler handler;

    private ChunkState(@Nonnull final ChunkKey key, @Nullable final ChunkSnapshot snapshot, @Nonnull final ChunkStatus state, @Nonnull final ChunkChangeHandler handler) {
        this.key = key;
        this.snapshot = snapshot;
        this.state = state;
        this.handler = handler;
    }

    public static ChunkState of(@Nonnull final ChunkKey key, @Nullable final ChunkSnapshot snapshot, @Nonnull final ChunkStatus state, @Nonnull final ChunkChangeHandler handler) {
        return new ChunkState(key, snapshot, state, handler);
    }

    public ChunkKey getKey() {
        return key;
    }

    public void apply() {
        handler.onChunkChange(key, snapshot, state);
    }
}