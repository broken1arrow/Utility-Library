package org.broken.arrow.visualization.library.builders;

import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public final class VisualizeData {
    private final Player viewer;
    private final Set<Player> playersAllowed;
    private FallingBlock fallingBlock;
    private final String text;
    private String permission;
    private final Material mask;
    private boolean stopIfAir;
    private boolean removeIfAir;
    private boolean stopVisualizeBlock;

    public VisualizeData(@Nonnull final Player viewer, @Nullable final String text, @Nonnull final Material mask) {
        this(viewer, new HashSet<>(), text, mask);
    }

    public VisualizeData(@Nullable final Player viewer, @Nullable final Set<Player> playersAllowed, @Nullable final String text, @Nonnull final Material mask) {
        this.viewer = viewer;
        this.playersAllowed = playersAllowed != null ? playersAllowed : new HashSet<>();
        this.text = text;
        this.mask = mask;
    }

    public void setStopIfAir(final boolean stopIfAir) {
        this.stopIfAir = stopIfAir;
    }

    public void setRemoveIfAir(final boolean removeIfAir) {
        this.removeIfAir = removeIfAir;
    }

    public void addPlayersAllowed(final Player viewer) {
        playersAllowed.add(viewer);
    }

    public void setPermission(final String permission) {
        this.permission = permission;
    }

    public void setFallingBlock(final FallingBlock fallingBlock) {
        this.fallingBlock = fallingBlock;
    }

    public void setStopVisualizeBlock(boolean stopVisualizeBlock) {
        this.stopVisualizeBlock = stopVisualizeBlock;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }

    @Nullable
    public Player getViewer() {
        return viewer;
    }

    @Nonnull
    public Set<Player> getPlayersAllowed() {
        return playersAllowed;
    }

    public FallingBlock getFallingBlock() {
        return fallingBlock;
    }

    public void removeFallingBlock() {
        if (fallingBlock != null)
            fallingBlock.remove();
    }

    @Nullable
    public String getText() {
        return text;
    }

    @Nonnull
    public Material getMask() {
        return mask;
    }

    public boolean isStopIfAir() {
        return stopIfAir;
    }

    public boolean isRemoveIfAir() {
        return removeIfAir;
    }

    public boolean isStopVisualizeBlock() {
        return stopVisualizeBlock;
    }
}