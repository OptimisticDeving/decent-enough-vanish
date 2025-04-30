package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
  @Shadow
  public ServerPlayer player;

  @WrapOperation(
    method = "<init>",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/network/TextFilter;join()V"
    )
  )
  private void init(TextFilter instance, Operation<Void> original) {
    if (((PlayerState) this.player).decentenoughvanish$isVanished()) return;
    original.call(instance);
  }

  @WrapOperation(
    method = "method_44900",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;" +
        "filterTextPacket(Ljava/lang/String;)" +
        "Ljava/util/concurrent/CompletableFuture;"
    )
  )
  private CompletableFuture<FilteredText> method_44900(
    ServerGamePacketListenerImpl instance,
    String text,
    Operation<CompletableFuture<FilteredText>> original
  ) {
    if (((PlayerState) this.player).decentenoughvanish$isVanished())
      return CompletableFuture.completedFuture(
        FilteredText.fullyFiltered(text)
      );

    return original.call(instance, text);
  }

  @WrapOperation(
    method = "removePlayerFromWorld",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/players/PlayerList;" +
        "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
    )
  )
  private void removePlayerFromWorld(
    PlayerList instance,
    Component message,
    boolean bypassHiddenChat,
    Operation<Void> original
  ) {
    if (((PlayerState) this.player).decentenoughvanish$isVanished()) return;
    original.call(instance, message, bypassHiddenChat);
  }

  @WrapOperation(
    method = "handleInteract",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;" +
        "getTarget(Lnet/minecraft/server/level/ServerLevel;)" +
        "Lnet/minecraft/world/entity/Entity;"
    )
  )
  private Entity handleInteract$getTarget(
    ServerboundInteractPacket instance,
    ServerLevel level,
    Operation<Entity> original
  ) {
    final var entity = original.call(instance, level);
    return entity instanceof final PlayerState playerState
      ?
      playerState.decentenoughvanish$isVanished()
        ? null
        :
        entity
      :
      entity;
  }

  @WrapOperation(
    method = "removePlayerFromWorld",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/network/TextFilter;leave()V"
    )
  )
  private void removePlayerFromWorld$leaveFilter(
    TextFilter instance,
    Operation<Void> original
  ) {
    if (((PlayerState) this.player).decentenoughvanish$isVanished()) return;
    original.call(instance);
  }
}
