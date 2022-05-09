package net.obmc.OBWizardFun;

// Used to store meta data about entities that are part of spells so they
// can be tracked and managed as part of the spell
public class SpellEntity {

	private String name = null;
	private String entityuuid = null;
	private String targetuuid = null;
	private int lifespan = 0;
		
	public SpellEntity(String name, String uuid, String targetuuid, int lifespan) {
		this.name = name;
		this.entityuuid = uuid;
		this.targetuuid = targetuuid;
		this.lifespan = lifespan;
	}

	public String getName() {
		return this.name;
	}
	public String getUUID() {
		return this.entityuuid;
	}
	public String getTargetUUID() {
		return this.targetuuid;
	}
	public int getLifespan() {
		return this.lifespan;
	}
}
