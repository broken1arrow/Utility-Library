package org.broken.arrow.utility.library.chunk.tracker;

import org.broken.arrow.library.chunk.tracking.ChunkRelevanceTracker;
import org.broken.arrow.library.chunk.tracking.chunk.ChunkEntry;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Consumer;

public class ChunkRelevanceTrackerWrapper extends ChunkRelevanceTracker {

    /**
     * Creates and initializes the chunk relevance tracker.
     *
     * <p>This sets up internal trackers, registers event listeners, and starts
     * the background dispatcher responsible for propagating chunk updates.</p>
     *
     * @param plugin the owning plugin instance
     */
    public ChunkRelevanceTrackerWrapper(@NonNull Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void registerListener(@NonNull Plugin plugin) {}

    @Override
    public void processChunkState(@NonNull ChunkKey chunkKey, @Nullable Chunk chunk, @Nullable ChunkStatus chunkStatus, @NonNull Consumer<ChunkEntry> callback) {
        super.processChunkState(chunkKey, chunk, chunkStatus, callback);
    }

    @Override
    public void processChunkState(@NonNull ChunkKey chunkKey, @Nullable ChunkSnapshot snapshot, @Nullable ChunkStatus chunkStatus, @NonNull Consumer<ChunkEntry> callback) {
        super.processChunkState(chunkKey, snapshot, chunkStatus, callback);
    }

}
