package mod_TreeBreaker;

import java.util.logging.Logger;

import net.minecraft.src.Block;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = "TreeBreaker", name = "TreeBreaker", version = "1.1")
@NetworkMod(clientSideRequired = false, serverSideRequired = true, channels = { "tbf" }, packetHandler = TBFPacketHandler.class, connectionHandler = TBFConnectionHandler.class, versionBounds = "[1.1]")
public class TreeBreaker {
	@SidedProxy(clientSide = "mod_TreeBreaker.ClientProxy", serverSide = "mod_TreeBreaker.CommonProxy")
	public static CommonProxy proxy;

	@Instance("TreeBreaker")
	public static TreeBreaker instance;

	public static Logger logger = Logger.getLogger("Minecraft");

	public static Config config = new Config();

	@Mod.Init
	public void load(FMLInitializationEvent event) {
		proxy.addKeyBinding();
		proxy.registerTickHandler();
		NetworkRegistry.instance().registerGuiHandler(instance, proxy);

		logger.info("mod_TreeBreaker.load");
	}

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		Configuration cfg = new Configuration(
				event.getSuggestedConfigurationFile());

		cfg.load();

		Property tools = cfg.get(Configuration.CATEGORY_GENERAL,
				"additionalTools", "");
		tools.comment = "additional tools class name, separated by ','";
		String additionalTools = tools.value;

		Property wood = cfg.get(Configuration.CATEGORY_GENERAL,
				"additionalWoods", "");
		wood.comment = "Additional wood block IDs, separate by ','";
		String additionalWoods = wood.value;

		Property leaves = cfg.get(Configuration.CATEGORY_GENERAL,
				"additionalLeaves", "");
		leaves.comment = "Additional leaves block IDs, separate by ','";
		String additionalLeaves = leaves.value;

		Property drop_here = cfg.get(Configuration.CATEGORY_GENERAL,
				"drop_here", true);
		drop_here.comment = "Harvest items near by player";
		config.drop_here = drop_here.getBoolean(true);

		Property debug = cfg.get(Configuration.CATEGORY_GENERAL,
				"debug", true);
		drop_here.comment = "Harvest items near by player";
		Util.debug = drop_here.getBoolean(true);

		cfg.save();

		for (String token : additionalTools.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			config.tool.add(token.trim());
		}

		config.wood.add(Block.wood.getClass().getName());
		for (String token : additionalWoods.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			config.wood.add(token.trim());
		}

		config.leaves.add(Block.leaves.getClass().getName());
		for (String token : additionalLeaves.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			config.leaves.add(token.trim());
		}
	}

}
