package net.obmc.OBWizardFun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class OBWizardFun extends JavaPlugin implements Listener
{
	
	Logger log = Logger.getLogger("Minecraft");

	static java.util.Random rand = new java.util.Random();
	
	// spell types we support
	enum EventType {
		FIRE, EXPLOSION, LIGHTNING, FIREWORK, CONE, FROSTLORD, PEE, DRENCH, FIREBALL
	}
	// spell randomizer 
	public static <T extends Enum<EventType>> T randomEnum(Class<T> clazz){
        int x = rand.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
	// mappings of messages, sounds and particles to our spells
	HashMap<EventType, HashMap<String, String>> messagemap = new HashMap<EventType,HashMap<String,String>>();
	HashMap<EventType, Sound> soundmap = new HashMap<EventType, Sound>();
	HashMap<EventType, Particle> particlemap = new HashMap<EventType, Particle>();
	
	private boolean doallplayers = false;
	private boolean domessage = false;
	private int taskid;
	private long startdelay = 20L;			// wait in seconds before starting spell casting
	private long spellinterval = 30L;		// interval in seconds between random spells
	
    public void onEnable() {

    	log.log(Level.INFO, "[OBWizardFun] Plugin Version " + this.getDescription().getVersion() + " activated");
	
    	// setup messages
    	// TODO: move to a config file later
    	messagemap.put(EventType.FIRE, new HashMap<String,String>());
    		messagemap.get(EventType.FIRE).put("single", ChatColor.AQUA + "A wizard just set #PLAYER# on " + ChatColor.RED + "fire" + ChatColor.AQUA + "!");
    		messagemap.get(EventType.FIRE).put("doall", ChatColor.AQUA + "A wizard just set everyone on " + ChatColor.RED + "fire" + ChatColor.AQUA + "!");
       	messagemap.put(EventType.EXPLOSION, new HashMap<String,String>());
       		messagemap.get(EventType.EXPLOSION).put("single", ChatColor.AQUA + "A wizard just cast an explosion spell on #PLAYER#!");
       		messagemap.get(EventType.EXPLOSION).put("doall", ChatColor.AQUA + "A wizard just blew everyone up!");
    	messagemap.put(EventType.LIGHTNING, new HashMap<String,String>());
        	messagemap.get(EventType.LIGHTNING).put("single", ChatColor.AQUA + "A wizard just cast a " + ChatColor.WHITE + "lightning" + ChatColor.AQUA + " spell on #PLAYER#!");
        	messagemap.get(EventType.LIGHTNING).put("doall", ChatColor.AQUA + "A wizard just cast a " + ChatColor.WHITE + "lightning" + ChatColor.AQUA + " spell on everyone!");
        messagemap.put(EventType.FIREWORK, new HashMap<String,String>());
        	messagemap.get(EventType.FIREWORK).put("single", ChatColor.AQUA + "A wizard just lit a firework under #PLAYER#!");
        	messagemap.get(EventType.FIREWORK).put("doall", ChatColor.AQUA + "A wizard just let fireworks under everyone!");
        messagemap.put(EventType.CONE, new HashMap<String,String>());
        	messagemap.get(EventType.CONE).put("single", ChatColor.AQUA + "A wizard cast a weird spell on #PLAYER#!");
        	messagemap.get(EventType.CONE).put("doall", ChatColor.AQUA + "A wizard cast a very weird spell on everyone!");
        messagemap.put(EventType.FROSTLORD, new HashMap<String,String>());
        	messagemap.get(EventType.FROSTLORD).put("single", ChatColor.AQUA + "A wizard cast a frost lord spell on #PLAYER#!");
        	messagemap.get(EventType.FROSTLORD).put("doall", ChatColor.AQUA + "A wizard cast the frost lord spell on everyone!");
        messagemap.put(EventType.PEE, new HashMap<String,String>());
        	messagemap.get(EventType.PEE).put("single", ChatColor.AQUA + "A wizard caused #PLAYER# to " + ChatColor.YELLOW + "pee" + ChatColor.AQUA + " their pants!");
        	messagemap.get(EventType.PEE).put("doall", ChatColor.AQUA + "A wizard caused everyone to " + ChatColor.YELLOW + "pee" + ChatColor.AQUA + " themselves!");
        messagemap.put(EventType.DRENCH, new HashMap<String,String>());
        	messagemap.get(EventType.DRENCH).put("single", ChatColor.AQUA + "A wizard just drenched #PLAYER#!");
        	messagemap.get(EventType.DRENCH).put("doall", ChatColor.AQUA + "A wizard just drenched everyone!");
        messagemap.put(EventType.FIREBALL, new HashMap<String,String>());
        	messagemap.get(EventType.FIREBALL).put("single", ChatColor.AQUA + "A wizard launched a " + ChatColor.GOLD + "fireball" + ChatColor.AQUA + " at #PLAYER#!");
        	messagemap.get(EventType.FIREBALL).put("doall", ChatColor.AQUA + "A wizard launched " + ChatColor.GOLD + "fireballs" + ChatColor.AQUA + " at everyone! Take cover!");        

        // setup sounds - some effects have their own sound
        soundmap.put(EventType.FIRE, Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE);
        soundmap.put(EventType.CONE, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD);
        soundmap.put(EventType.FROSTLORD, Sound.AMBIENT_NETHER_WASTES_MOOD);
        soundmap.put(EventType.PEE, Sound.ENTITY_PLAYER_HURT_ON_FIRE);
        soundmap.put(EventType.DRENCH, Sound.ENTITY_PLAYER_SPLASH);
        soundmap.put(EventType.FIREBALL, Sound.ENTITY_ENDER_DRAGON_SHOOT);
        	
        // setup particle map - some effects do not require a particle
        particlemap.put(EventType.CONE, Particle.ASH);
        particlemap.put(EventType.FROSTLORD, Particle.REDSTONE);
        particlemap.put(EventType.PEE, Particle.DRIPPING_HONEY);
        particlemap.put(EventType.DRENCH, Particle.WATER_DROP);

        // enable the main task
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskid = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
        		
            	int onlineplayercount = Bukkit.getOnlinePlayers().size();
            	if ( onlineplayercount > 0) {

            		// 10% chance event hits all players, 90% of time one random player
            		doallplayers = rand.nextDouble() < 0.1 && onlineplayercount > 1 ? true : false;
            	
            		// 20% chance of a message to all players informing of event
            		domessage = rand.nextDouble() < 0.2 ? true : false;
            	
            		// process random event
            		castSpell(randomEnum(EventType.class), doallplayers, domessage);
            	}
            } 
        }, startdelay*20, spellinterval*20);
    }

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(taskid);
		Bukkit.getLogger().info("[OBFireworksOnJoin] Plugin unloaded");
	}

	// cast a random spell
	void castSpell(EventType eventtype, boolean doallplayers, boolean domessage) {

		// get all players or a random player into our list of online players
		List<Player> allplayers = (List<Player>) Bukkit.getOnlinePlayers();
		ArrayList<Player> playerlist = new ArrayList<Player>();
		if (doallplayers) {
			playerlist.addAll(allplayers);
		} else {
			playerlist.add(allplayers.stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null));
		}

		// cast spell on players
		String message = "";
		for (Player player : playerlist) {
			
			// output message
			if (domessage) {
				message = doallplayers ? messagemap.get(eventtype).get("doall") : messagemap.get(eventtype).get("single").replace("#PLAYER#", player.getName());
				for (Player messageplayer : Bukkit.getOnlinePlayers()) {
					messageplayer.sendMessage(message);
				}
			}
			
			// sound effect
			if (soundmap.containsKey(eventtype)) {
				player.playSound(player.getLocation(), soundmap.get(eventtype), 2.0f, 1.0f);
			}

    		switch(eventtype) {
    		case FIRE:
    			doFireEffect(player);
    			break;
    		case EXPLOSION:
    			doExplosion(player);
    			break;
    		case LIGHTNING:
    			doLightning(player);
    			break;
    		case FIREWORK:
    			doFirework(player);
    			break;
    		case CONE:
    			doConeEffect(player);
    			break;
    		case FROSTLORD:
    			doFrostLordEffect(player);
    			break;
    		case PEE:
    			doPeeEffect(player);
    			break;
    		case DRENCH:
    			doDrenchEffect(player);
    			break;
    		case FIREBALL:
    			doFireballAttack(player);
    			break;
    		}
		}
    }
	
	// fire spell
	void doFireEffect(Player player) {
		player.setFireTicks(100);
	}

	// firework spell
	void doFirework(Player player) {
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
	void doExplosion(Player player) {
		Location loc = player.getLocation();
		loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 20.0F, false, false);
		// throw player a random direction
		Vector vec = new Vector((rand.nextDouble()*2.0)-1.0, rand.nextDouble()*2.0, (rand.nextDouble()*2.0)-1.0);
		player.setVelocity(vec.multiply(2));
	}

	// lightning spell
	void doLightning(Player player ) {
		player.getWorld().strikeLightningEffect(player.getLocation());
	}

	// soaking spell
	void doDrenchEffect(Player player) {
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
					loc.getWorld().spawnParticle(particlemap.get(EventType.DRENCH), loc, 1);
					loc.subtract(x, y, z);
				}
				if (phi > Math.PI*8) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// spiral cone spell
	void doConeEffect(Player player){
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
						loc.getWorld().spawnParticle(particlemap.get(EventType.CONE), loc, 2);
						loc.subtract(x,y,z);
					}
 				}		
 				if (phi > 10 * Math.PI){						
					this.cancel();
				}
			}	
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 3);
	}
	
	// frost lord spell
	void doFrostLordEffect(Player player) {
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
					loc.getWorld().spawnParticle(particlemap.get(EventType.FROSTLORD), loc, 10, 0.2, 0.0, 0.2, snow);
					loc.subtract(x,y,z);
 				}		
 				if (t >= Math.PI * 4) {
 					loc.add(x, y, z);
 					loc.getWorld().playEffect(loc, Effect.INSTANT_POTION_BREAK, Color.WHITE);
					this.cancel();
				}

			}	
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// pee spell
	void doPeeEffect(Player player) {
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
					player.getLocation().getWorld().spawnParticle(particlemap.get(EventType.PEE), player.getLocation(), 1, 0.2, 0, 0.2);
					player.getLocation().subtract(x, y, z);
				}
				if (phi > Math.PI * 4) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	// fireball attack spell
	void doFireballAttack(Player player) {
		int multiplier = rand.nextDouble() <= 0.1 ? 2+rand.nextInt(2) : 1;
		if (player.getLocation().getY() > 44 && player.getLocation().getY() < 200) {
			for (int i = 0; i < multiplier; i++ ) {
						launchFireball(player);
			}
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
