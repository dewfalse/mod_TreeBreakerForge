package mod_TreeBreaker;

import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.NetHandler;
import net.minecraft.src.NetLoginHandler;
import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet1Login;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;

public class TBFConnectionHandler implements IConnectionHandler {

	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler,
			NetworkManager manager) {
		try {
			String leaves = "";
			for(int i : TreeBreaker.config.getLeaves()) {
				leaves += String.valueOf(i);
				leaves += ",";
			}
			String wood = "";
			for(int i : TreeBreaker.config.getWood()) {
				wood += String.valueOf(i);
				wood += ",";
			}
			String tool = "";
			for(Class c : TreeBreaker.config.getTools()) {
				tool += c.toString();
				tool += ",";
			}
			Util.sendPacketToPlayer(player, EnumPacketType.config.ordinal(), new String[]{leaves, wood, tool}, new int[]{});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler,
			NetworkManager manager) {
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server,
			int port, NetworkManager manager) {
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler,
			MinecraftServer server, NetworkManager manager) {
	}

	@Override
	public void connectionClosed(NetworkManager manager) {
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler,
			NetworkManager manager, Packet1Login login) {
	}

}
