package dev.optimistic.decentenoughvanish.state;

import dev.optimistic.decentenoughvanish.mixin.accessor.CommandSourceStackAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface PlayerState {
  static boolean shouldSee(
    CommandSourceStack seer,
    @Nullable ServerPlayer other
  ) {
    if (((CommandSourceStackAccessor) seer).getSource() instanceof MinecraftServer)
      return true;
    if (other == null) return false;
    return seer.getEntity() == other
      || !((PlayerState) other).decentenoughvanish$isVanished();
  }

  static boolean shouldSee(Entity seer, ServerPlayer other) {
    return seer == other
      ||
      !((PlayerState) other).decentenoughvanish$isVanished();
  }

  boolean decentenoughvanish$isVanished();

  void decentenoughvanish$setVanish(boolean newVanish);
}
