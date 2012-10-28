package mod_TreeBreaker.client;

import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import mod_TreeBreaker.EnumPacketType;
import mod_TreeBreaker.Position;
import mod_TreeBreaker.TreeBreaker;
import mod_TreeBreaker.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EnumMovingObjectType;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.Item;
import net.minecraft.src.ItemAxe;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler {
	private int prev_blockHitWait = 0;
	private Position prev_pos = new Position(0,0,0);
	private int prev_sideHit = 0;
	private int prev_blockId = 0;
	private int prev_metadata = 0;
	//private boolean debugmode = true;
	public static Set<Position> vectors = new LinkedHashSet();
	public static Set<Position> positions = new LinkedHashSet();
	private int breaking_blockId = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
			if (guiscreen == null) {
				onTickInGame();
			}
		}
	}

	private void onTickInGame() {
		checkPlayerBreak();

		Minecraft mc = ModLoader.getMinecraftInstance();

		if (mc.objectMouseOver == null) {
			return;
		}

		if (mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			int i = mc.objectMouseOver.blockX;
			int j = mc.objectMouseOver.blockY;
			int k = mc.objectMouseOver.blockZ;
			prev_pos.set(i, j, k);
			prev_sideHit = mc.objectMouseOver.sideHit;
			prev_blockId = mc.theWorld.getBlockId(i, j, k);
			Block block = Block.blocksList[prev_blockId];
			prev_metadata = mc.theWorld.getBlockMetadata(i, j, k);
			return;
		}
	}

	private void checkPlayerBreak() {
		int blockHitWait = Util.getBlockHitWait();
		if(blockHitWait == -1) {
			return;
		}
		boolean breakflag = (blockHitWait == 5 && blockHitWait != prev_blockHitWait);
		prev_blockHitWait = blockHitWait;

		if(breakflag) {
			breaking_blockId = prev_blockId;
			String str = "Block ID=" + prev_blockId + " Class=" + Block.blocksList[prev_blockId].getClass().getName();
			Util.debugPrintChatMessage(str);
		}

		Minecraft mc = ModLoader.getMinecraftInstance();
		ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
		if(itemStack == null) {
			positions.clear();
			return;
		}

		Item item = Item.itemsList[itemStack.itemID];
		if(item == null) {
			positions.clear();
			return;
		}

		if(item instanceof ItemAxe == false && TreeBreaker.config.getTools().contains(item.getClass()) == false) {
			positions.clear();
			return;
		}

		if(breakflag) {
			positions.clear();

			if(TreeBreaker.config.getWood().contains(prev_blockId) == false && TreeBreaker.config.getLeaves().contains(prev_blockId) == false) {
				String str = prev_blockId + "not included in " + TreeBreaker.config.getWood().toString() + TreeBreaker.config.getLeaves().toString();
				Util.debugPrintChatMessage(str);
				return;
			}
			for(Position pos : getNextBreak(prev_pos, prev_blockId)) {
				positions.add(pos);
			}
		}

		continueBreak(prev_blockId);
	}

	private void continueBreak(int prev_blockId2) {
		Set<Position> oldPosition = new LinkedHashSet();
		if(positions.isEmpty() == false) {
			oldPosition.addAll(positions);
			positions.clear();
		}

		int n = 0;
		for(Position pos : oldPosition) {
			if(n < 16) {
				Minecraft mc = ModLoader.getMinecraftInstance();
				String playerName = mc.thePlayer.getEntityName();
				if(breaking_blockId == mc.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z)) {
					int metadata = mc.theWorld.getBlockMetadata((int)pos.x, (int)pos.y, (int)pos.z);

					try {
						Util.sendPacket(EnumPacketType.destroy.ordinal(), new String[]{playerName}, new int[]{pos.x, pos.y, pos.z, 0, breaking_blockId, metadata, TreeBreaker.config.drop_here ? 1 : 0});
						Util.debugPrintChatMessage("continueBreak " + pos.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					for(Position nextPos : getNextBreak(pos, breaking_blockId)) {
						positions.add(nextPos);
					}
				}
			}
			else {
				positions.add(pos);
			}
			n++;
		}
	}

	private Set<Position> getNextBreak(Position prev_pos, int blockId) {
		Set<Position> newPositions = new LinkedHashSet();
		for(Position vector : getVector()) {
			Position pos = new Position(prev_pos);
			pos.add(vector);
			Minecraft mc = ModLoader.getMinecraftInstance();
			int id = mc.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
			boolean bSame = false;
			if(TreeBreaker.config.getWood().contains(id) && TreeBreaker.config.getWood().contains(blockId)) {
				bSame = true;
			}
			else if(TreeBreaker.config.getLeaves().contains(id) && TreeBreaker.config.getLeaves().contains(blockId)) {
				bSame = true;
			}
			else {
				String str = "targetID=" + TreeBreaker.config.getWood().toString() + TreeBreaker.config.getLeaves().toString() + "not contains " +  id + " or " + "getNextBreak skip because id=" + id + " not equals blockId=" + blockId;
				Util.debugPrintChatMessage(str);
			}

			if(bSame) {
				newPositions.add(pos);
			}
		}
		return newPositions;
	}

	private Set<Position> getVector() {
		if(vectors.isEmpty()) {
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
		}

		return vectors;
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		return "TreeBreaker.TickHandler";
	}

}
