package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.optimistic.decentenoughvanish.CommandSourceStackState;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(MessageArgument.class)
public abstract class MessageArgumentMixin {
  @Unique
  private static final CommandSyntaxException EXIT_VANISH =
    new SimpleCommandExceptionType(
      Component.literal("Please exit vanish before broadcasting messages")
    ).create();

  @WrapMethod(method = "resolveChatMessage")
  private static void resolveChatMessage(
    CommandContext<CommandSourceStack> context,
    String key,
    Consumer<PlayerChatMessage> callback,
    Operation<Void> original
  ) throws CommandSyntaxException {
    final var source = context.getSource();
    final var entity = source.getEntity();

    if (entity == null) {
      original.call(context, key, callback);
      return;
    }

    if (((PlayerState) entity).decentenoughvanish$isVanished() &&
      !((CommandSourceStackState) source).decentenoughvanish$bypassVanishMessages())
      throw EXIT_VANISH;

    original.call(context, key, callback);
  }

  @WrapOperation(
    method = "resolveChatMessage",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/commands/CommandSigningContext" +
        ";getArgument(Ljava/lang/String;)" +
        "Lnet/minecraft/network/chat/PlayerChatMessage;"
    )
  )
  private static PlayerChatMessage resolveChatMessage$getArgument(
    CommandSigningContext instance,
    String key,
    Operation<PlayerChatMessage> original,
    @Local(argsOnly = true) CommandContext<CommandSourceStack> context
  ) {
    final var source = context.getSource();
    final var entity = source.getEntity();

    if (entity == null) return original.call(instance, key);

    if (((PlayerState) entity).decentenoughvanish$isVanished()) return null;
    return original.call(instance, key);
  }
}
