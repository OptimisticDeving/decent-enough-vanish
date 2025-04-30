package dev.optimistic.decentenoughvanish.mixin.containerblock;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.state.PlayerState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ContainerOpenersCounter.class)
public abstract class ContainerOpenersCounterMixin {
  @WrapMethod(method = {"incrementOpeners", "decrementOpeners"})
  private void incrementOrDecrementOpeners(
    Player player,
    Level level,
    BlockPos pos,
    BlockState state,
    Operation<Void> original
  ) {
    if (
      player instanceof final ServerPlayer serverPlayer
        && ((PlayerState) serverPlayer).decentenoughvanish$isVanished()
    ) {
      return;
    }

    original.call(player, level, pos, state);
  }

  @WrapMethod(method = "getPlayersWithContainerOpen")
  private List<Player> getPlayersWithContainerOpen(
    Level level,
    BlockPos pos,
    Operation<List<Player>> original
  ) {
    return original.call(level, pos)
      .stream()
      .filter(
        player ->
          !(player instanceof final ServerPlayer serverPlayer)
            || !((PlayerState) serverPlayer).decentenoughvanish$isVanished()
      ).toList();
  }
}
