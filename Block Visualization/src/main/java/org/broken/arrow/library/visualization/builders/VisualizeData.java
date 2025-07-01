package org.broken.arrow.library.visualization.builders;

import org.broken.arrow.library.visualization.BlockVisualize;
import org.broken.arrow.library.visualization.utility.Function;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This class stores the data set for visualizing a block glow
 * and also optionally a text at the top of the block.
 */
public final class VisualizeData {
    private final Player viewer;
    private final Set<Player> playersAllowed;
    private FallingBlock fallingBlock;
    private final String text;
    private String permission;
    private final Material mask;
    private boolean stopIfAir;
    private boolean stopVisualizeBlock;

    /**
     * Constructs a new VisualizeData instance with the specified viewer, text, and mask.
     *
     * @param viewer the player viewing the visualization.
     * @param text   the text associated with the visualization.
     * @param mask   the material mask used in the visualization.
     */
    public VisualizeData(@Nonnull final Player viewer, @Nullable final String text, @Nonnull final Material mask) {
        this(viewer, new HashSet<>(), text, mask);
    }

    /**
     * Constructs a new VisualizeData instance with the specified viewer, allowed players, text, and mask.
     *
     * @param viewer         the player viewing the visualization.
     * @param playersAllowed the set of players allowed to see the visualization.
     * @param text           the text associated with the visualization.
     * @param mask           the material mask used in the visualization.
     */
    public VisualizeData(@Nullable final Player viewer, @Nullable final Set<Player> playersAllowed, @Nullable final String text, @Nonnull final Material mask) {
        this.viewer = viewer;
        this.playersAllowed = playersAllowed != null ? playersAllowed : new HashSet<>();
        this.text = text;
        this.mask = mask;
    }

    /**
     * Sets whether to stop the visualization if the block is air.
     *
     * @param stopIfAir true if the visualization should stop when the block is air, false otherwise.
     */
    public void setStopIfAir(final boolean stopIfAir) {
        this.stopIfAir = stopIfAir;
    }


    /**
     * Adds a player to the set of players allowed to see the visualization.
     * <p>&nbsp;</p>
     * Due to some limits, it will not be fully hidden from other players.
     * Only the glow effect will be shown for the players with the permission.
     *
     * @param viewer the player to be added.
     */
    public void addPlayersAllowed(final Player viewer) {
        playersAllowed.add(viewer);
    }

    /**
     * Sets the permission required to view the visualization.
     * <p>&nbsp;</p>
     * Due to some limits, it will not be fully hidden from other players.
     * Only the glow effect will be shown for the players with the permission.
     *
     * @param permission the permission string.
     */
    public void setPermission(final String permission) {
        this.permission = permission;
    }

    /**
     * Sets the falling block associated with the visualization.
     * <p>&nbsp;</p>
     * <p><b>Note:</b> You don't need to set this value manually, as it will be set automatically.</p>
     *
     * @param fallingBlock the falling block to be set.
     */
    public void setFallingBlock(final FallingBlock fallingBlock) {
        this.fallingBlock = fallingBlock;
    }

    /**
     * Sets whether to stop visualizing the block.
     * <p>&nbsp;</p>
     * Recommends using {@link BlockVisualize#stopVisualizing(Block)}
     * or {@link BlockVisualize#visualizeBlock(Player, Block, Function, boolean)}
     * and setting the last argument to false.
     *
     * @param stopVisualizeBlock true if the visualization should stop, false otherwise.
     */
    public void setStopVisualizeBlock(boolean stopVisualizeBlock) {
        this.stopVisualizeBlock = stopVisualizeBlock;
    }

    /**
     * Gets the permission required to view the visualization.
     *
     * @return the permission string, or null if no permission is set.
     */
    @Nullable
    public String getPermission() {
        return permission;
    }

    /**
     * Gets the player viewing the visualization.
     * If it only one player that should see the
     * visualization.
     *
     * @return the viewer, or null if not set.
     */
    @Nullable
    public Player getViewer() {
        return viewer;
    }

    /**
     * Gets the set of players allowed to see the visualization.
     *
     * @return the set of allowed players.
     */
    @Nonnull
    public Set<Player> getPlayersAllowed() {
        return playersAllowed;
    }

    /**
     * Gets the falling block associated with the visualization.
     *
     * @return the falling block, or null if not set.
     */
    public FallingBlock getFallingBlock() {
        return fallingBlock;
    }

    /**
     * Removes the falling block from the visualization if it exists.
     */
    public void removeFallingBlock() {
        if (fallingBlock != null)
            fallingBlock.remove();
    }

    /**
     * Gets the text associated with the visualization.
     *
     * @return the text, or null if no text is set.
     */
    @Nullable
    public String getText() {
        return text;
    }

    /**
     * Gets the material mask that player sees in the visualization.
     *
     * @return the material mask.
     */
    @Nonnull
    public Material getMask() {
        return mask;
    }

    /**
     * Checks if the visualization should stop when the block is air.
     *
     * @return true if the visualization should stop, false otherwise.
     */
    public boolean isStopIfAir() {
        return stopIfAir;
    }

    /**
     * Checks if the visualization should stop.
     *
     * @return true if the visualization should stop, false otherwise.
     */
    public boolean isStopVisualizeBlock() {
        return stopVisualizeBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VisualizeData that = (VisualizeData) o;

        if (stopIfAir != that.stopIfAir) return false;
        if (stopVisualizeBlock != that.stopVisualizeBlock) return false;
        if (!Objects.equals(viewer, that.viewer)) return false;
        if (!playersAllowed.equals(that.playersAllowed)) return false;
        if (!Objects.equals(fallingBlock, that.fallingBlock)) return false;
        if (!Objects.equals(text, that.text)) return false;
        if (!Objects.equals(permission, that.permission)) return false;
        return mask == that.mask;
    }

    @Override
    public int hashCode() {
        int result = viewer != null ? viewer.hashCode() : 0;
        result = 31 * result + playersAllowed.hashCode();
        result = 31 * result + (fallingBlock != null ? fallingBlock.hashCode() : 0);
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (permission != null ? permission.hashCode() : 0);
        result = 31 * result + mask.hashCode();
        result = 31 * result + (stopIfAir ? 1 : 0);
        result = 31 * result + (stopVisualizeBlock ? 1 : 0);
        return result;
    }
}