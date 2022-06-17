import com.mojang.blaze3d.platform.*;
import com.voila.forge.*;
import net.minecraft.client.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** script press key */
@SuppressWarnings("all")
@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping {
	@Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
	private void i(CallbackInfoReturnable<Boolean> info){
		if(!Script.enabled){
			return;
		}
		Object key = this;
		Options settings = Minecraft.getInstance().options;
		if(Keys.runningScript.forward && key == settings.keyUp ||
			Keys.runningScript.backward && key == settings.keyDown ||
			Keys.runningScript.left && key == settings.keyLeft ||
			Keys.runningScript.right && key == settings.keyRight ||
			Keys.runningScript.jump && key == settings.keyJump ||
			Keys.runningScript.crouch && key == settings.keyShift ||
			Keys.runningScript.use && key == settings.keyUse ||
			Keys.runningScript.attack && key == settings.keyAttack){
			info.setReturnValue(true);
		}
	}

	/** Optifine zoom key */
	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("RETURN"))
	private void reg(String name, InputConstants.Type p_90826_, int p_90827_, String p_90828_, CallbackInfo info){
		KeyMapping k = (KeyMapping)(Object)this;
		if(name.equals("of.key.zoom")){
			Forgetest.ofZoom = k;
		}
	}
}
