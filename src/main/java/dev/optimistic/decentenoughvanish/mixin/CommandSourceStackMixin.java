package dev.optimistic.decentenoughvanish.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.CommandSourceStackState;
import dev.optimistic.decentenoughvanish.PlayerState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements CommandSourceStackState {
  @Unique
  private boolean bypassVanishCompletions;

  @Unique
  private boolean bypassVanishMessages;

  @Shadow
  @Final
  private MinecraftServer server;

  @Shadow
  public abstract boolean isPlayer();

  @Shadow
  public abstract ServerPlayer getPlayer();

  @Shadow
  public abstract boolean hasPermission(int permissionLevel);

  @WrapMethod(method = "getOnlinePlayerNames")
  private Collection<String> getOnlinePlayerNames(
    Operation<Collection<String>> original
  ) {
    final var originalList = original.call();
    if (this.decentenoughvanish$bypassVanishCompletions()) return originalList;
    final var playerList = this.server.getPlayerList();
    return originalList
      .stream()
      .filter(
        name -> PlayerState.shouldSee(
          (CommandSourceStack) (Object) this,
          playerList.getPlayerByName(name)
        )
      )
      .toList();
  }

  @Override
  public boolean decentenoughvanish$bypassVanishCompletions() {
    return this.hasPermission(Commands.LEVEL_ADMINS) || this.bypassVanishCompletions;
  }

  @Override
  public void decentenoughvanish$setBypassVanishCompletions(boolean newValue) {
    this.bypassVanishCompletions = newValue;
  }

  @Override
  public boolean decentenoughvanish$bypassVanishMessages() {
    return this.bypassVanishMessages;
  }

  @Override
  public void decentenoughvanish$setBypassVanishMessages(boolean newValue) {
    this.bypassVanishMessages = newValue;
  }

  @Inject(method = "broadcastToAdmins", at = @At("HEAD"), cancellable = true)
  private void broadcastToAdmins(Component message, CallbackInfo ci) {
    if (
      this.isPlayer()
        &&
        ((PlayerState) this.getPlayer()).decentenoughvanish$isVanished())
      ci.cancel();
  }
}