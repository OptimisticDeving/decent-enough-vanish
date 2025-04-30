package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
  @WrapOperation(
    method = "placeNewPlayer",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/players/PlayerList;" +
        "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
    )
  )
  private void placeNewPlayer$broadcastSystemMessage(
    PlayerList instance,
    Component message,
    boolean bypassHiddenChat,
    Operation<Void> original,
    @Local(argsOnly = true) ServerPlayer toPlace
  ) {
    if (((PlayerState) toPlace).decentenoughvanish$isVanished()) return;
    original.call(instance, message, bypassHiddenChat);
  }
}
