package mod_TreeBreaker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.NetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.FMLCommonHandler;
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
				Util.consoleLog(strData.toString());
			}
			else if(side == Side.CLIENT) {
				System.out.println(strData.toString());
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
		} catch (IOException e) {
			// TODO Ž©“®¶¬‚³‚ê‚½ catch ƒuƒƒbƒN
			e.printStackTrace();
		}

	}

}
