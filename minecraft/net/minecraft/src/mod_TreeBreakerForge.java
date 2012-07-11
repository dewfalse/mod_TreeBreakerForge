package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.forge.MessageManager;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.NetworkMod;

public class mod_TreeBreakerForge extends NetworkMod implements IConnectionHandler, IPacketHandler {
	public static String channel = "tbf";
	class Position {
		int x;
		int y;
		int z;

		public Position(long l, long m, long n) {
			this.x = (int) l;
			this.y = (int) m;
			this.z = (int) n;
		}

		public Position subtract(Position position) {
			return new Position(position.x - x, position.y - y, position.z - z);
		}

		public Position addVector(int x2, int y2, int z2) {
			return new Position(x2 + x, y2 + y, z2 + z);
		}

	    public String toString()
	    {
	        return (new StringBuilder()).append("(").append(x).append(", ").append(y).append(", ").append(z).append(")").toString();
	    }

	    @Override
	    public int hashCode() {
	    	return 13 * 13 * x + 13 * y + z;
	    }

	    @Override
	    public boolean equals(Object obj) {
	    	if(obj == null) return false;
	    	if(obj instanceof Position) {
	    		Position pos = (Position)obj;
	    		if(x == pos.x && y == pos.y && z == pos.z) {
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	}

	@MLProp(info = "Setting for SinglePlay")
	public static boolean breakwood = true;

	@MLProp(info = "Setting for SinglePlay")
	public static boolean breakleaves = true;

	public static boolean allow_breakwood = true;
	public static boolean allow_breakleaves = true;
	public static boolean breakwood_flg = true;
	public static boolean breakleaves_flg = true;

	public static int mode = 0;

	@MLProp(info = "Setting for SinglePlay. Additional target block IDs. Separate by ','")
	public static String additionalTargets = "";

	public static Set<Integer> targetIDs = new LinkedHashSet();


	@MLProp(info = "true: one break, all targetIDs   false: one break, one targetID")
	public static boolean break_alltargets_atonce = false;

	public static int prev_i;
	public static int prev_j;
	public static int prev_k;
	public static int sideHit;
	public static int blockId;
	public static int metadata;
	public static int prev_blockHitWait;
	public static Set<Position> vectors = new LinkedHashSet();
	public static Set<Position> positions = new LinkedHashSet();
	Set<String> additional_tools_set = new LinkedHashSet();

	@MLProp(info = "additional tools class name(separated by ',')")
	public static String additional_tools = "";

	public static boolean debugmode = false;

	@MLProp(info = "toggle mode key(default:50 = 'M')")
	public static int mode_key = 50;

	public static int key_push_times = 0;


	public static int breakcount = 0;
	public static int nBreakLimit = 0;

	@MLProp(info = "Setting for SinglePlay")
	public static int breaklimit = 0;

	public static boolean bObfuscate = true;

	public static final int cmd_break = 0;
	public static final int cmd_mode = 1;
	public static final int cmd_target = 2;
	public static final int cmd_limit = 3;
	public static final int cmd_itembreak = 4;
	public static final int cmd_tool = 5;

	public static boolean bInitMode = false;
	public static boolean bInitTarget = false;
	public static boolean bInitLimit = false;
	public static boolean bRemote = false;

	public static mod_TreeBreakerForge instance = null;
	public static Minecraft minecraft = null;

	@Override
	public String getVersion() {
		return "0.0.5";
	}

	public mod_TreeBreakerForge() {
		instance = this;
	}

	@Override
	public void load() {
		breakwood_flg = breakwood;
		breakleaves_flg = breakleaves;

		targetIDs.clear();
		if(breakwood_flg) {
			targetIDs.add(Block.wood.blockID);
		}
		if(breakleaves_flg) {
			targetIDs.add(Block.leaves.blockID);
		}
		String str = additionalTargets;
		String[] tokens = str.split(",");
		try {
			for(String token : tokens) {
				targetIDs.add(Integer.parseInt(token.trim()));
			}
		} catch(NumberFormatException e) {

		}
		str = additional_tools;
		tokens = str.split(",");
		for(String token : tokens) {
			additional_tools_set.add(token.trim());
		}
		MinecraftForge.registerConnectionHandler(instance);
		ModLoader.setInGameHook(this, true, true);
	}

	public void printTargetIDs(Minecraft minecraft) {
		String strMode = "TreeBreaker target :";
		for(int id : targetIDs) {
			Block b = Block.blocksList[id];
			if(b != null) {
				strMode += " " + b.getBlockName().replace("tile.", "") + "[" + id + "]";
			}
		}

		minecraft.ingameGUI.addChatMessage(strMode);
	}
	public void printMode(Minecraft minecraft) {
		String strMode = "TreeBreaker mode :";
		if(breakwood_flg) {
			strMode += " Wood";
		}
		if(breakleaves_flg) {
			strMode += " Leaves";
		}
		if(targetIDs.size() > 0) {
			strMode += " Others";
		}
		if(targetIDs.size() == 0) {
			strMode += " None";
		}

		minecraft.ingameGUI.addChatMessage(strMode);
	}

	private void changeMode() {
		switch(mode) {
		case 0:
			// wood, leaves, others
			if(allow_breakwood) {
				breakwood_flg = true;
				targetIDs.add(Block.wood.blockID);
			}
			if(allow_breakleaves) {
				breakleaves_flg = true;
				targetIDs.add(Block.leaves.blockID);
			}
			if(allow_breakwood || allow_breakleaves) {
				break;
			}
		case 1:
			// wood, others
			if(allow_breakwood) {
				breakwood_flg = true;
				breakleaves_flg = false;
				targetIDs.add(Block.wood.blockID);
				targetIDs.remove(Block.leaves.blockID);
				break;
			}
		case 2:
			// leaves
			if(allow_breakleaves) {
				breakwood_flg = false;
				breakleaves_flg = true;
				targetIDs.remove(Block.wood.blockID);
				targetIDs.add(Block.leaves.blockID);
				break;
			}
		case 3:
			// none
			breakwood_flg = false;
			breakleaves_flg = false;
			targetIDs.remove(Block.wood.blockID);
			targetIDs.remove(Block.leaves.blockID);
			break;
		}
	}

	@Override
	public boolean onTickInGame(float f, Minecraft minecraft) {
		if(this.minecraft == null) {
			this.minecraft = minecraft;
		}

		if(minecraft.isMultiplayerWorld() == false) {
			allow_breakwood = breakwood;
			allow_breakleaves = breakleaves;
			nBreakLimit = breaklimit;

			String str = additionalTargets;
			String[] tokens = str.split(",");
			targetIDs.add(Block.wood.blockID);
			targetIDs.add(Block.leaves.blockID);
			try {
				for(String token : tokens) {
					targetIDs.add(Integer.parseInt(token.trim()));
				}
			} catch(NumberFormatException e) {

			}
			changeMode();
		}
		bRemote = minecraft.isMultiplayerWorld();

		if(Keyboard.isKeyDown(mode_key)) {
			key_push_times++;
		} else if(key_push_times > 0) {
			mode = (mode + 1) % 4;
			changeMode();
			key_push_times = 0;
			printMode(minecraft);
		}

		if(minecraft.isMultiplayerWorld()) {
			if(bInitMode && bInitTarget && bInitLimit) {
				changeMode();
				printMode(minecraft);
				printTargetIDs(minecraft);
				bInitMode = false;
				bInitTarget = false;
				bInitLimit = false;
			}
		}
		else {
			if(bInitMode == false && bInitTarget == false && bInitLimit == false) {
				changeMode();
				printMode(minecraft);
				printTargetIDs(minecraft);
				bInitMode = true;
				bInitTarget = true;
				bInitLimit = true;
			}
		}

		boolean breakflag = false;

		int blockHitWait = getBlockHitWait(minecraft);
		if(blockHitWait == 5 && blockHitWait != prev_blockHitWait) {
			breakflag = true;
		} else if(blockHitWait == -1) {
			return false;
		}
		prev_blockHitWait = blockHitWait;

		if(breakflag) {

			Block b = Block.blocksList[blockId];
			if(b != null) {
				if(targetIDs.contains(blockId)) {
					if(b instanceof BlockLog) {
						List list = minecraft.theWorld.getLoadedEntityList();
						if(list.isEmpty() == false) {
							Object obj = list.get(list.size() - 1);
							if(obj instanceof EntityItem) {
								if(((EntityItem)obj).item.itemID == Block.wood.blockID) {
									((EntityItem)obj).setPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ);
									((EntityItem)obj).onUpdate();
									minecraft.theWorld.updateEntity((Entity) obj);
								}
							}
						}
					}
					startBreak(minecraft);
				} else {
					if(debugmode) {
						System.out.print("BlockId ");
						System.out.print(blockId);
						System.out.print(" not in targetIDs ");
						System.out.println(targetIDs);
					}
				}
			}

		}

		if(positions.size() > 0) {
			continueBreak(minecraft);
			return true;
		}

		if (minecraft.objectMouseOver == null) {
			return true;
		}

		if (minecraft.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			prev_i = minecraft.objectMouseOver.blockX;
			prev_j = minecraft.objectMouseOver.blockY;
			prev_k = minecraft.objectMouseOver.blockZ;
			sideHit = minecraft.objectMouseOver.sideHit;
			blockId = minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k);
			Block block = Block.blocksList[blockId];
			metadata = minecraft.theWorld.getBlockMetadata(prev_i, prev_j, prev_k);
			Material material = minecraft.theWorld.getBlockMaterial(prev_i, prev_j, prev_k);
			return true;
		}

		return true;
	}

