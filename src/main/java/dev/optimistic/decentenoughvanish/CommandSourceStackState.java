package dev.optimistic.decentenoughvanish;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public interface CommandSourceStackState {
  static CommandContext<CommandSourceStack> withBypassCompletions(
    CommandContext<CommandSourceStack> ctx
  ) {
    ((CommandSourceStackState) ctx.getSource())
      .decentenoughvanish$setBypassVanishCompletions(true);
    return ctx;
  }

  static CommandContext<CommandSourceStack> withBypassMessages(
    CommandContext<CommandSourceStack> ctx
  ) {
    ((CommandSourceStackState) ctx.getSource())
      .decentenoughvanish$setBypassVanishMessages(true);
    return ctx;
  }

  boolean decentenoughvanish$bypassVanishCompletions();

  void decentenoughvanish$setBypassVanishCompletions(boolean newValue);

  boolean decentenoughvanish$bypassVanishMessages();

  void decentenoughvanish$setBypassVanishMessages(boolean newValue);
}
