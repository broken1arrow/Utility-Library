package org.broken.arrow.library.itemcreator.meta.map.builder;

import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.itemcreator.meta.map.utility.MapRendererBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Step object returned after loading a cached layer.
 *
 * <p>Allows optional renderer configuration or builder configuration.</p>
 */
public class RendererStepAfterCache extends FinalRenderStep {
    private final MapRendererData renderer;

    /**
     * Creates a step after loading a cached layer.
     *
     * @param renderer the renderer instance receiving cached layers
     * @param mapRendererBuilder the builder instance used for automatic layer assignment
     */
    public RendererStepAfterCache(@Nonnull final MapRendererData renderer, @Nonnull final MapRendererBuilder mapRendererBuilder) {
        super(mapRendererBuilder);
        this.renderer = renderer;
    }

    /**
     * Provides direct access to the renderer’s internal data.
     *
     * <p>Users can add or replace layers of map pixels. Multiple calls may overwrite
     * previously set layers if the same indices are used. For example, if this is combined
     * with cached layer configuration using the same layer index, the previous content will be replaced.</p>
     *
     * @param config a consumer that modifies the {@link MapRendererData} instance
     * @return a step object allowing cached layer loading or builder configuration
     */
    public FinalRenderStep withRenderer(@Nonnull final Consumer<MapRendererData> config) {
        config.accept(this.renderer);
        return this;
    }

}