package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
  @Shadow
  @Final
  private MinecraftServer server;
  @Shadow
  @Final
  private PlayerDataStorage playerIo;

  @Shadow
  public abstract Optional<CompoundTag> load(ServerPlayer player);

  @Shadow
  protected abstract void save(ServerPlayer player);

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

  @Inject(method = "deop", at = @At("TAIL"))
  private void afterDeop(
    GameProfile profile,
    CallbackInfo ci,
    @Local ServerPlayer presentPlayer
  ) {
    if (presentPlayer == null) return;
    ((PlayerState) presentPlayer).decentenoughvanish$setVanish(false);
  }
}
