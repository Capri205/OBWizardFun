package net.obmc.OBWizardFun;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Bee;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wolf;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;


public class OBWizardFun extends JavaPlugin implements Listener
{
	
	Logger log = Logger.getLogger("Minecraft");

	public static OBWizardFun instance;
	
    public OBWizardFun() {
    	instance = this;
    }
    
	private static String plugin = "OBWizardFun";
	private static String pluginprefix = "[" + plugin + "]";
	private static String chatmsgprefix = ChatColor.AQUA + "" + ChatColor.BOLD + plugin + ChatColor.DARK_GRAY + ChatColor.BOLD + " » " + ChatColor.LIGHT_PURPLE + "";
	private static String logmsgprefix = pluginprefix + " » ";
	
	private EventListener listener;
	
	private java.util.Random rand = new java.util.Random();

	// spell types we support
	public static enum SpellType {
		FIRE, FIREWORK, EXPLOSION, LIGHTNING, SOAK, WEIRD, FROST, PEE, GEYSER, FIREBALL, SOUNDEFFECT, EVILWITCH, ANGRYBEES, RABIDWOLVES
	}
	
	// spell randomizer 
	private <T extends Enum<SpellType>> T randomSpell(Class<T> clazz){
        int x = rand.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
	// sound randomizer 
	private <T extends Enum<Sound>> T randomSound(Class<T> clazz){
        int x = rand.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

	// Evil witch names and randomizer
	private enum WitchName {
		Agatha, Agnes, Cecily, Cruella, Deirdra, Desdemona, Edith, Elspeth, Elvira, Endora, Esmerelda, Gertrude, Greta, Grimhild, Hag, Hestia, Hilda, Hildy, Hyacinth, Isla, Lucinda, Mabel,
		Matilda, Morgana, Muriel, Myrtle, Naga, Nora, Raven, Romilda, Scylla, Sybil, Zelda
	}
	private <T extends Enum<WitchName>> T randomWitchName(Class<T> clazz){
        int x = rand.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

	// entity trackers
	private HashMap<String, SpellEntity> evilwitchtracker = new HashMap<String, SpellEntity>();
	private HashMap<String, SpellEntity> angrybeetracker = new HashMap<String, SpellEntity>();
	private HashMap<String, SpellEntity> rabidwolftracker = new HashMap<String, SpellEntity>();
	
	// mappings of messages, sounds and particles to our spells
	private HashMap<SpellType, HashMap<String, String>> messagemap = new HashMap<SpellType,HashMap<String,String>>();
	private HashMap<SpellType, Sound> soundmap = new HashMap<SpellType, Sound>();
	private HashMap<SpellType, Particle> particlemap = new HashMap<SpellType, Particle>();
	
	private boolean doallplayers = false;
	private boolean domessage = false;
	private int taskid;
	private int evilwitchchecker;
	private int angrybeechecker;
	private int rabidwolfchecker;
	private long startdelay = 20L;				// wait in seconds before starting spell casting
	private long spellinterval = 30L;			// interval in seconds between random spells
	private long evilwitchcheckinterval = 5L;		// interval in seconds between checks on evil witches
	private long angrybeecheckinterval = 3L;		// interval in seconds between checks on angry bees
	private long rabidwolfcheckinterval = 5L;		// interval in seconds between checks on angry bees
	private boolean spelltrackerchecking = false;
	
    public void onEnable() {

    	log.log(Level.INFO, "[OBWizardFun] Plugin Version " + this.getDescription().getVersion() + " activated");
	
    	registerListeners();
    	
    	// setup messages
    	// TODO: move to a config file later
    	messagemap.put(SpellType.FIRE, new HashMap<String,String>());
    		messagemap.get(SpellType.FIRE).put("single", ChatColor.AQUA + "A wizard just set #PLAYER# on " + ChatColor.RED + "fire" + ChatColor.AQUA + "!");
    		messagemap.get(SpellType.FIRE).put("doall", ChatColor.AQUA + "A wizard just set everyone on " + ChatColor.RED + "fire" + ChatColor.AQUA + "!");
        messagemap.put(SpellType.FIREWORK, new HashMap<String,String>());
        	messagemap.get(SpellType.FIREWORK).put("single", ChatColor.AQUA + "A wizard just lit a firework under #PLAYER#!");
        	messagemap.get(SpellType.FIREWORK).put("doall", ChatColor.AQUA + "A wizard just let fireworks under everyone!");
  		messagemap.put(SpellType.EXPLOSION, new HashMap<String,String>());
       		messagemap.get(SpellType.EXPLOSION).put("single", ChatColor.AQUA + "A wizard just cast an explosion spell on #PLAYER#!");
       		messagemap.get(SpellType.EXPLOSION).put("doall", ChatColor.AQUA + "A wizard just blew everyone up!");
    	messagemap.put(SpellType.LIGHTNING, new HashMap<String,String>());
        	messagemap.get(SpellType.LIGHTNING).put("single", ChatColor.AQUA + "A wizard just cast a " + ChatColor.WHITE + "lightning" + ChatColor.AQUA + " spell on #PLAYER#!");
        	messagemap.get(SpellType.LIGHTNING).put("doall", ChatColor.AQUA + "A wizard just cast a " + ChatColor.WHITE + "lightning" + ChatColor.AQUA + " spell on everyone!");
        messagemap.put(SpellType.SOAK, new HashMap<String,String>());
        	messagemap.get(SpellType.SOAK).put("single", ChatColor.AQUA + "A wizard just soaked #PLAYER#!");
        	messagemap.get(SpellType.SOAK).put("doall", ChatColor.AQUA + "A wizard just soaked everyone!");
        messagemap.put(SpellType.WEIRD, new HashMap<String,String>());
        	messagemap.get(SpellType.WEIRD).put("single", ChatColor.AQUA + "A wizard cast a very weird spell on #PLAYER#!");
        	messagemap.get(SpellType.WEIRD).put("doall", ChatColor.AQUA + "A wizard cast a very weird spell on everyone!");
        messagemap.put(SpellType.FROST, new HashMap<String,String>());
        	messagemap.get(SpellType.FROST).put("single", ChatColor.AQUA + "A wizard cast some kind of frost spell on #PLAYER#!");
        	messagemap.get(SpellType.FROST).put("doall", ChatColor.AQUA + "A wizard cast some kind of frost spell on everyone!");
        messagemap.put(SpellType.PEE, new HashMap<String,String>());
        	messagemap.get(SpellType.PEE).put("single", ChatColor.AQUA + "A wizard caused #PLAYER# to " + ChatColor.YELLOW + "pee" + ChatColor.AQUA + " their pants!");
        	messagemap.get(SpellType.PEE).put("doall", ChatColor.AQUA + "A wizard caused everyone to " + ChatColor.YELLOW + "pee" + ChatColor.AQUA + " themselves!");
        messagemap.put(SpellType.GEYSER, new HashMap<String,String>());
        	messagemap.get(SpellType.GEYSER).put("single", ChatColor.AQUA + "A wizard cast a steam jet under " + "#PLAYER#! Run #PLAYER#, Run!");
        	messagemap.get(SpellType.GEYSER).put("doall", ChatColor.AQUA + "A wizard caused steam jets to form under everyone! Run!");
        messagemap.put(SpellType.FIREBALL, new HashMap<String,String>());
        	messagemap.get(SpellType.FIREBALL).put("single", ChatColor.AQUA + "A wizard launched a " + ChatColor.GOLD + "fireball" + ChatColor.AQUA + " at #PLAYER#!");
        	messagemap.get(SpellType.FIREBALL).put("doall", ChatColor.AQUA + "A wizard launched " + ChatColor.GOLD + "fireballs" + ChatColor.AQUA + " at everyone! Take cover!");        
        messagemap.put(SpellType.EVILWITCH, new HashMap<String,String>());
            messagemap.get(SpellType.EVILWITCH).put("single", ChatColor.AQUA + "A wizard sent an evil witch to destroy #PLAYER#!");
            messagemap.get(SpellType.EVILWITCH).put("doall", ChatColor.AQUA + "Evil witches are out to get everyone!");
        messagemap.put(SpellType.ANGRYBEES, new HashMap<String,String>());
            messagemap.get(SpellType.ANGRYBEES).put("single", ChatColor.AQUA + "A wizard released a swarm of angry bees on #PLAYER#!");
            messagemap.get(SpellType.ANGRYBEES).put("doall", ChatColor.AQUA + "Swarms of angry bees are on the loose! Run!");
        messagemap.put(SpellType.RABIDWOLVES, new HashMap<String,String>());
            messagemap.get(SpellType.RABIDWOLVES).put("single", ChatColor.AQUA + "A wizard released a pack of rabid wolves on #PLAYER#!");
            messagemap.get(SpellType.RABIDWOLVES).put("doall", ChatColor.AQUA + "A pack of rabid wolves are on the loose! Run!");
        	
        // setup sounds - some effects have their own sound
        soundmap.put(SpellType.FIRE, Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE);
        soundmap.put(SpellType.WEIRD, Sound.AMBIENT_CRIMSON_FOREST_MOOD);
        soundmap.put(SpellType.FROST, Sound.AMBIENT_NETHER_WASTES_MOOD);
        soundmap.put(SpellType.PEE, Sound.ENTITY_PLAYER_HURT_ON_FIRE);
        soundmap.put(SpellType.SOAK, Sound.ENTITY_PLAYER_SPLASH);
        soundmap.put(SpellType.GEYSER, Sound.BLOCK_BUBBLE_COLUMN_UPWARDS_INSIDE);
        soundmap.put(SpellType.FIREBALL, Sound.ENTITY_ENDER_DRAGON_SHOOT);
        soundmap.put(SpellType.EVILWITCH, Sound.ENTITY_WITCH_CELEBRATE);
        soundmap.put(SpellType.ANGRYBEES, Sound.ENTITY_BEE_LOOP_AGGRESSIVE);
        soundmap.put(SpellType.RABIDWOLVES, Sound.ENTITY_WOLF_HOWL);
        	
        // setup particle map - some effects do not require a particle
        particlemap.put(SpellType.SOAK, Particle.WATER_DROP);
        particlemap.put(SpellType.FROST, Particle.REDSTONE);
        particlemap.put(SpellType.PEE, Particle.DRIPPING_HONEY);
        particlemap.put(SpellType.GEYSER, Particle.CLOUD);

        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        
        // entity based spell checkers - clears entities that have despawned, need despawning or need retargetting
        evilwitchchecker = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
        	@Override
        	public void run() {
        		spellEntityChecker(evilwitchtracker, EntityType.WITCH, "Evil Witch");
        	}
        }, startdelay*22, evilwitchcheckinterval*20);
        
        angrybeechecker = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
        	@Override
        	public void run() {
        		spellEntityChecker(angrybeetracker, EntityType.BEE, "Angry Bee");
        	}
        }, startdelay*20, angrybeecheckinterval*20);

