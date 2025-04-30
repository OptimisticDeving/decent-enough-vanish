package dev.optimistic.decentenoughvanish.mixin.command.impl;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import dev.optimistic.decentenoughvanish.state.PlayerState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.OpCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(OpCommand.class)
public abstract class OpCommandMixin {
  @WrapOperation(
    method = "method_13467",
    at = @At(
      value = "INVOKE",
      target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"
    )
  )
  private static Stream<ServerPlayer> filterOnlinePlayers(
    Stream<ServerPlayer> instance,
    Predicate<ServerPlayer> predicate,
    Operation<Stream<ServerPlayer>> original,
    @Local(argsOnly = true) CommandContext<CommandSourceStack> context
  ) {
    final var source = context.getSource();

    return original.call(
      instance,
      predicate.and(
        player -> PlayerState.shouldSee(source, player)
      )
    );
  }
}
