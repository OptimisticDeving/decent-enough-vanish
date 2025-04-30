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
  private boolean bypassVanish;

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
    if (this.decentenoughvanish$bypassVanish()) return originalList;
    final var playerList = this.server.getPlayerList();
    return originalList
      .stream()
      .filter(
        name -> PlayerState.shouldSee(
          playerList.getPlayerByName(name),
          (CommandSourceStack) (Object) this
        )
      )
      .toList();
  }

  @Override
  public boolean decentenoughvanish$bypassVanish() {
    return this.hasPermission(Commands.LEVEL_ADMINS) || this.bypassVanish;
  }

  @Override
  public void decentenoughvanish$setBypassVanish(boolean newValue) {
    this.bypassVanish = newValue;
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