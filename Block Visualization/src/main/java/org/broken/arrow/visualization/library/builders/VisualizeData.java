package org.broken.arrow.visualization.library.builders;

import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public final class VisualizeData {
	private final Player viwer;
	private final Set<Player> playersAllowed;
	private FallingBlock fallingBlock;
	private final String text;
	private String permission;
	private final Material mask;
	private boolean stopIfAir;
	private boolean removeIfAir;

	public VisualizeData(@Nonnull final Player viwer, @Nullable final String text, @Nonnull final Material mask) {
		this(viwer, new HashSet<>(), text, mask);
	}

	public VisualizeData(@Nullable final Player viwer, @Nullable final Set<Player> playersAllowed, @Nullable final String text, @Nonnull final Material mask) {
		this.viwer = viwer;
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

	public void addPlayersAllowed(final Player viwer) {
		playersAllowed.add(viwer);
	}

	public void setPermission(final String permission) {
		this.permission = permission;
	}

	public VisualizeData setFallingBlock(final FallingBlock fallingBlock) {
		this.fallingBlock = fallingBlock;
		return this;
	}

	@Nullable
	public String getPermission() {
		return permission;
	}

	@Nullable
	public Player getViwer() {
		return viwer;
	}

	@Nonnull
	public Set<Player> getPlayersAllowed() {
		return playersAllowed;
	}

	public FallingBlock getFallingBlock() {
		return fallingBlock;
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

}