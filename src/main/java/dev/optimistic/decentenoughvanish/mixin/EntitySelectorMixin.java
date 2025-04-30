package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.CommandSourceStackState;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {
  @WrapMethod(method = "findPlayers")
  private List<ServerPlayer> findPlayers(
    CommandSourceStack source,
    Operation<List<ServerPlayer>> original
  ) {
    final var originalList = original.call(source);
    if (((CommandSourceStackState) source).decentenoughvanish$bypassVanishCompletions())
      return originalList;
    return originalList
      .stream()
      .filter(player -> PlayerState.shouldSee(player, source))
      .toList();
  }

  @WrapMethod(method = "findEntities")
  private List<? extends Entity> findEntities(
    CommandSourceStack source,
    Operation<List<? extends Entity>> original
  ) {
    final var originalList = original.call(source);
    if (((CommandSourceStackState) source).decentenoughvanish$bypassVanishCompletions())
      return originalList;
    return originalList
      .stream()
      .filter(
        entity ->
          !(entity instanceof final ServerPlayer player)
            || PlayerState.shouldSee(player, source)
      )
      .toList();
  }
}
