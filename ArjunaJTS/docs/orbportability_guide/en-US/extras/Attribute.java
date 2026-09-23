package com.arjuna.orbportability.orb;
public abstract class Attribute {
	public abstract void initialise(String[] params);

	public boolean postORBInit();
};

package com.arjuna.orbportability.oa;
public abstract class OAAttribute {
	public abstract void initialise(String[] params);

	public boolean postOAInit();
};