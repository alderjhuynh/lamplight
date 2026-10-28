package com.aura.lamplight.tinkerer.charm;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ResolvedCharm {

	public record InsetApplication(@Nullable CharmMaterial material, double potency,
	                               List<CharmEffect> effects, boolean alloy) {}

	private final CharmTemplate template;
	private final CharmComponent component;
	private final CharmMaterial setting;
	private final List<InsetApplication> insets;

	private ResolvedCharm(final CharmTemplate template, final CharmComponent component,
	                      final CharmMaterial setting, final List<InsetApplication> insets) {
		this.template = template;
		this.component = component;
		this.setting = setting;
		this.insets = insets;
	}

	public static @Nullable ResolvedCharm of(final ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}
		CharmComponent component = stack.get(ModDataComponents.CHARM);
		if (component == null) {
			return null;
		}
		CharmTemplate template = CharmTemplates.byId(component.templateId());
		CharmMaterial setting = CharmMaterials.byId(component.settingItem());
		if (template == null || setting == null) {
			return null;
		}
		double base = setting.settingMultiplier() * template.focus();
		List<InsetApplication> resolved = new ArrayList<>();
		for (CharmComponent.Inset inset : component.insets()) {
			CharmMaterial material = CharmMaterials.byId(inset.item());
			List<CharmEffect> effectSource = material == null
					? List.of()
					: template.effectsFor(material.item(), material.insets());
			resolved.add(new InsetApplication(material, base, effectSource, hasAlloy(effectSource)));
		}
		ResolvedCharm charm = new ResolvedCharm(template, component, setting, resolved);
		charm.foldAlloy();
		return charm;
	}

	private static boolean hasAlloy(final List<CharmEffect> effects) {
		for (CharmEffect effect : effects) {
			if (effect instanceof AlloyCharmEffect) {
				return true;
			}
		}
		return false;
	}

	private void foldAlloy() {
		double bonus = 0.0;
		for (InsetApplication inset : this.insets) {
			if (inset.alloy() && inset.material() != null) {
				for (CharmEffect effect : inset.effects()) {
					if (effect instanceof AlloyCharmEffect alloy) {
						bonus += Math.min(alloy.cap(), alloy.bonusPerPotency() * inset.potency());
					}
				}
			}
		}
		if (bonus <= 0.0) {
			return;
		}
		for (int i = 0; i < this.insets.size(); i++) {
			InsetApplication inset = this.insets.get(i);
			if (inset.alloy() || inset.material() == null) {
				continue;
			}
			this.insets.set(i, new InsetApplication(inset.material(),
					inset.potency() * (1.0 + bonus), inset.effects(), false));
		}
	}

	public void mergeInto(final CharmAggregate aggregate) {
		for (InsetApplication inset : this.insets) {
			if (inset.material() == null || inset.alloy()) {
				continue;
			}
			for (CharmEffect effect : inset.effects()) {
				if (effect instanceof AlloyCharmEffect) {
					continue; 
				}
				aggregate.mergeInset(effect, inset.potency());
			}
		}
	}

	public CharmTemplate template() {
		return this.template;
	}

	public CharmComponent component() {
		return this.component;
	}

	public CharmMaterial setting() {
		return this.setting;
	}

	public List<InsetApplication> insets() {
		return List.copyOf(this.insets);
	}

	public @Nullable Identifier insetItem(final int index) {
		List<CharmComponent.Inset> insets = this.component.insets();
		return index < insets.size() ? insets.get(index).item() : null;
	}
}
