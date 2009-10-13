package pipe.dataLayer;

import java.util.Collection;
import java.util.HashMap;

public class ConstantStore {
	private HashMap<String, Constant> constants = new HashMap<String, Constant>();

	public Collection<Constant> getConstants()
	{
		return constants.values();
	}
	
	public boolean updateConstant(String oldName, Constant constant)
	{
		if(isUpdateAllowed(oldName, constant)){
			if(constants.containsKey(oldName)){
				constants.put(oldName, constant);
				return true;
			}
		}
		return false;
	}

	private boolean isUpdateAllowed(String oldName, Constant constant) {
		return oldName.equals(constant.getName()) || !constants.containsKey(constant.getName());
	}


	public boolean addConstant(String name, int value) {	
		if(!constants.containsKey(name)){
			constants.put(name, new Constant(name, value));
			return true;
		}
		return false;
	}

	public boolean removeConstant(String name){
		if(!isConstantInUse(name)){
			if(constants.containsKey(name)){
				constants.remove(name);
				return true;
			}
		}
		return false;
	}


	private boolean isConstantInUse(String name) {
		return false;
	}
}
