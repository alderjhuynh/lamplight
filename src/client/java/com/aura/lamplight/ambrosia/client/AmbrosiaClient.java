package com.aura.lamplight.ambrosia.client;

import com.aura.lamplight.ambrosia.block.CampfireCookingPotBlock;
import com.aura.lamplight.ambrosia.block.ModBlockEntities;
import com.aura.lamplight.ambrosia.block.ModBlocks;
import com.aura.lamplight.ambrosia.block.PotFluid;
import com.aura.lamplight.ambrosia.block.PotParticles;
import com.aura.lamplight.ambrosia.client.appleskin.AppleSkin;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AmbrosiaClient implements ClientModInitializer {
	private static final BlockTintSource POT_FLUID_TINT = new BlockTintSource() {
		@Override
		public int color(BlockState state) {
			return fluidColor(state);
		}

		@Override
		public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
			return fluidColor(state);
		}

		private static int fluidColor(BlockState state) {
			PotFluid fluid = state.getValue(CampfireCookingPotBlock.FLUID);
			return fluid == PotFluid.EMPTY ? -1 : fluid.color();
		}
	};

	@Override
	@SuppressWarnings ("deprecation")
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(AppleSkin.INSTANCE::tick);
		BlockEntityRendererRegistry.register(ModBlockEntities.COOKING_POT, CookingPotRenderer::new);
		BlockColorRegistry.register(List.of(POT_FLUID_TINT), ModBlocks.CAMPFIRE_COOKING_POT, ModBlocks.SOUL_CAMPFIRE_COOKING_POT);
		ParticleProviderRegistry.getInstance().register(PotParticles.COOKING_POT_BUBBLE, CookingPotBubbleParticle.Provider::new);	}
}
