package org.broken.arrow.library.itemcreator.meta.map.builder;

import org.broken.arrow.library.itemcreator.meta.map.MapRendererData;
import org.broken.arrow.library.itemcreator.meta.map.cache.MapRendererDataCache;
import org.broken.arrow.library.itemcreator.meta.map.utility.MapRendererBuilder;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Step object returned after configuring the renderer directly.
 *
 * <p>Allows optional loading of cached layers or builder configuration.</p>
 */
public class RendererStepAfter {

    private final MapRendererData renderer;
    private final MapRendererBuilder mapRendererBuilder;
    private final FinalRenderStep finalRenderStep;

    /**
     * Creates a step after configuring the renderer.
     *
     * @param renderer the renderer instance configured in the previous step
     * @param mapRendererBuilder the builder instance used for automatic layer assignment
     */
    public RendererStepAfter(@Nonnull final MapRendererData renderer, @Nonnull final MapRendererBuilder mapRendererBuilder) {
        this.renderer = renderer;
        this.mapRendererBuilder = mapRendererBuilder;
        this.finalRenderStep = new FinalRenderStep(mapRendererBuilder);
    }

    /**
     * Loads a cached layer from a {@link MapRendererDataCache} into the renderer.
     *
     * <p>Multiple calls on the same layer index overwrite previous data and may override
     * what was set using {@link  RenderConfigurator#withRenderer(Consumer)}.</p>
     *
     * @param layer the layer index to replace
     * @param cacheId the identifier of the cached pixel data
     * @param cache the cache providing pixel data
     * @return a step object allowing renderer or builder configuration
     */
    public FinalRenderStep withCachedLayer(final int layer, final int cacheId, @Nonnull final MapRendererDataCache cache) {
        cache.setLayerToRender(layer,cacheId ,this.renderer);
        return this.finalRenderStep;
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