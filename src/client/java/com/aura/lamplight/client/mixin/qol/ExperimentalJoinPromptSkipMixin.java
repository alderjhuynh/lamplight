package com.aura.lamplight.client.mixin.qol;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Skips the join-time "Worlds using Experimental Settings are not supported"
 * confirmation screen.
 *
 * <p>When entering a world whose level data records experimental datapack
 * features, {@code WorldOpenFlows.openWorldCheckWorldStemCompatibility} sees
 * {@code WorldData.worldGenSettingsLifecycle() != Lifecycle.stable()} and calls
 * {@code askForBackup}, which raises a {@code BackupConfirmScreen} with the
 * {@code selectWorld.backupQuestion/Warning.experimental} wording
 * ("Create Backup and Load" / "I Know What I'm Doing!" / "Cancel").
 * Proceeding via the second button is behaviorally just the vanilla
 * continuation without a backup toast, so this redirect makes the check parse
 * the world's lifecycle as stable and the world loads directly.
 *
 * <p>This deliberately does not touch the separate version-compatibility
 * backup prompt in {@code openWorldCheckVersionCompatibility} (worlds saved on
 * a newer data version), nor the world-creation-time confirmation in
 * {@code CreateWorldScreen}.
 */
@Mixin(WorldOpenFlows.class)
public abstract class ExperimentalJoinPromptSkipMixin {
	@Redirect(
		method = "openWorldCheckWorldStemCompatibility(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/server/WorldStem;Lnet/minecraft/server/packs/repository/PackRepository;Ljava/lang/Runnable;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/WorldData;worldGenSettingsLifecycle()Lcom/mojang/serialization/Lifecycle;"
		)
	)
	private Lifecycle lamplight$skipExperimentalJoinPrompt(WorldData worldData) {
		return Lifecycle.stable();
	}
}
