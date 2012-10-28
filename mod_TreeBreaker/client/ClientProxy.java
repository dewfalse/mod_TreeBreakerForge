package mod_TreeBreaker.client;

import mod_TreeBreaker.CommonProxy;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void addKeyBinding() {
		KeyBindingRegistry.registerKeyBinding(new ClientKeyHandler());
	}

	@Override
	public void registerTickHandler() {
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}
}
