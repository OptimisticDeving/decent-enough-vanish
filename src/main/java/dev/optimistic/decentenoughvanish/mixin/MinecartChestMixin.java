package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecartChest.class)
public abstract class MinecartChestMixin {
  @WrapMethod(method = "stopOpen")
  private void stopOpen(Player player, Operation<Void> original) {
    if (
      player instanceof final ServerPlayer serverPlayer
        && ((PlayerState) serverPlayer).decentenoughvanish$isVanished()
    ) return;

    original.call(player);
  }
}
