package org.broken.arrow.library.menu.utility.metadata;

import javax.annotation.Nonnull;

public final class MenuMetaKeyIdentifier implements MetadataKey {
    private final MenuMetadataKey baseKey;
    private final int id;

    /**
     * Creates a custom metadata key identifier based on a predefined {@link MenuMetadataKey}.
     * This is useful when you need to distinguish different modes or usages of a single key.
     *
     * @param baseKey the base metadata key to associate with
     * @param id the custom ID to distinguish the key variation
     */
    public MenuMetaKeyIdentifier(@Nonnull final MenuMetadataKey baseKey, final int id) {
        this.baseKey = baseKey;
        this.id = id;
    }

    @Nonnull
    @Override
    public MenuMetadataKey getBaseKey() {
        return baseKey;
    }

    @Override
    public int getId() {
        return id;
    }
}