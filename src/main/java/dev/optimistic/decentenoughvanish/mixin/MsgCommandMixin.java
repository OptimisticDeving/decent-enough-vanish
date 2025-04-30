package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.context.CommandContext;
import dev.optimistic.decentenoughvanish.CommandSourceStackState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(MsgCommand.class)
public abstract class MsgCommandMixin {
  @WrapOperation(
    method = "method_13463",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/commands/arguments/MessageArgument;" +
        "resolveChatMessage(Lcom/mojang/brigadier/context/CommandContext;" +
        "Ljava/lang/String;Ljava/util/function/Consumer;)V"
    )
  )
  private static void bypassVanishInTell(
    CommandContext<CommandSourceStack> context,
    String key,
    Consumer<PlayerChatMessage> callback,
    Operation<Void> original
  ) {
    original.call(
      CommandSourceStackState.withBypassMessages(context),
      key,
      callback
    );
  }
}