        rabidwolfchecker = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
        	@Override
        	public void run() {
        		spellEntityChecker(rabidwolftracker, EntityType.WOLF, "Rabid Wolf");
        	}
        }, startdelay*20, rabidwolfcheckinterval*20);
    
        
        // enable the main task
		taskid = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
        		
            	int onlineplayercount = Bukkit.getOnlinePlayers().size();
            	if ( onlineplayercount > 0) {

            		// choose random spell
            		SpellType spell = randomSpell(SpellType.class);
            		
            		// 10% chance spell hits all players, 90% of time one random player
           			doallplayers = rand.nextDouble() < 0.1 && onlineplayercount > 1 ? true : false;
            	
            		// 20% chance of a message to all players informing of spell being cast
           			domessage = rand.nextDouble() < 0.2 ? true : false;
            	
            		// perform random spell
            		castSpell(spell, doallplayers, domessage, null, null);
            	}
            } 
        }, startdelay*20, spellinterval*20);
    }

    // register any listeners
	private void registerListeners() {
        this.listener = new EventListener();
        this.getServer().getPluginManager().registerEvents((Listener)this.listener, (Plugin)this);
        this.getCommand("cast").setExecutor(new CommandListener());

	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(evilwitchchecker);
		Bukkit.getScheduler().cancelTask(angrybeechecker);
		Bukkit.getScheduler().cancelTask(rabidwolfchecker);
		Bukkit.getScheduler().cancelTask(taskid);
		Bukkit.getLogger().info("[OBFireworksOnJoin] Plugin unloaded");
	}

	// return this instance
    public static OBWizardFun getInstance() {
    	return instance;
    }	
	// consistent messaging
	public static String getPluginName() {
		return plugin;
	}
	public static String getPluginPrefix() {
		return pluginprefix;
	}
	public String getChatMsgPrefix() {
		return chatmsgprefix;
	}
	public String getLogMsgPrefix() {
		return logmsgprefix;
	}

	// check a spell is valid
	public boolean isSpell(String chkspell) {
		SpellType spell = null;
		try {
			spell = SpellType.valueOf(chkspell);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public SpellType getSpell(String spell) {

		if (isSpell(spell)) {
			return SpellType.valueOf(spell);
		}
		return null;
	}

	// checker for entity based spells - cross check world entities against tracker and
	// tracker entries against world entities, and re-target entities if necessary
	void spellEntityChecker(HashMap<String, SpellEntity> spelltracker, EntityType type, String mobname) {
		if (spelltracker.size() > 0) {
			spelltrackerchecking = true;
			Iterator<Entity> eit = Bukkit.getWorld("world").getEntities().iterator();
			while (eit.hasNext()) {
				Entity entity = eit.next();
				String entityname = "";
				if ( entity.getCustomName() != null) {
					entityname = ChatColor.stripColor(entity.getCustomName());
				}
				if (entity.getType().equals(type) && entityname.contains(mobname)) {
					String entityuuid = entity.getUniqueId().toString();
					SpellEntity spellentity = null;
					spellentity = spelltracker.get(entityuuid);
					if (spellentity != null) {
						Player target = Bukkit.getPlayer(UUID.fromString(spellentity.getTargetUUID()));
						if (target == null || target.isDead() || target.isInWater() ||
								target.getGameMode().equals(GameMode.CREATIVE) || target.getGameMode().equals(GameMode.SPECTATOR) ||
								target.getLocation().getY() > 200) {
							// player no longer in world so despawn entities
							entity.playEffect(EntityEffect.ENTITY_POOF);
							entity.remove();
							spelltracker.remove(entityuuid);
						} else {
							if (entity.getTicksLived() > spellentity.getLifespan()) {
								// entity reached end of live so despawn
								entity.playEffect(EntityEffect.ENTITY_POOF);
								entity.remove();								
								spelltracker.remove(entityuuid);
							} else {
								Mob mob = (Mob) entity;
								// move mob to target player if player has moved away
								int distance = 7;
								switch (entity.getType()) {
								case WITCH:
									distance = 10;
									break;
								case WOLF:
									distance = 15;
									break;
								default:
									distance = 7;
									break;
								}
								if (target.getLocation().distance(entity.getLocation()) > distance) {
									entity.teleport(target);
								}
								if (mob.getTarget() == null) {
									// re-target mob onto target player
									switch (type) {
									case BEE:
										Bee angrybee = (Bee) mob;
										angrybee.setHasStung(false);
										angrybee.setTarget(target);
										angrybee.setAnger(500);
										angrybee.attack(target);
										target.playSound(target.getLocation(), soundmap.get(SpellType.ANGRYBEES), 1.0f, 1.0f);
										break;
									case WITCH:
										Witch evilwitch = (Witch) mob;
										evilwitch.setTarget(target);
										evilwitch.attack(target);
										target.playSound(target.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.0f, 1.0f);
										break;
									case WOLF:
										Wolf rabidwolf = (Wolf) mob;
										rabidwolf.setTarget(target);
										rabidwolf.attack(target);
										double growlorhowl = rand.nextDouble();
										if (growlorhowl < 0.5) {
											target.playSound(target.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
										} else {
											target.playSound(target.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.0f, 1.0f);
										}
										break;
									default:
										break;
									}
								} else {
									// no need to re target, but do something anyway - play sounds perhpas?
									switch (type) {
									case BEE:
										double buzzer = rand.nextDouble();
										if (buzzer < 0.3) {
											target.playSound(target.getLocation(), Sound.ENTITY_BEE_LOOP, 1.0f, 1.0f);
										} else if (buzzer < 0.6) {
											target.playSound(target.getLocation(), Sound.ENTITY_BEE_LOOP_AGGRESSIVE, 1.0f, 1.0f);
										} else {
											target.playSound(target.getLocation(), Sound.ENTITY_BEE_STING, 1.0f, 1.0f);
										}
										target.playSound(target.getLocation(), soundmap.get(SpellType.ANGRYBEES), 1.0f, 1.0f);
										break;
									case WOLF:
										double growlorhowl = rand.nextDouble();
										if (growlorhowl < 0.5) {
											target.playSound(target.getLocation(), Sound.ENTITY_WOLF_GROWL, 1.0f, 1.0f);
										} else {
											target.playSound(target.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.0f, 1.0f);
										}
										break;
									default:
										break;
									}
								}
							}
						}
					} else {
						// tracker entry gone so remove entity
						entity.remove();
					}
				}
			}
			// cross check tracker gainst world entities
			ArrayList<String> worldentities = new ArrayList<String>();
			Iterator<Entity> weit = Bukkit.getWorld("world").getEntities().iterator();
			while (weit.hasNext()) {
				Entity entity = weit.next();
				if (entity.getType().equals(type)) {
					worldentities.add(entity.getUniqueId().toString());
				}
			}
			Iterator<String> stit = spelltracker.keySet().iterator();
			while(stit.hasNext()) {
				String trackeruuid = stit.next();
				if (!worldentities.contains(trackeruuid)) {
					// remove tracker entry as world entity has gone
					stit.remove();
				}
			}
			// check tracker vs world entities
			spelltrackerchecking = false;
		}
	}
	
	// return a random location around a player within a range
	Location getRandomLocation(Location loc, int rangelow, int rangehigh) {
		boolean goodspot = false;
		int tries = 0;
		while (!goodspot && tries < 5) {
			Material[] blockmatch = new Material[3];
			int offsetx = 0;
			int offsetz = 0;
			while (offsetx == 0 && offsetz == 0) {
				offsetx = (int) (Math.random() * ((rangehigh-rangelow)+1)+rangelow);
				offsetx = (int) (rand.nextDouble() < 0.5 ? offsetx-(offsetx*2) : offsetx);
				offsetz = (int) (Math.random() * ((rangehigh-rangelow)+1)+rangelow);
				offsetz = (int) (rand.nextDouble() < 0.5 ? offsetz-(offsetz*2) : offsetz);
			}

			Location tryspot = loc.clone();	tryspot.add(offsetx, 0, offsetz); tryspot.setY(tryspot.getWorld().getHighestBlockAt(tryspot).getY()+1); Block blockatspot = tryspot.getBlock(); 
			Location underspot = tryspot.clone(); underspot.add(0, -1, 0); Block blockunderspot = underspot.getBlock();
			Location overspot = tryspot.clone(); overspot.add(0, 1, 0);  Block blockoverspot = overspot.getBlock();
			blockmatch[1] = blockatspot.getType(); blockmatch[0] = blockunderspot.getType(); blockmatch[2] = blockoverspot.getType();
			if (loc.distance(tryspot) < 10 ) {
				boolean blocksetmatch = blockmatch[0] != Material.AIR && blockmatch[0] != Material.WATER && blockmatch[0] != Material.LAVA && blockmatch[1] == Material.AIR && blockmatch[2] == Material.AIR;
				while (!blocksetmatch && loc.distance(tryspot) < 10) {
					tryspot.add(0, 1, 0); blockatspot = tryspot.getBlock(); blockmatch[1] = blockatspot.getType();
					underspot.add(0, 1, 0); blockunderspot = underspot.getBlock(); blockmatch[0] = blockunderspot.getType();
					overspot.add(0, 1, 0);  blockoverspot = overspot.getBlock(); blockmatch[2] = blockoverspot.getType();
					blocksetmatch = blockmatch[0] != Material.AIR && blockmatch[0] != Material.WATER && blockmatch[0] != Material.LAVA && blockmatch[1] == Material.AIR && blockmatch[2] == Material.AIR;
				}
				if (blocksetmatch) {
					return tryspot;
				}
			}
			tries++;
		}
		return loc;
	}
	
	// cast a random spell
	void castSpell(SpellType spelltype, boolean doallplayers, boolean domessage, Player caster, Player specificplayer) {

		// get all online players into a list
		boolean docaster = false;
		ArrayList<Player> eligibleplayers = new ArrayList<Player>();
		if (specificplayer != null && caster != null) {
			eligibleplayers.add(specificplayer);
			docaster = true;
			doallplayers = false;
		} else {
			eligibleplayers.addAll(Bukkit.getOnlinePlayers());
		}

		// process any exclusions
		Iterator<Player> pit = eligibleplayers.iterator();
		while(pit.hasNext()) {
			Player player = pit.next();
			if (player.isDead() || player.isInWater() || player.getGameMode().equals(GameMode.CREATIVE) || player.getGameMode().equals(GameMode.SPECTATOR) || player.getLocation().getY() > 200) {
				if (docaster) {
					caster.sendMessage(chatmsgprefix + ChatColor.RED + "Cannot cast a spell on " + ChatColor.WHITE + player.getName() + ChatColor.RED + " right now");
				}
				pit.remove();
			}
		}
		if (eligibleplayers.size() < 1) {
			return;			
		}
		
		// do all players or a random player into our list of online players
		ArrayList<Player> finallist = new ArrayList<Player>();
		if (!doallplayers && eligibleplayers.size() > 1) {
			finallist.add(eligibleplayers.stream().skip((int) (eligibleplayers.size() * Math.random())).findFirst().orElse(null));
		} else {
			finallist.addAll(eligibleplayers);
		}
		eligibleplayers.clear();

		// output message
		if (domessage && !spelltype.equals(SpellType.SOUNDEFFECT)) {
			String message = doallplayers ? messagemap.get(spelltype).get("doall") : messagemap.get(spelltype).get("single").replace("#PLAYER#", finallist.get(0).getName());
			if (docaster) {
				message = message.replace("A wizard", caster.getName());
			}
			for (Player messageplayer : Bukkit.getOnlinePlayers()) {
				messageplayer.sendMessage(message);
			}
		}
		
		// cast spell on eligible players
		for (Player player : finallist) {
			
			// spell sound
			if (soundmap.containsKey(spelltype)) {
				player.playSound(player.getLocation(), soundmap.get(spelltype), 1.0f, 1.0f);
			}

			// play spell
    		switch(spelltype) {
    		case FIRE:
    			castFireSpell(player);
    			break;
    		case FIREWORK:
    			castFireworkSpell(player);
    			break;
    		case EXPLOSION:
    			castExplosionSpell(player);
    			break;
    		case LIGHTNING:
    			castLightningSpell(player);
    			break;
    		case SOAK:
    			castSoakSpell(player);
    			break;
    		case WEIRD:
    			castWeirdSpell(player);
    			break;
    		case FROST:
    			castFrostSpell(player);
    			break;
    		case PEE:
    			castPeeSpell(player);
    			break;
    		case GEYSER:
    			castGeyserSpell(player);
    			break;
    		case FIREBALL:
    			castFireballSpell(player);
    			break;
    		case SOUNDEFFECT:
    			playSoundEffect(player);
    			break;
    		case EVILWITCH:
    			spawnEvilWitch(player);
    			break;
    		case ANGRYBEES:
    			spawnAngryBees(player);
    			break;
    		case RABIDWOLVES:
    			spawnRabidWolves(player);
    			break;
    		}
    	}
    }
	
	// pack of rabid wolves
	void spawnRabidWolves(Player player) {
		if (rabidwolftracker.size() < 15 && !spelltrackerchecking) {
			int packsize = (int) (Math.random() * ((5-3)+1)+3);
			for (int i = 0; i < packsize; i++) {
				Location wolfspawn = getRandomLocation(player.getLocation(), 3, 8);
				LivingEntity rabidwolfbase = (LivingEntity) player.getWorld().spawnEntity(wolfspawn, EntityType.WOLF);
				Wolf rabidwolf = (Wolf) rabidwolfbase;
				rabidwolf.setCustomName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Rabid" + ChatColor.RESET + " " + ChatColor.WHITE + "Wolf");
				rabidwolf.setTarget(player);
				rabidwolf.attack(player);
				rabidwolftracker.put(rabidwolf.getUniqueId().toString(),
					new SpellEntity(rabidwolf.getCustomName(),
							rabidwolf.getUniqueId().toString(),
							player.getUniqueId().toString(),
					(int) (Math.random() * (30 - 15 + 1 ) + 15)*20)
				);
			}
		}
	}

	// swarm of angry bees
	void spawnAngryBees(Player player) {
		if (angrybeetracker.size() < 15 && !spelltrackerchecking) {
			int swarmsize = (int) (Math.random() * (10-5+1)+5);
			for (int i = 0; i < swarmsize; i++) {
				Location beespawn = getRandomLocation(player.getLocation(), 2, 5);
				LivingEntity angrybeebase = (LivingEntity) player.getWorld().spawnEntity(beespawn, EntityType.BEE);
				Bee angrybee = (Bee) angrybeebase;
				angrybee.setCustomName(ChatColor.RED + "Angry" + " " + ChatColor.GOLD + "Bee");
				angrybee.setAnger(500);
				angrybee.setTarget(player);
				angrybee.attack(player);
				angrybeetracker.put(angrybee.getUniqueId().toString(),
					new SpellEntity(angrybee.getCustomName(),
							angrybee.getUniqueId().toString(),
							player.getUniqueId().toString(),
					(int) (Math.random() * (30 - 15 + 1 ) + 15)*20)
				);
			}
		}
	}

	// random sound effect
	void playSoundEffect(Player player) {
		Sound randomsound = randomSound(Sound.class);
		if ( randomsound.toString().startsWith("MUSIC_DISC")) {
			player.sendMessage(ChatColor.AQUA + "Oh no! A wizard just put on his favioute track! Cover your ears!");
		}
		player.playSound(player.getLocation(), randomsound, 1.0f, 1.0f);
	}
	
	// spawn evil witch if not maxed out
	void spawnEvilWitch(Player player) {
		if (evilwitchtracker.size() < 3 && !spelltrackerchecking) {
			Location loc = getRandomLocation(player.getLocation(), 5, 10);
			Witch evilwitch = (Witch) player.getWorld().spawnEntity(loc, EntityType.WITCH);
			evilwitch.setCustomName("Evil Witch " + randomWitchName(WitchName.class));
			evilwitch.setCustomNameVisible(true);
			evilwitch.setTarget(player);
			Double onfire = rand.nextDouble();
			if (onfire < 0.1) {
				evilwitch.setVisualFire(true);
				evilwitch.setCustomName(ChatColor.RED + "Flaming " + ChatColor.WHITE + evilwitch.getCustomName());
			} else {
				evilwitch.setVisualFire(false);
			}
			evilwitchtracker.put(evilwitch.getUniqueId().toString(),
				new SpellEntity(evilwitch.getCustomName(),
					evilwitch.getUniqueId().toString(),
					player.getUniqueId().toString(),
					(int) (Math.random() * (60 - 30 + 1 ) + 30)*20)
			);
		}
	}

	// fire spell
	void castFireSpell(Player player) {
		player.setFireTicks(100);
	}

	// firework spell
	void castFireworkSpell(Player player) {
		Location loc = player.getLocation();
		final Firework f = (Firework)loc.getWorld().spawn(loc, (Class)Firework.class);
		final FireworkMeta fm = f.getFireworkMeta();
 		fm.addEffect(buildFirework());
		fm.setPower(rand.nextInt(6));
		f.setFireworkMeta(fm);
	}
	FireworkEffect buildFirework() {
		double r = rand.nextDouble(); boolean doflicker = r < 0.5 ? true : false;
		r = rand.nextDouble(); boolean dotrail = r < 0.5 ? true : false;
		int type = new Random().nextInt(FireworkEffect.Type.values().length);
		int color = (int) Math.floor(Math.random()*16777215);
		int fade = (int) Math.floor(Math.random()*16777215);
		return FireworkEffect.builder()
				.flicker(doflicker).trail(dotrail)
				.with(FireworkEffect.Type.values()[type])
				.withColor(Color.fromRGB(color))
				.withFade(Color.fromRGB(fade))
				.build();
	}

	// explosion spell
	void castExplosionSpell(Player player) {
		Location loc = player.getLocation();
		loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 20.0F, false, false);
		// throw player a random direction
		Vector vec = new Vector((rand.nextDouble()*2.0)-1.0, rand.nextDouble()*2.0, (rand.nextDouble()*2.0)-1.0);
		player.setVelocity(vec.multiply(2));
	}

	// lightning spell
	void castLightningSpell(Player player ) {
		player.getWorld().strikeLightningEffect(player.getLocation());
	}

	// soaking spell
	void castSoakSpell(Player player) {
		Location loc = player.getLocation();
		new BukkitRunnable() {
			double phi = 0;
			public void run() {
				phi += Math.PI/10;
				for (double theta = 0; theta < 2*Math.PI; theta += Math.PI/40) {
					double r = 1.5;
					double x = r*Math.cos(theta)*Math.sin(phi);
					double y = r*Math.cos(phi) + 1.5;
					double z = r*Math.sin(theta)*Math.sin(phi);
					loc.add(x, y, z);
					loc.getWorld().spawnParticle(particlemap.get(SpellType.SOAK), loc, 1);
					loc.subtract(x, y, z);
				}
				if (phi > Math.PI*8) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// weird spell - override particle for different behaviours
	void castWeirdSpell(Player player){
		double randomnum = rand.nextDouble();

		Particle randomparticle = null;
		if (randomnum <= 0.1) {
			randomparticle = Particle.WARPED_SPORE;
		} else if (randomnum > 0.1 && randomnum < 0.2) {
			randomparticle = Particle.CRIMSON_SPORE;
		} else if (randomnum > 0.2 && randomnum < 0.3) {
			randomparticle = Particle.LAVA;
		} else if (randomnum > 0.3 && randomnum < 0.4) {
			randomparticle = Particle.CAMPFIRE_COSY_SMOKE;
		} else if (randomnum > 0.4 && randomnum < 0.5) {
			randomparticle = Particle.REVERSE_PORTAL;
		} else if (randomnum > 0.5 && randomnum < 0.6) {
			randomparticle = Particle.NAUTILUS;
		} else if (randomnum > 0.6 && randomnum < 0.7) {
			randomparticle = Particle.SOUL;
		} else if (randomnum > 0.7 && randomnum < 0.8) {
			randomparticle = Particle.DRAGON_BREATH;
		} else if (randomnum > 0.8 && randomnum < 0.9) {
			randomparticle = Particle.COMPOSTER;
		} else if (randomnum > 0.9 && randomnum <= 1.0) {
			randomparticle = Particle.DOLPHIN;
		} else {
			randomparticle = particlemap.get(SpellType.WEIRD);
		}

		final Particle spellparticle = randomparticle;
		Location loc = player.getLocation();
		new BukkitRunnable(){
			double phi = 0;
			public void run(){
				phi = phi + Math.PI/8;					
				double x, y, z;			
 				for (double t = 0; t <= 2*Math.PI; t = t + Math.PI/16){
					for (double i = 0; i <= 1; i = i + 1){
						x = 0.4*(2*Math.PI-t)*0.5*Math.cos(t + phi + i*Math.PI);
						y = 0.5*t;
						z = 0.4*(2*Math.PI-t)*0.5*Math.sin(t + phi + i*Math.PI);
						loc.add(x, y, z);
						loc.getWorld().spawnParticle(spellparticle, loc, 1);
						loc.subtract(x,y,z);
					}
 				}		
 				if (phi > 10 * Math.PI){						
					this.cancel();
				}
			}	
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 3);
	}
	
	// frost spell
	void castFrostSpell(Player player) {
		new BukkitRunnable(){
			double t = 0;
			double x, y, z = 0;
			Particle.DustOptions snow = new Particle.DustOptions(Color.WHITE, 0.5f);
			public void run() {
				t +=  Math.PI / 8;
				Location loc = player.getLocation();
 				for (double phi = 0; phi <= 2 * Math.PI; phi += Math.PI / 2) {
					x = 0.15 * (4 * Math.PI - t) * Math.cos(t + phi);
					y = 0.2 * t;
					z = 0.15 * (4 * Math.PI - t) * Math.sin(t + phi);
					loc.add(x, y, z);
					loc.getWorld().spawnParticle(particlemap.get(SpellType.FROST), loc, 10, 0.2, 0.0, 0.2, snow);
					loc.subtract(x,y,z);
 				}		
 				if (t >= Math.PI * 4) {
 					loc.add(x, y, z);
 					loc.getWorld().playEffect(loc, Effect.INSTANT_POTION_BREAK, Color.WHITE);
 					loc.getWorld().playEffect(player.getLocation(), Effect.GHAST_SHRIEK, null);
					this.cancel();
				}

			}	
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// pee spell
	void castPeeSpell(Player player) {
		//TODO: unnecessarily complicated math for this effect - rework as a proper projectile stream (facing direction) or a simple pool like this ultimately does
		new BukkitRunnable() {
			double phi = 0;
			public void run() {
				phi += Math.PI/5;
				for (double theta = 0; theta < 2 * Math.PI; theta += Math.PI / 2.5) {
					double r = 1.5;
					double x = r*Math.cos(theta)*Math.sin(phi);
					double y = r*Math.cos(phi) + 1.5;
					double z = r*Math.sin(theta)*Math.sin(phi);
					player.getLocation().add(x, y, z);
					player.getLocation().getWorld().spawnParticle(particlemap.get(SpellType.PEE), player.getLocation(), 1, 0.2, 0, 0.2);
					player.getLocation().subtract(x, y, z);
				}
				if (phi > Math.PI * 4) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// geyser spell
	void castGeyserSpell(Player player) {
		float effectlife = 5000f;
		Location baseloc = player.getLocation();
		new BukkitRunnable() {
			long startTime = System.currentTimeMillis();
			long elapsedTime = 0L;
			float life = (effectlife*0.25f) + rand.nextFloat() + ((effectlife * 0.75f) + (effectlife * 0.25f));
			float decay = 50f;
			Location loc = baseloc.clone();
			public void run() {
				loc.getWorld().spawnParticle(Particle.CLOUD, loc, 0, 0, 0.5, 0);
				life -= decay;
				if (life <= 0) {
					loc = baseloc.clone();
					life = (effectlife*0.25f) + rand.nextFloat() + ((effectlife * 0.75f) + (effectlife * 0.25f));
				}
				elapsedTime = (new Date()).getTime() - startTime;
				if (elapsedTime > effectlife) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// fireball attack spell
	void castFireballSpell(Player player) {
		int multiplier = rand.nextDouble() <= 0.1 ? 2+rand.nextInt(2) : 1;
		for (int i = 0; i < multiplier; i++ ) {
				launchFireball(player);
		}
	}
	void launchFireball(Player player) {
		// get random point around player some distance away and up in the air
		Location rloc = null;
		Location ploc = null;
		int tries = 0;
		do {
			double theta = 360 * rand.nextDouble();
			double dist = rand.nextDouble()*(80-65)+65;
			double xrand = dist * Math.cos(theta);
			double zrand = dist * Math.sin(theta);
			int yrand = 45+(55+rand.nextInt(70-55));	// water level is 45 in our lobby
			if (yrand > 255) { yrand = 255; }			// keep to world height

			// get a fireball launch location based on player position and the random point
			ploc = player.getLocation();
			rloc = player.getLocation().add(xrand,yrand,zrand);
			tries++;
			
		} while (!rloc.getBlock().getType().name().equals("AIR") && tries > 5);
		if (tries > 5) {
			return;
		}
		// launch a fireball at the player
		Vector from = new Vector(rloc.getX(), rloc.getY(), rloc.getZ());
		Vector to = new Vector(ploc.getX(), ploc.getY(), ploc.getZ());
		Vector trajectory = to.subtract(from).normalize();
		// 6% chance of dragon fireball, and equal 47% chance of large or regular fireball
		Fireball fireball = null;
		int prob = rand.nextInt(100);
		if (prob <= 5) {
			fireball = rloc.getWorld().spawn(rloc, DragonFireball.class);
		} else if (prob > 5 && prob <= 52) {
			fireball = rloc.getWorld().spawn(rloc, LargeFireball.class);
		} else {
			fireball = rloc.getWorld().spawn(rloc, Fireball.class);
		}
		fireball.setDirection(trajectory);
		fireball.setIsIncendiary(rand.nextDouble() < 0.5 ? true : false);
		fireball.setGravity(false);
	}
}
