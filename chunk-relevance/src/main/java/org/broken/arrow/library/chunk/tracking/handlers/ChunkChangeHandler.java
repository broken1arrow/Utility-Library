package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.bukkit.ChunkSnapshot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface ChunkChangeHandler {

    void onChunkChange(@Nonnull final ChunkKey chunkKey, @Nullable final ChunkSnapshot chunkSnapshot, @Nonnull final ChunkStatus state);

}
