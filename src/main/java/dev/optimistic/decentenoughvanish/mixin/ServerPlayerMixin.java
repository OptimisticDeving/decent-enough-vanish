package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PlayerState {
  @Unique
  private static final Component CURRENTLY_VANISHED =
    Component.literal("You are currently in vanish.")
      .withStyle(ChatFormatting.GREEN);

  @Unique
  private static final String KEY = "decent-enough-vanish$vanished";
  @Shadow
  @Final
  public MinecraftServer server;
  @Shadow
  @Final
  private TextFilter textFilter;
  @Unique
  private boolean vanished;
  @Unique
  private boolean vanishedLastTick;

  @Shadow
  public abstract void sendSystemMessage(Component message, boolean overlay);

  @Shadow
  public abstract void sendSystemMessage(Component mesage);

  @Override
  public boolean decentenoughvanish$isVanished() {
    return this.vanished;
  }

  @Override
  public void decentenoughvanish$setVanish(boolean newVanish) {
    final boolean oldVanish = this.vanished;
    this.vanished = newVanish;

    if (oldVanish != this.vanished) {
      final ServerPlayer serverPlayer = ((ServerPlayer) (Object) this);
      final Packet<?> packet;
      final var server = serverPlayer.getServer();
      assert server != null;
      final var playerList = server.getPlayerList();
      final String translationKey;

      final var trackedEntity =
        serverPlayer.serverLevel()
          .getChunkSource()
          .chunkMap
          .entityMap
          .get(serverPlayer.getId());

      if (this.vanished) {
        packet = new ClientboundPlayerInfoRemovePacket(
          Collections.singletonList(serverPlayer.getUUID())
        );

        this.textFilter.leave();
        translationKey = "multiplayer.player.left";

        serverPlayer.unRide();
        trackedEntity.seenBy.forEach(seenBy -> trackedEntity.removePlayer(seenBy.getPlayer()));
      } else {
        packet = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(
          Collections.singleton(serverPlayer)
        );

        this.textFilter.join();
        translationKey = "multiplayer.player.joined";
      }

      playerList.broadcastSystemMessage(
        Component.translatable(
          translationKey,
          serverPlayer.getDisplayName()
        ).withStyle(ChatFormatting.YELLOW),
        false
      );

      for (
        final ServerPlayer listedPlayer : playerList.getPlayers()
      ) {
        if (listedPlayer == serverPlayer) continue;
        listedPlayer.connection.send(packet);
      }

      if (this.vanished) return;
      trackedEntity.updatePlayers(
        serverPlayer
          .serverLevel()
          .getPlayers(player -> player != serverPlayer)
      );
    }
  }

  @WrapMethod(method = "broadcastToPlayer")
  private boolean shouldBroadcastToPlayer(
    ServerPlayer player,
    Operation<Boolean> original
  ) {
    return !this.decentenoughvanish$isVanished() && original.call(player);
  }

  @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
  private void addAdditionalData(
    CompoundTag compound,
    CallbackInfo ci
  ) {
    compound.putBoolean(KEY, this.vanished);
  }

  @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
  private void readAdditionalData(
    CompoundTag compound,
    CallbackInfo ci
  ) {
    this.vanished = compound.getBoolean(KEY);
  }

  @Inject(method = "restoreFrom", at = @At("HEAD"))
  private void restoreFrom(
    ServerPlayer that,
    boolean keepEverything,
    CallbackInfo ci
  ) {
    this.vanished =
      ((PlayerState) that).decentenoughvanish$isVanished();
  }

  @WrapOperation(
    method = "die",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/world/level/GameRules;" +
        "getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"
    )
  )
  private boolean die$getHideDeathMessages(
    GameRules instance,
    GameRules.Key<GameRules.BooleanValue> key,
    Operation<Boolean> original
  ) {
    if (key != GameRules.RULE_SHOWDEATHMESSAGES)
      return original.call(instance, key);
    return !this.vanished && original.call(instance, key);
  }

  @Inject(method = "tick", at = @At("HEAD"))
  private void afterTick(CallbackInfo ci) {
    if (this.vanished) {
      this.vanishedLastTick = true;
      this.sendSystemMessage(CURRENTLY_VANISHED, true);
    } else if (this.vanishedLastTick) {
      this.vanishedLastTick = false;
      this.sendSystemMessage(CommonComponents.EMPTY, true);
    }
  }
}
