package org.broken.arrow.library.itemcreator.meta.map.builder;

import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.itemcreator.meta.map.cache.MapRendererDataCache;
import org.broken.arrow.library.itemcreator.meta.map.utility.MapRendererBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
/**
 * Entry point for configuring a {@link MapRendererData}.
 *
 * <p>This class allows optional configuration of renderer layers, cached layers,
 * and builder-based automatic layer assignment. All steps are optional and may
 * be called in any order, but calling multiple layer-setting methods on the same
 * indices may overwrite existing layers.</p>
 */
public class RenderConfigurator {

    private final MapRendererData renderer;
    private final MapRendererBuilder mapRendererBuilder;

    /**
     * Creates a new configurator for a given renderer and builder.
     *
     * @param renderer the renderer instance to configure
     * @param mapRendererBuilder the builder instance used for automatic layer assignment
     */
    public RenderConfigurator(@Nonnull final MapRendererData renderer, @Nonnull final MapRendererBuilder mapRendererBuilder) {
        this.renderer = renderer;
        this.mapRendererBuilder = mapRendererBuilder;
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
    public RendererStepAfter withRenderer(@Nonnull final Consumer<MapRendererData> config) {
        config.accept(this.renderer);
        return new RendererStepAfter(this.renderer, this.mapRendererBuilder);
    }

    /**
     * Loads a cached layer from a {@link MapRendererDataCache} into the renderer.
     *
     * <p>Multiple calls on the same layer index overwrite previous data and may override
     * what was set using {@link #withRenderer(Consumer)}.</p>
     *
     * @param layer the layer index to replace
     * @param cacheId the identifier of the cached pixel data
     * @param cache the cache providing pixel data
     * @return a step object allowing renderer or builder configuration
     */
    public RendererStepAfterCache withCachedLayer(final int layer, final int cacheId, @Nonnull final MapRendererDataCache cache) {
        cache.setLayerToRender(layer,cacheId ,this.renderer);
        return new RendererStepAfterCache(renderer, mapRendererBuilder);
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