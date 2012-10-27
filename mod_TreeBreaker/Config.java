package mod_TreeBreaker;

import java.util.LinkedHashSet;
import java.util.Set;

public class Config {

	enum Mode { none, woodonly, leavesonly, both};

	public Set<String> leaves = new LinkedHashSet();
	public Set<String> wood = new LinkedHashSet();
	public Set<String> tool = new LinkedHashSet();
	public Mode mode = Mode.both;
	boolean drop_here = true;

}