	private int getBlockHitWait(Minecraft minecraft) {
		int blockHitWait = 0;

		if(bObfuscate) {
			String s = "";
			try {
				if(minecraft.playerController instanceof PlayerControllerMP) {
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, minecraft.playerController, "i");
				}
				else if(minecraft.playerController instanceof PlayerControllerSP) {
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerSP.class, minecraft.playerController, "i");
				}
				else if(minecraft.playerController instanceof PlayerControllerCreative) {
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerSP.class, minecraft.playerController, "i");
				}
				return blockHitWait;
			} catch (IllegalArgumentException e) {
				s = "bObfuscate IllegalArgumentException";
				e.printStackTrace();
			} catch (SecurityException e) {
				s = "bObfuscate SecurityException";
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				s = "bObfuscate NoSuchFieldException";
				e.printStackTrace();
			}
			minecraft.ingameGUI.addChatMessage(s);
		}

		bObfuscate = false;

		String s = "";
		try {
			if(minecraft.playerController instanceof PlayerControllerMP) {
				blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, minecraft.playerController, "blockHitDelay");
			}
			else if(minecraft.playerController instanceof PlayerControllerSP) {
				blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerSP.class, minecraft.playerController, "blockHitWait");
			}
			else if(minecraft.playerController instanceof PlayerControllerCreative) {
				blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerSP.class, minecraft.playerController, "field_35647_c");
			}
			return blockHitWait;
		} catch (IllegalArgumentException e) {
			s = "not bObfuscate IllegalArgumentException";
			e.printStackTrace();
		} catch (SecurityException e) {
			s = "not bObfuscate SecurityException";
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			s = "not bObfuscate NoSuchFieldException";
			e.printStackTrace();
		}
		minecraft.ingameGUI.addChatMessage(s);

		return -1;
	}

	private void continueBreak(Minecraft minecraft) {
		if(debugmode) {
			System.out.print("continueBreak start ");
			System.out.println(positions);
		}
		int i = 0;
		Set<Position> oldPositions = new LinkedHashSet();
		Set<Position> newPositions = new LinkedHashSet();
		oldPositions.addAll(positions);

		int breakOnce = 1;

		// 一度に壊すのは１個までとする
		if(oldPositions.size() < breakOnce) {
			int j = 0;
			// １個目以降は次回
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					newPositions.add(position);
				}
				j++;
			}

			j = 0;
			// 周辺ブロックを次回に予約
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					break;
				}
				j++;
				for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
					if(positions.contains(new_pos) == false) {
						newPositions.add(new_pos);
					}
				}
			}

			j = 0;
			// 予約済みブロックを破壊
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					break;
				}
				j++;
				// ツール破損は終了
				if(breakBlock(minecraft, position) == false) {
					positions.clear();
					return;
				}
			}
		}
		// すべて壊す
		else {
			// 周辺ブロックを次回に予約
			for(Position position : oldPositions) {
				for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
					if(positions.contains(new_pos) == false) {
						newPositions.add(new_pos);
					}
				}
			}
			// 予約済みブロックを破壊
			for(Position position : oldPositions) {
				// ツール破損は終了
				if(breakBlock(minecraft, position) == false) {
					positions.clear();
					return;
				}
			}
		}

		positions = newPositions;
		if(positions.size() > 1) {
			if(debugmode) {
				System.out.print("continueBreak end");
				System.out.println(positions);
			}

		}
		if(debugmode) {
			System.out.print("continueBreak end");
			System.out.println(positions);
		}
	}

	// true : continue break
	// false: stop break
	private boolean breakBlock(Minecraft minecraft, Position position) {
		if(debugmode) {
			System.out.print("breakBlock ");
			System.out.print(position.toString());
			System.out.println(" start");
		}

		if(position.x == 0.0F && position.y == 0.0F && position.z == 0.0F) {
			if(debugmode) System.out.println("breakBlock abnormal");

		}

		// height limitation
		if(position.y < 1 || position.y > 255) {
			if(debugmode) System.out.println("breakBlock skip(height)");
			return true;
		}


		// must equip item tool
        ItemStack itemstack = minecraft.thePlayer.getCurrentEquippedItem();
        if (itemstack == null)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack == null)");
        	return false;
        }
        if (itemstack.stackSize == 0)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack.stackSize == 0)");
        	return false;
        }

    	if(debugmode) System.out.printf("breakBlock itemstack.itemDamage == %d\n", itemstack.getItemDamage());
    	String itemName = Item.itemsList[itemstack.itemID].getClass().getName();
		if(Item.itemsList[itemstack.itemID] instanceof ItemAxe == false && !additional_tools_set.contains(itemName)) {
			if(debugmode) System.out.printf("breakBlock skip(Item not ItemTool)");
			return false;
		}

		int id = minecraft.theWorld.getBlockId((int)position.x, (int)position.y, (int)position.z);
		boolean bSame = false;
		if(id == blockId) {
			bSame = true;
		}
		if(breakwood_flg && breakleaves_flg && break_alltargets_atonce) {
			if(targetIDs.contains(id) && targetIDs.contains(blockId)) {
				bSame = true;
			}
		}
		if(bSame == false) {
			if(debugmode) System.out.printf("breakBlock skip(BlockId)");
			return true;
		}
		Block block = Block.blocksList[id];

		if(block == null) {
			if(debugmode) System.out.printf("breakBlock skip(block == null)");
			return true;
		}

        int i = minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z);

        boolean ret = true;
        if(bRemote) {
    		EntityClientPlayerMP player = ((EntityClientPlayerMP)minecraft.thePlayer);

            int currentItem = minecraft.thePlayer.inventory.currentItem;
            player.sendQueue.addToSendQueue(new Packet16BlockItemSwitch(currentItem));

            {
                String[] as = {minecraft.thePlayer.username};
                int[] ai = {(int)position.x, (int)position.y, (int)position.z, id, i};
                sendPacket(cmd_break, as, ai);
            }

        	block.onBlockDestroyedByPlayer(minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
    		player.sendQueue.addToSendQueue(new Packet14BlockDig(2, (int)position.x, (int)position.y, (int)position.z, 0));
            minecraft.theWorld.playAuxSFX(2001, (int)position.x, (int)position.y, (int)position.z, block.blockID + (minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z) << 12));
            boolean flag = minecraft.theWorld.setBlockWithNotify((int)position.x, (int)position.y, (int)position.z, 0);

            if (block != null && flag)
            {
                block.onBlockDestroyedByPlayer( minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
            }

            itemstack.onDestroyBlock(i, (int)position.x, (int)position.y, (int)position.z, minecraft.thePlayer);

            if (itemstack.stackSize == 0)
            {
                itemstack.onItemDestroyedByUse(minecraft.thePlayer);
                minecraft.thePlayer.destroyCurrentEquippedItem();

                String[] as = {minecraft.thePlayer.username};
                int[] ai = {};
                sendPacket(cmd_itembreak, as, ai);

                ret = false;
            }
        }
        else {
            int currentItem = minecraft.thePlayer.inventory.currentItem;

            minecraft.theWorld.playAuxSFX(2001, (int)position.x, (int)position.y, (int)position.z, block.blockID + (minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z) << 12));
            boolean flag = minecraft.theWorld.setBlockWithNotify((int)position.x, (int)position.y, (int)position.z, 0);

            if (block != null && flag)
            {
                block.onBlockDestroyedByPlayer( minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
            }

            itemstack.onDestroyBlock(i, (int)position.x, (int)position.y, (int)position.z, minecraft.thePlayer);
            if (itemstack.stackSize == 0)
            {
                itemstack.onItemDestroyedByUse(minecraft.thePlayer);
                minecraft.thePlayer.destroyCurrentEquippedItem();
                ret = false;
            }

            boolean flag1 = minecraft.thePlayer.canHarvestBlock(block);

            if (flag1)
            {
            	block.harvestBlock(minecraft.theWorld, minecraft.thePlayer, (int)minecraft.thePlayer.posX, (int)minecraft.thePlayer.posY, (int)minecraft.thePlayer.posZ, i);
            }
        }

		breakcount++;

		if(debugmode) System.out.println("breakBlock end");
		if(nBreakLimit > 0 && breakcount > nBreakLimit) {
			breakcount = 0;
			return false;
		}
		return ret;

	}

	public static void sendPacket(int packetType, String[] as, int[] ai) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeInt(packetType);
			stream.writeInt(as.length);
			for(String s : as) {
				stream.writeUTF(s);

			}
			stream.writeInt(ai.length);
			for(int i : ai) {
				stream.writeInt(i);
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	    Packet250CustomPayload packet = new Packet250CustomPayload();
	    packet.channel = channel;
	    packet.data = bytes.toByteArray();
	    packet.length = packet.data.length;
	    ModLoader.sendPacket(packet);

	    if(minecraft != null) {
	    	//String s = packet.channel + " " + packetType + " " + packet.length;

	    	//minecraft.ingameGUI.addChatMessage(s);
	    }
	}

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] data) {
		if(channel.equals(this.channel) == false) {
			return;
		}

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

		int packetType = -1;
		try {
			packetType = stream.readInt();
			int as_size = stream.readInt();
			String[] as = new String[as_size];
			for(int i = 0; i < as_size; i++) {
				as[i] = stream.readUTF();
			}
			int ai_size = stream.readInt();
			int[] ai = new int[ai_size];
			for(int i = 0; i < ai_size; i++) {
				ai[i] = stream.readInt();
			}

			//System.out.println("RECV: " + packetType + " " + as.toString() + " " + ai.toString());

			switch(packetType) {
			case cmd_mode:
				int nMode = ai[0];
				allow_breakwood =  (nMode & 1) > 0;
				allow_breakleaves = (nMode & 2) > 0;
				breakwood_flg = allow_breakwood;
				breakleaves_flg = allow_breakleaves;
				if(breakwood_flg) {
					targetIDs.add(Block.wood.blockID);
				}
				else {
					targetIDs.remove(Block.wood.blockID);
				}
				if(breakleaves_flg) {
					targetIDs.add(Block.leaves.blockID);
				}
				else {
					targetIDs.remove(Block.leaves.blockID);
				}
				bInitMode = true;
				break;
			case cmd_limit:
				nBreakLimit = ai[0];
				bInitLimit = true;
				break;
			case cmd_target:
				targetIDs.clear();
				if(breakwood_flg) {
					targetIDs.add(Block.wood.blockID);
				}
				else {
					targetIDs.remove(Block.wood.blockID);
				}
				if(breakleaves_flg) {
					targetIDs.add(Block.leaves.blockID);
				}
				else {
					targetIDs.remove(Block.leaves.blockID);
				}
				String str = as[0];
				String[] tokens = str.split(",");
				try {
					for(String token : tokens) {
						targetIDs.add(Integer.parseInt(token.trim()));
					}
				} catch(NumberFormatException e) {

				}
				bInitTarget = true;
				break;
			case cmd_tool:
				str = as[0];
				additional_tools = str;
				additional_tools_set.clear();
				tokens = str.split(",");
				for(String token : tokens) {
					if(token.isEmpty()) {
						continue;
					}
					additional_tools_set.add(token.trim());
				}

			}

		    if(minecraft != null) {
		    	//String s = channel + " " + packetType + " " + as.toString() + " " + ai.toString();

		    	//minecraft.ingameGUI.addChatMessage(s);
		    }
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public Position getDirection(Minecraft minecraft) {
		Position player_position = new Position(Math.round(minecraft.thePlayer.posX), Math.round(minecraft.thePlayer.posY), Math.round(minecraft.thePlayer.posZ));
		Position block_position = new Position(prev_i, prev_j, prev_k);
		Position tmp = player_position.subtract(block_position);
		if(tmp.z == 0) {
			return new Position(Math.round(Math.signum(tmp.x)), 0, 0);
		} else if(tmp.x == 0) {
			return new Position(0, 0, Math.round(Math.signum(tmp.x)));
		} else if(Math.abs(tmp.x) > Math.abs(tmp.z)) {
			if(tmp.x > 0) {
				return new Position(1, 0, 0);
			}
			return new Position(-1, 0, 0);
		}
		else {
			if(tmp.z > 0) {
				return new Position(0, 0, 1);
			}
			return new Position(0, 0, -1);
		}
	}

	public Set<Position> getBackDirections(Minecraft minecraft) {
		Position v = getDirection(minecraft);

		Set<Position> set = new LinkedHashSet();
		if(v.x == 1) {
			set.add(new Position(-1, 0, 1));set.add(new Position(-1, 0, 0));set.add(new Position(-1, 0, -1));
			set.add(new Position(-1, 1, 1));set.add(new Position(-1, 1, 0));set.add(new Position(-1, 1, -1));
			set.add(new Position(-1, -1, 1));set.add(new Position(-1, -1, 0));set.add(new Position(-1, -1, -1));
		} else if(v.x == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(1, 0, 0));set.add(new Position(1, 0, -1));
			set.add(new Position(1, 1, 1));set.add(new Position(1, 1, 0));set.add(new Position(1, 1, -1));
			set.add(new Position(1, -1, 1));set.add(new Position(1, -1, 0));set.add(new Position(1, -1, -1));
		} else if(v.z == 1) {
			set.add(new Position(1, 0, -1));set.add(new Position(0, 0, -1));set.add(new Position(-1, 0, -1));
			set.add(new Position(1, 1, -1));set.add(new Position(0, 1, -1));set.add(new Position(-1, 1, -1));
			set.add(new Position(1, -1, -1));set.add(new Position(0, -1, -1));set.add(new Position(-1, -1, -1));
		} else if(v.z == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(0, 0, 1));set.add(new Position(-1, 0, 1));
			set.add(new Position(1, 1, 1));set.add(new Position(0, 1, 1));set.add(new Position(-1, 1, 1));
			set.add(new Position(1, -1, 1));set.add(new Position(0, -1, 1));set.add(new Position(-1, -1, 1));
		}
		return set;
	}

	public void startBreak(Minecraft minecraft) {
		if(debugmode) System.out.println("startBreak start");

		Set<Position> backDirections = getBackDirections(minecraft);
		positions.clear();
		vectors.clear();

		positions.add(new Position(prev_i, prev_j, prev_k));
		vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
		vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
		vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

		vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
		vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
		vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

		vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
		vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
		vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));

		if(debugmode) System.out.println("startBreak end");
	}

	public Set<Position> addNextBreakBlocks(Minecraft minecraft, Position position) {
		Set<Position> newPositions = new LinkedHashSet();
		for(Position vector : vectors) {
			Position pos = position.addVector(vector.x, vector.y, vector.z);
			int id = minecraft.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
			boolean bSame = false;
			if(id == blockId) {
				bSame = true;
			}
			if(breakwood_flg && breakleaves_flg && break_alltargets_atonce) {
				if(targetIDs.contains(id) && targetIDs.contains(blockId)) {
					bSame = true;
				}
			}

			if(bSame) {
				if(positions.contains(pos) == false && newPositions.contains(pos) == false) {
					if(debugmode) {
						System.out.print("addNextBreakBlocks ");
						System.out.println(pos.toString());
					}
					newPositions.add(pos);
				}
			}
		}

		return newPositions;
	}

	@Override
	public void onConnect(NetworkManager network) {
		MessageManager.getInstance().registerChannel(network, this, channel);

	}

	@Override
	public void onLogin(NetworkManager network, Packet1Login login) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void onDisconnect(NetworkManager network, String message,
			Object[] args) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
