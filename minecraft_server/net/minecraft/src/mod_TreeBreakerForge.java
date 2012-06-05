package net.minecraft.src;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.forge.IConnectionHandler;
import net.minecraft.src.forge.IPacketHandler;
import net.minecraft.src.forge.MessageManager;
import net.minecraft.src.forge.MinecraftForge;
import net.minecraft.src.forge.NetworkMod;
import net.minecraft.src.mod_StoneBreakerForge.BreakResister;

public class mod_TreeBreakerForge extends NetworkMod implements IConnectionHandler, IPacketHandler, ICommandListener {
	public static String channel = "tbf";
	@MLProp
	public static boolean breakwood = true;

	@MLProp
	public static boolean breakleaves = true;

	@MLProp(info = "Additional target block IDs. Separate by ','")
	public static String additionalTargets = "";

	public static Set<Integer> targetIDs = new LinkedHashSet();

	@MLProp(info = "maximum number of block break (0 = unlimited)")
	public static int breaklimit = 0;

	public static final int cmd_break = 0;
	public static final int cmd_mode = 1;
	public static final int cmd_target = 2;
	public static final int cmd_limit = 3;
	public static final int cmd_itembreak = 4;

	public static mod_TreeBreakerForge instance = null;
	public MinecraftServer minecraftServer = null;

	public static ArrayList<String> userList = new ArrayList<String>();

	class BreakResister {
		public EntityPlayerMP player;
		int i;
		int j;
		int k;
		int blockId;
		int metadata;
		int stacksize;
		int itemdamage;
		World worldObj;

		public BreakResister(EntityPlayerMP entityplayermp, int i, int j, int k, int blockId, int metadata) {
			this.player = entityplayermp;
			this.i = i;
			this.j = j;
			this.k = k;
			this.blockId = blockId;
			this.metadata = metadata;
			this.worldObj = entityplayermp.worldObj;
		}
	}

	public mod_TreeBreakerForge() {
		instance = this;
	}

	@Override
	public String getVersion() {
		return "0.0.4";
	}

	@Override
	public void load() {
		targetIDs.clear();
		if(breakwood) {
			targetIDs.add(Block.wood.blockID);
		}
		else {
			targetIDs.remove(Block.wood.blockID);
		}
		if(breakleaves) {
			targetIDs.add(Block.leaves.blockID);
		}
		else {
			targetIDs.remove(Block.leaves.blockID);
		}
		String str = additionalTargets;
		String[] tokens = str.split(",");
		try {
			for(String token : tokens) {
				targetIDs.add(Integer.parseInt(token.trim()));
			}
		} catch(NumberFormatException e) {

		}

		String strMode = "TreeBreaker target =";
		strMode += " " + targetIDs;
		System.out.println(strMode);

		MinecraftForge.registerConnectionHandler(instance);
		ModLoader.setInGameHook(this, true, true);
	}

