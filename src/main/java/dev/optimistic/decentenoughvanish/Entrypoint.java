package dev.optimistic.decentenoughvanish;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class Entrypoint implements DedicatedServerModInitializer {
  private static final TargetProvider SELF =
    ctx ->
      Collections.singleton(ctx.getSource().getPlayerOrException());
  private static final EntityArgument TARGETS = EntityArgument.players();

  private static int set(
    CommandContext<CommandSourceStack> ctx,
    boolean newValue,
    Collection<? extends Entity> targets
  ) {
    targets.forEach(entity ->
      ((PlayerState) entity).decentenoughvanish$setVanish(newValue));

    final var source = ctx.getSource();

    final var enabledOrDisabled = newValue ? "Enabled" : "Disabled";

    if (
      targets.size() == 1
        &&
        source.getPlayer() == targets.stream().findFirst().orElse(null)
    ) {
      source.sendSystemMessage(
        Component.literal(enabledOrDisabled + " your vanish")
      );
    } else {
      source.sendSystemMessage(
        Component.literal(
          enabledOrDisabled + " vanish for " + targets.size() + " player(s)"
        )
      );
    }

    return targets.size();
  }

  private static int toggle(
    CommandContext<CommandSourceStack> ctx,
    Collection<ServerPlayer> targets
  ) {
    if (targets.size() == 1) {
      final ServerPlayer firstPlayer = targets.stream().findFirst().orElseThrow();
      final boolean enable = !(((PlayerState) firstPlayer)
        .decentenoughvanish$isVanished());

      set(ctx, enable, targets);
      return 1;
    }

    final List<Entity> toEnable = new ObjectArrayList<>();
    final List<Entity> toDisable = new ObjectArrayList<>();

    for (final Entity target : targets) {
      if (((PlayerState) target).decentenoughvanish$isVanished()) {
        toDisable.add(target);
      } else {
        toEnable.add(target);
      }
    }

    set(ctx, true, toEnable);
    set(ctx, false, toDisable);

    return targets.size();
  }

  private static <T extends ArgumentBuilder<CommandSourceStack, ?>> T withTargets(
    T parent,
    TargetProvider targetsProducer
  ) {
    // noinspection unchecked
    return (T) parent
      .executes(
        ctx ->
          toggle(ctx, targetsProducer.getTargets(ctx))
      )
      .then(
        Commands.literal("on")
          .executes(
            ctx ->
              set(
                ctx,
                true,
                targetsProducer.getTargets(ctx)
              )
          )
      )
      .then(
        Commands.literal("off")
          .executes(
            ctx ->
              set(
                ctx,
                false,
                targetsProducer.getTargets(ctx)
              )
          )
      )
      .then(
        Commands.literal("toggle")
          .executes(ctx ->
            toggle(
              ctx,
              targetsProducer.getTargets(ctx)
            )
          )
      );
  }

  private static void registerName(
    CommandDispatcher<CommandSourceStack> dispatcher,
    String name
  ) {
    dispatcher.register(
      withTargets(
        Commands.literal(name)
          .requires(
            ctx -> ctx.hasPermission(Commands.LEVEL_GAMEMASTERS)
          ),
        SELF
      ).then(
        withTargets(
          Commands.argument(
            "targets",
            TARGETS
          ).requires(
            ctx -> ctx.hasPermission(Commands.LEVEL_ADMINS)
          ),
          ctx ->
            EntityArgument.getPlayers(
              CommandSourceStackState.withBypass(ctx, true),
              "targets"
            )
        )
      )
    );
  }

  @Override
  public void onInitializeServer() {
    CommandRegistrationCallback.EVENT.register(
      (
        dispatcher,
        registries,
        environment
      ) -> {
        registerName(dispatcher, "vanish");
        registerName(dispatcher, "v");
      }
    );
  }

  @FunctionalInterface
  private interface TargetProvider {
    Collection<ServerPlayer> getTargets(
      CommandContext<CommandSourceStack> ctx
    ) throws CommandSyntaxException;
  }
}
