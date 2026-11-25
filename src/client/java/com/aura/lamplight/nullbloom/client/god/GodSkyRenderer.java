package com.aura.lamplight.nullbloom.client.god;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

public final class GodSkyRenderer {
	public static final Identifier TEXTURE_ID = Identifier.fromNamespaceAndPath("lamplight", "textures/environment/god_sky.png");

	private static final float CUBE_HALF_SIZE = 100.0F;
	private static final int WHITE = -1;

	private static final int IMAGE_W = 2048;
	private static final int IMAGE_H = 1536;
	private static final int TILE = 512;

	public static final boolean SUN_FACES_PLAYER = true;

	private static GpuBuffer skyBuffer;

	private GodSkyRenderer() {
	}

	public static boolean render(RenderTarget target, float playerYawDegrees) {
		AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(TEXTURE_ID);
		if (texture == null || texture.getTextureView() == null) {
			return false;
		}
		GpuBuffer buffer = getOrBuildBuffer();

		Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrixCopy());
		if (SUN_FACES_PLAYER) {
			modelView.rotateY((float) Math.toRadians(-playerYawDegrees));
		}
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView);
		RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
		GpuBuffer indexBuffer = indices.getBuffer(36);

		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
			() -> "God sky", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
			pass.setPipeline(RenderPipelines.END_SKY);
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", transforms);
			pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
			pass.setVertexBuffer(0, buffer.slice());
			pass.setIndexBuffer(indexBuffer, indices.type());
			pass.drawIndexed(36, 1, 0, 0, 0);
		}
		return true;
	}

	private static synchronized GpuBuffer getOrBuildBuffer() {
		if (skyBuffer == null || skyBuffer.isClosed()) {
			skyBuffer = buildSkyBuffer();
		}
		return skyBuffer;
	}

	private static GpuBuffer buildSkyBuffer() {
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize())) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			addFace(builder, null, tileUv(1, 2), new int[]{1, 2, 3, 0}); // nadir
			addFace(builder, new Matrix4f().rotationX((float) (Math.PI / 2.0)), tileUv(0, 1), new int[]{0, 1, 2, 3}); // north
			addFace(builder, new Matrix4f().rotationX((float) (-Math.PI / 2.0)), tileUv(2, 1), new int[]{2, 3, 0, 1}); // south (sun)
			addFace(builder, new Matrix4f().rotationX((float) Math.PI), tileUv(1, 0), new int[]{3, 0, 1, 2}); // zenith
			addFace(builder, new Matrix4f().rotationZ((float) (Math.PI / 2.0)), tileUv(1, 1), new int[]{1, 2, 3, 0}); // east
			addFace(builder, new Matrix4f().rotationZ((float) (-Math.PI / 2.0)), tileUv(3, 1), new int[]{3, 0, 1, 2}); // west

			try (MeshData mesh = builder.buildOrThrow()) {
				return RenderSystem.getDevice().createBuffer(
					() -> "God sky vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, mesh.vertexBuffer());
			}
		}
	}

	private record TileUv(float u0, float v0, float u1, float v1) {
	}

	private static TileUv tileUv(int tx, int ty) {
		float insetU = 0.5F / IMAGE_W;
		float insetV = 0.5F / IMAGE_H;
		return new TileUv(
			(tx * TILE) / (float) IMAGE_W + insetU,
			(ty * TILE) / (float) IMAGE_H + insetV,
			((tx + 1) * TILE) / (float) IMAGE_W - insetU,
			((ty + 1) * TILE) / (float) IMAGE_H - insetV
		);
	}

	private static void addFace(BufferBuilder builder, Matrix4f transform, TileUv uv, int[] order) {
		vertex(builder, transform, -CUBE_HALF_SIZE, -CUBE_HALF_SIZE, -CUBE_HALF_SIZE, cornerU(uv, order[0]), cornerV(uv, order[0]));
		vertex(builder, transform, -CUBE_HALF_SIZE, -CUBE_HALF_SIZE, CUBE_HALF_SIZE, cornerU(uv, order[1]), cornerV(uv, order[1]));
		vertex(builder, transform, CUBE_HALF_SIZE, -CUBE_HALF_SIZE, CUBE_HALF_SIZE, cornerU(uv, order[2]), cornerV(uv, order[2]));
		vertex(builder, transform, CUBE_HALF_SIZE, -CUBE_HALF_SIZE, -CUBE_HALF_SIZE, cornerU(uv, order[3]), cornerV(uv, order[3]));
	}

	private static float cornerU(TileUv uv, int corner) {
		return corner <= 1 ? uv.u0() : uv.u1();
	}

	private static float cornerV(TileUv uv, int corner) {
		return (corner == 0 || corner == 3) ? uv.v0() : uv.v1();
	}

	private static void vertex(BufferBuilder builder, Matrix4f transform, float x, float y, float z, float u, float v) {
		VertexConsumer vertex = transform == null ? builder.addVertex(x, y, z) : builder.addVertex(transform, x, y, z);
		vertex.setUv(u, v).setColor(WHITE);
	}
}
