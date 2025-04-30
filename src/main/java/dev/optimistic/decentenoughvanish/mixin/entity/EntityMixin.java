package dev.optimistic.decentenoughvanish.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.decentenoughvanish.state.PlayerState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class EntityMixin {
  @WrapMethod(method = "canBeSeenByAnyone")
  private boolean canBeSeenByAnyone(Operation<Boolean> original) {
    if (((Object) this instanceof final ServerPlayer player) && ((PlayerState) player).decentenoughvanish$isVanished())
      return false;

    return original.call();
  }
}
