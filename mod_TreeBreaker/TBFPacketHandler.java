package mod_TreeBreaker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mod_StoneBreaker.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.Item;
import net.minecraft.src.ItemAxe;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
public class TBFPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(NetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if(packet.channel.equals(Config.channel) == false) {
			return;
		}

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));

		try {
			int type = stream.readInt();
			int as_size = stream.readInt();
			List<String> strData = new ArrayList();
			for(int i = 0; i < as_size; i++) {
				strData.add(stream.readUTF());
			}
			int ai_size = stream.readInt();
			List<Integer> integerData = new ArrayList();
			for(int i = 0; i < ai_size; i++) {
				integerData.add(stream.readInt());
			}

			Side side = FMLCommonHandler.instance().getEffectiveSide();
			if(side == Side.SERVER) {
				//Util.consoleLog(strData.toString());
			}
			else if(side == Side.CLIENT) {
				Util.debugPrintChatMessage(strData.toString());
				switch(EnumPacketType.values()[type]) {
				case config:
					TreeBreaker.config.setLeaves(strData.get(0));
					TreeBreaker.config.setWood(strData.get(1));
					TreeBreaker.config.setTool(strData.get(2));
					break;
				default:
					break;
				}
			}
			if(EnumPacketType.values()[type] == EnumPacketType.destroy) {
				if(Util.debug) MinecraftServer.logger.info("TBF receive: destroy " + strData.toString() + ", " + integerData.toString());
				Object[] obj = Util.getServerWorldAndPlayer(strData.get(0));

				EntityPlayerMP thePlayer = (EntityPlayerMP)obj[1];
				World world = (World)obj[0];

				Position pos = new Position(integerData.get(0), integerData.get(1), integerData.get(2));
				boolean drop_here = integerData.get(6) > 0;
				breakBlock(world, thePlayer, pos, drop_here);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean breakBlock(World world, EntityPlayer player, Position pos, boolean drop_here) {
		if(TreeBreaker.config.debug) {
			Util.consoleLog("breakBlock " + pos.toString() + " start");
		}

		if(pos.x == 0.0F && pos.y == 0.0F && pos.z == 0.0F) {
			if(TreeBreaker.config.debug) Util.consoleLog("breakBlock abnormal");
		}

		// height limitation
		if(pos.y < 1 || pos.y > 255) {
			if(TreeBreaker.config.debug) Util.consoleLog("breakBlock skip(height)");
			return true;
		}


		// must equip item tool
		ItemStack itemstack = player.getCurrentEquippedItem();
		if (itemstack == null)
		{
			if(TreeBreaker.config.debug) Util.consoleLog("breakBlock skip(itemstack == null)");
			return false;
		}
		if (itemstack.stackSize == 0)
		{
			if(TreeBreaker.config.debug) Util.consoleLog("breakBlock skip(itemstack.stackSize == 0)");
			return false;
		}

		if(TreeBreaker.config.debug) Util.consoleLog("breakBlock itemstack.itemDamage == " + String.valueOf(itemstack.getItemDamage()));
		String itemName = Item.itemsList[itemstack.itemID].getClass().getName();
		if(Item.itemsList[itemstack.itemID] instanceof ItemAxe == false && !TreeBreaker.config.getTools().contains(Item.itemsList[itemstack.itemID].getClass())) {
			if(TreeBreaker.config.debug) Util.consoleLog("breakBlock skip(Item not ItemTool)");
			return false;
		}

		Block block = Block.blocksList[world.getBlockId((int)pos.x, (int)pos.y, (int)pos.z)];
		if(block == null) {
			return false;
		}
		int id = world.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
		int metadata = world.getBlockMetadata((int)pos.x, (int)pos.y, (int)pos.z);
        boolean ret = true;

        if(world.setBlockWithNotify((int)pos.x, (int)pos.y, (int)pos.z, 0)) {
        	if(drop_here) {
            	block.harvestBlock( world, player, (int)player.posX, (int)player.posY + 2, (int)player.posZ, metadata);
        	}
        	else {
            	block.harvestBlock( world, player, (int)pos.x, (int)pos.y, (int)pos.z, metadata);
        	}
        }
		return ret;

	}

}
