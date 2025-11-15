package org.broken.arrow.library.itemcreator.meta.map.builder;

import org.broken.arrow.library.itemcreator.meta.map.utility.MapRendererBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
/**
 * Final step object providing optional builder configuration.
 *
 * <p>At this point, all renderer or cached-layer changes should already be completed.</p>
 */
public class FinalRenderStep {
    private final MapRendererBuilder mapRendererBuilder;

    /**
     * Constructs the final step for render configuration.
     *
     *
     * @param mapRendererBuilder the builder instance used for automatic layer assignment.
     */
    public FinalRenderStep(@Nonnull final MapRendererBuilder mapRendererBuilder) {
        this.mapRendererBuilder = mapRendererBuilder;
    }

    /**
     * Configures the builder for automatic layer assignment.
     *
     * <p>The builder assigns layers sequentially and is aware of existing layers,
     * avoiding collisions with previously configured layers. However, manual or cached
     * layers with custom indices may still overlap layers already set on the pixel map.</p>
     *
     * @param config a consumer configuring the {@link MapRendererBuilder} instance
     */
    public void withBuilder(@Nonnull final Consumer<MapRendererBuilder> config) {
        config.accept(this.mapRendererBuilder);
    }
}