	@Override
	public void onPacketData(NetworkManager network, String channel, byte[] data) {
		if(channel.equals(this.channel) == false) {
			return;
		}

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

		try {
			int packetType = stream.readInt();
			int as_size = stream.readInt();
			ArrayList<String> as = new ArrayList<String>();
			for(int i = 0; i < as_size; i++) {
				as.add(stream.readUTF());
			}
			int ai_size = stream.readInt();
			ArrayList<Integer> ai = new ArrayList<Integer>();
			for(int i = 0; i < ai_size; i++) {
				ai.add(stream.readInt());
			}

			if(packetType == cmd_break) {
				if(as.size() == 0) return;
				EntityPlayerMP player = minecraftServer.configManager.getPlayerEntity(as.get(0));
				if(player == null) return;
				if(ai.size() < 5) return;
				int i = ai.get(0);
				int j = ai.get(1);
				int k = ai.get(2);
				int blockId = ai.get(3);
				int metadata = ai.get(4);
				BreakResister breakResister = new BreakResister(player, i, j, k, blockId, metadata);
				breakBlock(breakResister);
			}
			else if(packetType == cmd_itembreak) {
				EntityPlayerMP player = minecraftServer.configManager.getPlayerEntity(as.get(0));
				if(player == null) return;
				breakItem(player);
			}

			//System.out.println("RECV: " + channel + " " + packetType + " " + as.toString() + " " + ai.toString());
			//EntityPlayerMP player = minecraftServer.configManager.getPlayerEntity(as.get(0));
			//System.out.println(player.username);

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void breakItem(EntityPlayerMP entityplayermp) {
        ItemStack itemstack = entityplayermp.getCurrentEquippedItem();

        if(itemstack != null) {
	        itemstack.onItemDestroyedByUse(entityplayermp);
	        entityplayermp.destroyCurrentEquippedItem();
        }
	}

	@Override
	public boolean onTickInGame(MinecraftServer minecraftserver) {
		if(this.minecraftServer == null) {
			this.minecraftServer = minecraftserver;
		}
		if(userList.isEmpty() == false) {
			//System.out.println(userList.toString());
		}
		ArrayList<String> users = new ArrayList<String>();
		for(String user : userList) {
			if(minecraftServer.configManager.getPlayerEntity(user) == null) {
				users.add(user);
				continue;
			}
			sendBreakMode(user);
			sendTargetIds(user);
			sendBreakLimit(user);
		}
		userList.clear();
		userList.addAll(users);
		return true;
	}

	public void breakBlock(BreakResister breakResister) {
		//System.out.println("breakBlock");

		int blockId = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
		if(Block.blocksList[blockId] == null) {
			return;
		}

		Block b = Block.blocksList[blockId];
		if(breakwood == false && b instanceof BlockLog) return;
		if(breakleaves == false && b instanceof BlockLeaves) return;
		if(b instanceof BlockLog || b instanceof BlockLeaves) {

		}
		else if(targetIDs.contains(blockId) == false) {
			return;
		}

		Material material = breakResister.worldObj.getBlockMaterial(breakResister.i, breakResister.j, breakResister.k);
		if(material.isSolid() == false) {
			return;
		}

		//System.out.printf("breakBlock %d, %d, %d\n", breakResister.i, breakResister.j, breakResister.k);

        if (breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k) != 0)
        {
    		//breakResister.player.itemInWorldManager.blockHarvessted(breakResister.i, breakResister.j, breakResister.k);


        	// copy from blockHarvessted
            int i = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
            int j = breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k);
            breakResister.worldObj.playAuxSFXAtEntity(breakResister.player, 2001, breakResister.i, breakResister.j, breakResister.k, i + (breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k) << 12));
            boolean flag = breakResister.player.itemInWorldManager.removeBlock(breakResister.i, breakResister.j, breakResister.k);

            if (breakResister.player.itemInWorldManager.isCreative())
            {
                ((EntityPlayerMP)breakResister.player).playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
            }
            else
            {
                ItemStack itemstack = breakResister.player.getCurrentEquippedItem();
                boolean flag1 = breakResister.player.canHarvestBlock(Block.blocksList[i]);

                if (itemstack != null)
                {
                    itemstack.onDestroyBlock(i, breakResister.i, breakResister.j, breakResister.k, breakResister.player);

                    if (itemstack.stackSize == 0)
                    {
                        itemstack.onItemDestroyedByUse(breakResister.player);
                        breakResister.player.destroyCurrentEquippedItem();
                    }
                }

                if (flag && flag1)
                {
                    Block.blocksList[i].harvestBlock(breakResister.worldObj, breakResister.player, (int)breakResister.player.posX, (int)breakResister.player.posY, (int)breakResister.player.posZ, j);
                }
            }



        	breakResister.player.playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
        }
	}

	public void sendBreakLimit(String playerName) {
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<Integer> ai = new ArrayList<Integer>();
		ai.add(breaklimit);
		sendPacket(cmd_limit, playerName, as, ai);
	}

	public void sendBreakMode(String playerName) {
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<Integer> ai = new ArrayList<Integer>();

		int nMode = 0;
		if(breakwood) nMode |= 1;
		if(breakleaves) nMode |= 2;
		ai.add(nMode);

		sendPacket(cmd_mode, playerName, as, ai);
	}

	public void sendTargetIds(String playerName) {
		ArrayList<String> as = new ArrayList<String>();
		ArrayList<Integer> ai = new ArrayList<Integer>();
		as.add(additionalTargets);

		sendPacket(cmd_target, playerName, as, ai);
	}

	public static void sendPacket(int packetType, String playerName, ArrayList<String> as, ArrayList<Integer> ai) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);

		try {
			stream.writeInt(packetType);
			stream.writeInt(as.size());
			for(String s : as) {
				stream.writeUTF(s);

			}
			stream.writeInt(ai.size());
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
		ModLoader.getMinecraftServerInstance().configManager.sendPacketToPlayer(playerName, packet);

		//String s = "SEND: " + channel + " " + packetType + " " + as.toString() + " " + ai.toString();
		//System.out.println(s);
	}

	@Override
	public void onConnect(NetworkManager network) {
		MessageManager.getInstance().registerChannel(network, this, channel);

	}

	@Override
	public void onLogin(NetworkManager network, Packet1Login login) {
		userList.add(login.username);
	}

	@Override
	public void onDisconnect(NetworkManager network, String message,
			Object[] args) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public boolean onServerCommand(String command, String sender,
			ICommandListener listener) {
		//System.out.println(command + ", " + sender);
		if(command.startsWith("TreeBreaker") == false) return false;
		String[] op = command.split(" ");

		if(op.length <= 1)  return false;

		Logger minecraftLogger = Logger.getLogger("Minecraft");
		boolean bUpdate = false;
		if(op[1].equalsIgnoreCase("target")) {
			if(op.length > 3) {
				int id = 0;
				boolean bAdd = op[2].equalsIgnoreCase("add");
				boolean bDel = op[2].equalsIgnoreCase("del");
				try {
					id = Integer.parseInt(op[3]);
					bUpdate = true;
				}
				catch(NumberFormatException e) {
					for(Block block : Block.blocksList) {
						if(block == null) continue;
						String blockName = block.getBlockName();
						if(blockName == null) continue;
						blockName = blockName.replaceFirst("tile.", "").replaceFirst("item.", "");
						if(blockName.equalsIgnoreCase(op[3])) {
							id = block.blockID;
							bUpdate = true;
						}
					}
				}

				if(bUpdate) {
					if(bAdd) {
						targetIDs.add(id);
						additionalTargets = targetIDs.toString().replace("[", "").replace("]", "");
						String s = "";
						for(int i : targetIDs) {
							Block b = Block.blocksList[i];
							if(b != null) {
								s += " " + b.getBlockName().replace("tile.", "") + "[" + i + "]";
							}
						}

						minecraftLogger.info("TreeBreaker: UPDATED! target = " + s);
					}
					else if(bDel) {
						targetIDs.remove(id);
						additionalTargets = targetIDs.toString().replace("[", "").replace("]", "");
						minecraftLogger.info("TreeBreaker: UPDATED! target = " + targetIDs.toString());
					}
					else {
						bUpdate = false;
					}
				}
			}
		}
		else if(op[1].equalsIgnoreCase("mode")) {
			if(op.length > 3) {
				bUpdate = true;
				String modeName = op[2];
				boolean flg = false;
				if(op[3].equalsIgnoreCase("ON")) {
					flg = true;
				}
				else if(op[3].equalsIgnoreCase("OFF") == false) {
					bUpdate = false;
				}

				if(bUpdate) {
					if(modeName.equalsIgnoreCase("wood")) {
						breakwood = flg;
					}
					else if(modeName.equalsIgnoreCase("leaves")) {
						breakleaves = flg;
					}
					else {
						bUpdate = false;
					}
				}

				if(bUpdate) {
					String s = "";
					if(breakwood) {
						s += " WOOD";
					}
					if(breakleaves) {
						s += " LEAVES";
					}
					minecraftLogger.info("TreeBreaker: UPDATED! mode =" + s );
				}
			}
		}
		else if(op[1].equalsIgnoreCase("limit")) {
			if(op.length > 2) {
				try {
					int i = Integer.parseInt(op[2]);
					breaklimit = i;
					bUpdate = true;
					if(bUpdate) {
						minecraftLogger.info("TreeBreaker: UPDATED! breaklimit = " + breaklimit);
					}
				}
				catch(NumberFormatException e) {
				}
			}
		}

		if(bUpdate) {
			for(String s : minecraftServer.configManager.getPlayerNamesAsList()) {
				userList.add(s);
			}
		}
		else {
			minecraftLogger.info("TreeBreaker: Unknown console command.");
		}
		return true;
	}

	@Override
	public void log(String var1) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public String getUsername() {
		// TODO 自動生成されたメソッド・スタブ
		return "mod_TreeBreaker";
	}

}
