package mod_TreeBreaker;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		return null;
	}

	public void registerTickHandler() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

	public void addKeyBinding() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u

	}

}
