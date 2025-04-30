package dev.optimistic.decentenoughvanish.mixin.command.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.optimistic.decentenoughvanish.state.CommandSourceStackState;
import dev.optimistic.decentenoughvanish.state.PlayerState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.ListPlayersCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ListPlayersCommand.class)
public abstract class ListPlayersCommandMixin {
  @WrapOperation(
    method = "format",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/server/players/PlayerList;" +
        "getPlayers()Ljava/util/List;"
    )
  )
  private static List<ServerPlayer> format$getPlayers(
    PlayerList instance,
    Operation<List<ServerPlayer>> original,
    @Local(argsOnly = true) CommandSourceStack source
  ) {
    final var originalList = original.call(instance);
    if (((CommandSourceStackState) source).decentenoughvanish$bypassVanishCompletions())
      return originalList;
    return originalList
      .stream()
      .filter(player -> PlayerState.shouldSee(source, player))
      .toList();
  }
}
