package com.aura.lamplight.folio.server;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import com.aura.lamplight.folio.item.SpellbookItem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

// this is temporary rendering I will be replacing it with a better system
public final class FolioBossBars {
    private FolioBossBars() {}

    private static final Map<UUID, Bars> BARS = new HashMap<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(FolioBossBars::tick);
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> remove(handler.player.getUUID()));
    }

    private static void tick(final MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            update(player);
        }

        Iterator<Map.Entry<UUID, Bars>> iterator = BARS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Bars> entry = iterator.next();
            if (server.getPlayerList().getPlayer(entry.getKey()) == null) {
                entry.getValue().detach();
                iterator.remove();
            }
        }
    }

    private static void update(final ServerPlayer player) {
        Bars bars = BARS.get(player.getUUID());
        if (bars == null || bars.owner != player) {
            if (bars != null) {
                bars.detach();
            }
            bars = new Bars(player);
            BARS.put(player.getUUID(), bars);
        }

        double max = FolioManaHandler.maxMana(player);
        double mana = Math.max(0.0D, Math.min(max, FolioManaHandler.mana(player)));
        FolioCastHandler.CastView cast = FolioCastHandler.castView(player);

        boolean showCast = cast != null && !player.isSpectator(); 
        boolean showMana = max > 0.0D && !player.isSpectator() && max != mana
            && (cast != null || mana < max || SpellbookItem.findEquipped(player) != null);

        bars.mana.setVisible(showMana);
        if (showMana) {
            bars.mana.setProgress((float) (mana / max));
            bars.mana.setName(Component.translatable("bossbar.lamplight.mana",
                (int) Math.ceil(mana), (int) Math.ceil(max)));
        }

        bars.cast.setVisible(showCast);
        if (showCast) {
            bars.cast.setProgress(cast.progress());
            bars.cast.setName(cast.spell().name());
        }
    }

    private static void remove(final UUID id) {
        Bars bars = BARS.remove(id);
        if (bars != null) {
            bars.detach();
        }
    }

    private static final class Bars {
        final ServerPlayer owner;
        final ServerBossEvent mana = new ServerBossEvent(
            UUID.randomUUID(), Component.translatable("bossbar.lamplight.mana", 0, 0),
            BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
        final ServerBossEvent cast = new ServerBossEvent(
            UUID.randomUUID(), Component.empty(),
            BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

        Bars(final ServerPlayer owner) {
            this.owner = owner;
            this.mana.setVisible(false);
            this.cast.setVisible(false);
            this.mana.addPlayer(owner);
            this.cast.addPlayer(owner);
        }

        void detach() {
            this.mana.removeAllPlayers();
            this.cast.removeAllPlayers();
        }
    }
}
