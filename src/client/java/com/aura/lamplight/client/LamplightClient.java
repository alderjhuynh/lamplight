package com.aura.lamplight.client;

import com.aura.lamplight.vantage.client.VantageClient;
import com.aura.lamplight.cairn.client.CairnClient;
import com.aura.lamplight.almanac.client.AlmanacClient;
import com.aura.lamplight.hearthwright.client.HearthwrightClient;
import com.aura.lamplight.ambrosia.client.AmbrosiaClient;
import com.aura.lamplight.panacea.client.PanaceaClient;
import com.aura.lamplight.tinkerer.client.TinkererClient;
import com.aura.lamplight.menagerie.client.MenagerieClient;
import com.aura.lamplight.gloaming.client.GloamingClient;
import com.aura.lamplight.arcanum.client.ArcanumClient;
import com.aura.lamplight.armory.client.ArmoryClient;
import com.aura.lamplight.folio.client.FolioClient;
import com.aura.lamplight.wayfarer.client.WayfarerClient;
import com.aura.lamplight.puppeteer.client.PuppeteerClient;
import com.aura.lamplight.picturesque.client.PicturesqueClient;
import net.fabricmc.api.ClientModInitializer;

public class LamplightClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new VantageClient().onInitializeClient();
		new CairnClient().onInitializeClient();
		new AlmanacClient().onInitializeClient();
		new HearthwrightClient().onInitializeClient();
		new AmbrosiaClient().onInitializeClient();
		new PanaceaClient().onInitializeClient();
		new TinkererClient().onInitializeClient();
		new MenagerieClient().onInitializeClient();
		new GloamingClient().onInitializeClient();
		new ArcanumClient().onInitializeClient();
		new ArmoryClient().onInitializeClient();
		new FolioClient().onInitializeClient();
		new WayfarerClient().onInitializeClient();
		new com.aura.lamplight.wayfarer.atlas.AtlasClient().onInitializeClient();
		new PuppeteerClient().onInitializeClient();
		new PicturesqueClient().onInitializeClient();
	}
}
