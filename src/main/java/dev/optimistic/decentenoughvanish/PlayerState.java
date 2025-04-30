package dev.optimistic.decentenoughvanish;

import dev.optimistic.decentenoughvanish.mixin.accessor.CommandSourceStackAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface PlayerState {
  static boolean shouldSee(
    @Nullable ServerPlayer player,
    CommandSourceStack stack
  ) {
    if (((CommandSourceStackAccessor) stack).getSource() instanceof MinecraftServer)
      return true;
    if (player == null) return false;
    return stack.getEntity() == player
      || !((PlayerState) player).decentenoughvanish$isVanished();
  }

  static boolean shouldSee(Entity seer, ServerPlayer other) {
    return seer == other
      ||
      !((PlayerState) other).decentenoughvanish$isVanished();
  }

  boolean decentenoughvanish$isVanished();

  void decentenoughvanish$setVanish(boolean newVanish);
}
