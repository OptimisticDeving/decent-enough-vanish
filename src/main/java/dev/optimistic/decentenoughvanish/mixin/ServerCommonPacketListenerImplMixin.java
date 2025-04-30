package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.PlayerState;
import dev.optimistic.decentenoughvanish.StreamUtil;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
  @Unique
  private final Set<UUID> seenPlayers = new ConcurrentSkipListSet<>();

  @Shadow
  @Final
  protected MinecraftServer server;

  @WrapMethod(
    method = "send(Lnet/minecraft/network/protocol/Packet;" +
      "Lnet/minecraft/network/PacketSendListener;)V"
  )
  private void send(
    Packet<?> packet,
    @Nullable PacketSendListener listener,
    Operation<Void> original
  ) {
    if (!((Object) this instanceof final ServerGamePacketListenerImpl gamePacketListener)) {
      original.call(packet, listener);
      return;
    }

    switch (packet) {
      case ClientboundPlayerInfoUpdatePacket infoUpdatePacket -> {
        final var playerList = this.server.getPlayerList();
        final var newPlayers = StreamUtil.filterMap(
            infoUpdatePacket.entries()
              .stream(),
            entry -> {
              final var id = entry.profileId();
              final var player = playerList.getPlayer(id);
              if (PlayerState.shouldSee(gamePacketListener.player, player)) {
                this.seenPlayers.add(id);
                return player;
              }
              return null;
            }
          )
          .toList();

        if (newPlayers.isEmpty()) return;

        packet = new ClientboundPlayerInfoUpdatePacket(
          infoUpdatePacket.actions(),
          newPlayers
        );
      }
      case ClientboundPlayerInfoRemovePacket infoRemovePacket -> {
        final var toRemove = infoRemovePacket.profileIds()
          .stream()
          .filter(seenPlayers::remove)
          .toList();

        if (toRemove.isEmpty()) return;

        packet = new ClientboundPlayerInfoRemovePacket(
          toRemove
        );
      }
      case null, default -> {

      }
    }

    original.call(packet, listener);
  }
}
