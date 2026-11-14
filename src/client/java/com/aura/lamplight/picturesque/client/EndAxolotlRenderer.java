package  com.aura.lamplight.picturesque.client;

import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class EndAxolotlRenderer extends AxolotlRenderer {
	private static final String MAGIC_NAME = "End";

	private static final Identifier END_ADULT =
		Identifier.fromNamespaceAndPath("lamplight", "textures/entity/axolotl/axolotl_end.png");
	private static final Identifier END_BABY =
		Identifier.fromNamespaceAndPath("lamplight", "textures/entity/axolotl/axolotl_end_baby.png");

	public EndAxolotlRenderer(final EntityRendererProvider.Context context) {
		super(context);
	}

	public static class EndAxolotlRenderState extends AxolotlRenderState {
		public boolean isEnd;
	}

	@Override
	public AxolotlRenderState createRenderState() {
		return new EndAxolotlRenderState();
	}

	@Override
	public void extractRenderState(final Axolotl entity, final AxolotlRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		if (state instanceof EndAxolotlRenderState endState) {
			endState.isEnd = checkMagicName(entity, MAGIC_NAME);
		}
	}

	@Override
	public Identifier getTextureLocation(final AxolotlRenderState state) {
		if (state instanceof EndAxolotlRenderState endState && endState.isEnd) {
			return state.isBaby ? END_BABY : END_ADULT;
		}
		return super.getTextureLocation(state);
	}
}