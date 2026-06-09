package com.joive.scavengerextratrials.mixin;

import com.joive.scavengerextratrials.ExtraClientEvents;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraRollMixin {
    @Shadow
    @Final
    private static Vector3f FORWARDS;

    @Shadow
    @Final
    private static Vector3f UP;

    @Shadow
    @Final
    private static Vector3f LEFT;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

    @Inject(method = "setRotation(FF)V", at = @At("TAIL"))
    private void scavengerExtraTrials$applyCameraRoll(float yRot, float xRot, CallbackInfo callbackInfo) {
        float roll = ExtraClientEvents.upsideDownRoll();
        if (Math.abs(roll) < 0.05F) {
            return;
        }
        this.rotation.rotateZ((float) Math.toRadians(roll));
        FORWARDS.rotate(this.rotation, this.forwards);
        UP.rotate(this.rotation, this.up);
        LEFT.rotate(this.rotation, this.left);
    }
}
