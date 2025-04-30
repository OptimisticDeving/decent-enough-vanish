package dev.optimistic.decentenoughvanish;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

public interface CommandSourceStackState {
  static CommandContext<CommandSourceStack> withBypass(
    CommandContext<CommandSourceStack> ctx,
    boolean bypass
  ) {
    ((CommandSourceStackState) ctx.getSource())
      .decentenoughvanish$setBypassVanish(bypass);
    return ctx;
  }

  boolean decentenoughvanish$bypassVanish();

  void decentenoughvanish$setBypassVanish(boolean newValue);
}
