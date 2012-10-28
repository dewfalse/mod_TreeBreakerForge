package mod_TreeBreaker.client;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import mod_TreeBreaker.TreeBreaker;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class ClientKeyHandler extends KeyHandler {
	static KeyBinding myBinding = new KeyBinding("TreeBreaker", Keyboard.KEY_M);

	public ClientKeyHandler(KeyBinding[] keyBindings, boolean[] repeatings) {
		super(keyBindings, repeatings);
	}

	public ClientKeyHandler() {
		super(new KeyBinding[]{myBinding}, new boolean[]{false});
	}

	@Override
	public String getLabel() {
		return "TreeBreaker.KeyHandler";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		if(tickEnd) {
			TreeBreaker.config.ToggleMode();
			ModLoader.getMinecraftInstance().ingameGUI.getChatGUI()
			.printChatMessage("TreeBreaker Mode: " + TreeBreaker.config.mode.toString());
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
