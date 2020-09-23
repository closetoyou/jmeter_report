package org.test.ant.taskdefs.jmeter;

public final class Property {

	private String name = "";
	private String value = "";
	private boolean remote = false;

	public void setName(String arg) {
		name = arg.trim();
	}

	public String getName() {
		return name;
	}

	public void setValue(String arg) {
		value = arg.trim();
	}

	public String getValue() {
		return value;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public boolean isRemote() {
		return remote;
	}

	public boolean isValid() {
		return (!name.equals("") && !value.equals(""));
	}

	public String toString() {
		return name + "=" + value;
	}

}
