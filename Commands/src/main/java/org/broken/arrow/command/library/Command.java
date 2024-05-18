package org.broken.arrow.command.library;

import org.broken.arrow.command.library.command.CommandHolder;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Command extends CommandHolder {

    public Command() {
        super("command","command2");
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] cmdArgs) {
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull String commandLabel, @Nonnull String[] cmdArgs) {
        return new ArrayList<>();
    }
}
