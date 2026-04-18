package org.broken.arrow.library.chunk.tracking.handlers;

import org.broken.arrow.library.chunk.tracking.ChunkKey;
import org.broken.arrow.library.chunk.tracking.event.status.ChunkStatus;
import org.bukkit.Chunk;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@FunctionalInterface
public interface ChunkAccessHandler {

    void onChunkAccess(@Nonnull final ChunkKey chunkKey, @Nullable final Chunk chunk, @Nonnull final ChunkStatus state);

}