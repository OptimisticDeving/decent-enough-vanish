package dev.optimistic.decentenoughvanish.mixin.containerblock;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.state.PlayerState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin {
  @WrapMethod(method = {"startOpen", "stopOpen"})
  private void startOrStopOpen(Player player, Operation<Void> original) {
    if (
      player instanceof final ServerPlayer serverPlayer
        && ((PlayerState) serverPlayer).decentenoughvanish$isVanished()
    ) return;

    original.call(player);
  }
}
