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
public class RendererStepAfter extends FinalRenderStep {

    private final MapRendererData renderer;

    /**
     * Creates a step after configuring the renderer.
     *
     * @param renderer the renderer instance configured in the previous step
     * @param mapRendererBuilder the builder instance used for automatic layer assignment
     */
    public RendererStepAfter(@Nonnull final MapRendererData renderer, @Nonnull final MapRendererBuilder mapRendererBuilder) {
        super(mapRendererBuilder);
        this.renderer = renderer;
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
        return this;
    }

}