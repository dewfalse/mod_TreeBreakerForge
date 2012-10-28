package mod_TreeBreaker;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.src.Block;
import net.minecraft.src.Item;

public class Config {
	public static String channel = "tbf";

	public enum Mode { none, woodonly, leavesonly, both};

	public Set<String> leaves = new LinkedHashSet();
	public Set<String> wood = new LinkedHashSet();
	public Set<String> tool = new LinkedHashSet();
	public Mode mode = Mode.both;
	public boolean drop_here = true;

	public Set<Class> getTools() {
		Set<Class> ret = new LinkedHashSet();
		for(String s : tool) {
			try {
				int i = Integer.parseInt(s);
				if(Item.itemsList[i] != null) {
					ret.add(Item.itemsList[i].getClass());
				}
				continue;
			} catch(NumberFormatException e) {
			}

			try {
				ret.add(Class.forName(s));
			} catch(ClassNotFoundException e) {
			}
		}
		return ret;
	}

	public void ToggleMode() {
		mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
	}

	void setLeaves(String additionalLeaves) {
		leaves.clear();
		leaves.add(Block.leaves.getClass().getName());
		for (String token : additionalLeaves.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			leaves.add(token.trim());
		}
	}

	void setWood(String additionalWoods) {
		wood.clear();
		wood.add(Block.wood.getClass().getName());
		for (String token : additionalWoods.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			wood.add(token.trim());
		}
	}

	void setTool(String additionalTools) {
		tool.clear();
		for (String token : additionalTools.split(",")) {
			if (token.trim().isEmpty()) {
				continue;
			}
			tool.add(token.trim());
		}
	}

	public Set<Integer> getLeaves() {
		Set<Integer> ret = new LinkedHashSet();
		switch(mode) {
		case none:
			return ret;
		case woodonly:
			return ret;
		default:
			break;
		}

		for(String s : leaves) {
			try {
				int i = Integer.parseInt(s);
				if(Block.blocksList[i] != null) {
					ret.add(i);
				}
				continue;
			} catch(NumberFormatException e) {
			}

			try {
				Class c = Class.forName(s);
				if(c == null) {
					continue;
				}
				for(Block b : Block.blocksList) {
					if(b == null) {
						continue;
					}
					if(b.getClass() == c) {
						ret.add(b.blockID);
					}
				}
			} catch(ClassNotFoundException e) {
			}
		}
		return ret;
	}

	public Set<Integer> getWood() {
		Set<Integer> ret = new LinkedHashSet();
		switch(mode) {
		case none:
			return ret;
		case leavesonly:
			return ret;
		default:
			break;
		}

		for(String s : wood) {
			try {
				int i = Integer.parseInt(s);
				if(Block.blocksList[i] != null) {
					ret.add(i);
				}
				continue;
			} catch(NumberFormatException e) {
			}

			try {
				Class c = Class.forName(s);
				if(c == null) {
					continue;
				}
				for(Block b : Block.blocksList) {
					if(b == null) {
						continue;
					}
					if(b.getClass() == c) {
						ret.add(b.blockID);
					}
				}
			} catch(ClassNotFoundException e) {
			}
		}
		return ret;
	}

}
