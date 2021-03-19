package net.obmc.OBWizardFun;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
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
	
	java.util.Random rand = new java.util.Random();
	private int eventtype = 0;
	private boolean doallplayers = false;
	private boolean domessage = false;
	private int taskid;
	
    public void onEnable() {
		log.log(Level.INFO, "[OBWizardFun] Plugin Version " + this.getDescription().getVersion() + " activated");
		
		// enable the main task
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		taskid = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
        		eventtype = 0;
        		doallplayers = false;
        		domessage = false;
        		
            	// 10% chance to hit all players, 90% of time one random player
            	double r = rand.nextDouble();
            	if (r < 0.1 && eventtype != 0) {
            		log.log(Level.INFO, "debug -  doing all players");
            		doallplayers = true;
            	}
            	
            	// 20% chance of a message - some events might override by ignoring this
            	r = rand.nextDouble();
            	if (r < 0.20) {
            		domessage = true;
            	}
            	
            	// process event
        		// get our base random event - currently looking at 10 events (0.0~0.1, 0.1~0.2 etc)
            	eventtype = rand.nextInt(12);
            	if (Bukkit.getOnlinePlayers().size() > 0) {
            		switch(eventtype) {
            		case 0:
            			doExplosion();
            			break;
            		case 1:
            			doFirework();
            			break;
            		case 2:
            			doLightning();
            			break;
            		case 3:
            			doDrenchEffect();
            			break;
            		case 4:
            			doFireEffect();
            			break;
            		case 5:
            			doPeeEffect();
            			break;
            		case 6:
            			doFireballAttack();
            			break;
            		case 7: case 8: case 9: case 10: case 11:
            			break;
            		}
            	}
            	
            } 
        }, 0L, 600L);
    }

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTask(taskid);
		Bukkit.getLogger().info("[OBFireworksOnJoin] Plugin unloaded");
	}

	// a random firework
	void doFirework() {
    	if (doallplayers){
			log.log(Level.INFO, "debug - firework all players");
    		for (Player player : Bukkit.getOnlinePlayers()) {
				if (domessage) {
					player.sendMessage(ChatColor.AQUA + "A wizard just let fireworks under everyone!");
				}
    			doFirework(player.getLocation());
    		}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - firework player " + player.getName());
			if (player != null) {
				if (domessage) {
					for (Player allplayer : Bukkit.getOnlinePlayers()) {
						allplayer.sendMessage(ChatColor.AQUA + "A wizard just lit a firework under " + player.getName());
					}
				}
				doFirework(player.getLocation());
			}
		}
	}
	void doFirework(Location loc) {
		final Firework f = (Firework)loc.getWorld().spawn(loc, (Class)Firework.class);
		final FireworkMeta fm = f.getFireworkMeta();
 		fm.addEffect(makeFirework());
		fm.setPower(rand.nextInt(6));
		f.setFireworkMeta(fm);
	}
	FireworkEffect makeFirework() {
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

	// explosion at each player location
	void doExplosion() {
		if (doallplayers) {
			log.log(Level.INFO, "debug - explode all players");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (domessage) {
					player.sendMessage(ChatColor.AQUA + "A wizard just blew everyone up!");
				}
				doExplosion(player);
			}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - explode player " + player.getName());
			if (player != null) {
				if (domessage) {
					for (Player allplayer : Bukkit.getOnlinePlayers()) {
						allplayer.sendMessage(ChatColor.AQUA + "A wizard just cast an explosion spell on " + player.getName());
					}
				}
				doExplosion(player);
			}
		}
	}
	void doExplosion(Player player) {
		Location loc = player.getLocation();
		loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), 20.0F, false, false);
		Vector vec = new Vector((rand.nextDouble()*2.0)-1.0, rand.nextDouble()*2.0, (rand.nextDouble()*2.0)-1.0);
		player.setVelocity(vec.multiply(2));
	}
	
	// striketh them down!
	void doLightning() {
		if (doallplayers) {
			log.log(Level.INFO, "debug - lightning all players");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (domessage) {
					player.sendMessage(ChatColor.AQUA + "A wizard just cast a " + ChatColor.WHITE + "lightning" + ChatColor.AQUA + " spell on everyone!");
				}
				doLightning(player.getLocation());
			}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - lightning player " + player.getName());
			if (player != null) {
				if (domessage) {
					for (Player allplayer : Bukkit.getOnlinePlayers()) {
						allplayer.sendMessage(ChatColor.AQUA + "A wizard just cast a " + ChatColor.WHITE + "lightning" + ChatColor.AQUA + " spell on " + player.getName());
					}
				}
				doLightning(player.getLocation());
			}
		}
	}
	void doLightning(Location loc) {
		loc.getWorld().strikeLightningEffect(loc);
	}
	
	// burn baby burn!
	void doFireEffect() {
		if (doallplayers) {
			log.log(Level.INFO, "debug - burn all players");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (domessage) {
					player.sendMessage(ChatColor.AQUA + "A wizard just set everyone on " + ChatColor.RED + "fire!");
				}
				player.playSound(player.getLocation(), Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 1.0f, 1.0f);
				doFireEffect(player);
			}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - burn player " + player.getName());
			if (player != null) {
				if (domessage) {
					for (Player allplayer : Bukkit.getOnlinePlayers()) {
						allplayer.sendMessage(ChatColor.AQUA + "A wizard just set" + player.getName() + " on " + ChatColor.RED + "fire!");
					}
				}
				player.playSound(player.getLocation(), Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 1.0f, 1.0f);
				doFireEffect(player);
			}
		}
	}
	void doFireEffect(Player player) {
		player.setFireTicks(100);
	}

	// Give 'em a soaking
	void doDrenchEffect() {
		if (doallplayers) {
			log.log(Level.INFO, "debug - drench effect all players");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (domessage) {
					player.sendMessage(ChatColor.AQUA + "A wizard just drenched everyone!");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
				doDrenchEffect(player.getLocation());
			}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - drench effect on player " + player.getName());
			if (player != null) {
				if (domessage) {
					for (Player allplayer : Bukkit.getOnlinePlayers()) {
						allplayer.sendMessage(ChatColor.AQUA + "A wizard just drenched " + player.getName() + "!");
					}
				}
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1.0f, 1.0f);
				doDrenchEffect(player.getLocation());
			}
		}
	}
	void doDrenchEffect(Location loc) {
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
					loc.getWorld().spawnParticle(Particle.WATER_DROP, loc, 1);
					loc.subtract(x, y, z);
				}
				if (phi > Math.PI*8) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	void doPeeEffect() {
		if (doallplayers) {
			log.log(Level.INFO, "debug - pee effect all players");
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.sendMessage(ChatColor.AQUA + "A wizard caused everyone to " + ChatColor.YELLOW + "pee " + ChatColor.AQUA + "themselves!");
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
				doPeeEffect(player);
			}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - pee effect on player " + player.getName());
			if (player != null) {
				for (Player allplayer : Bukkit.getOnlinePlayers()) {
					allplayer.sendMessage(ChatColor.AQUA + "A wizard caused " + player.getName() + " to " + ChatColor.YELLOW + "pee " + ChatColor.AQUA + "his pants!");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1.0f, 1.0f);
				doPeeEffect(player);
			}
		}
	}
	void doPeeEffect(Player player) {
		//TODO: unnecessarily complicated math for this effect - rework as a proper projectile stream (facing direction) or a simple pool like this ultimately does
		new BukkitRunnable() {
			double phi = 0;
			//Particle.DustOptions pee = new Particle.DustOptions(Color.fromRGB(217,186,30), 1);
			public void run() {
				phi += Math.PI/5;
				for (double theta = 0; theta < 2*Math.PI; theta += Math.PI/2.5) {
					double r = 1.5;
					double x = r*Math.cos(theta)*Math.sin(phi);
					double y = r*Math.cos(phi) + 1.5;
					double z = r*Math.sin(theta)*Math.sin(phi);
					player.getLocation().add(x, y, z);
					//player.getLocation().getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 1, 0, 0, 0, 0, pee);
					player.getLocation().getWorld().spawnParticle(Particle.DRIPPING_HONEY, player.getLocation(), 1, 0.2, 0, 0.2);
					player.getLocation().subtract(x, y, z);
				}
				if (phi > Math.PI*4) {
					this.cancel();
				}
			}
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("OBWizardFun"), 0, 1);
	}
	
	void doFireballAttack() {
		if (doallplayers) {
			log.log(Level.INFO, "debug - fireball attack on all players");
			for (Player player : Bukkit.getOnlinePlayers()) {
				player.sendMessage(ChatColor.AQUA + "A wizard launched " + ChatColor.GOLD + "fireballs" + ChatColor.AQUA + " at everyone! Take cover!");
				player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.0f);
				doFireballAttack(player);
			}
		} else {
			Player player = Bukkit.getOnlinePlayers().stream().skip((int) (Bukkit.getOnlinePlayers().size() * Math.random())).findFirst().orElse(null);
			log.log(Level.INFO, "debug - fireball attack on player " + player.getName());
			if (player != null) {
				for (Player allplayer : Bukkit.getOnlinePlayers()) {
					allplayer.sendMessage(ChatColor.AQUA + "A wizard launched a " + ChatColor.GOLD + "fireball" + ChatColor.AQUA + " at " + player.getName() +"!");
				}
				player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1.0f, 1.0f);
				doFireballAttack(player);
			}
		}
	}
	void doFireballAttack(Player player) {

		// get random point around player some distance away and up in the air
		double theta = 360 * rand.nextDouble();
		double dist = rand.nextDouble()*(80-65)+65;
		double xrand = dist * Math.cos(theta);
		double zrand = dist * Math.sin(theta);
		int yrand = 45+(55+rand.nextInt(70-55));	// water level is 45 in our lobby

		// get a fireball launch location based on player position and the random point
		Location ploc = player.getLocation();
		Location rloc = player.getLocation().add(xrand,yrand,zrand);
		log.log(Level.INFO, "debug -player offset : X=" + xrand +", Y="+yrand+", Z="+zrand);
		log.log(Level.INFO, "debug - player ploc : X=" + ploc.getX()+",Y="+ploc.getY()+",Z="+ploc.getZ());
		log.log(Level.INFO, "debug - player rloc : X=" + rloc.getX()+",Y="+rloc.getY()+",Z="+rloc.getZ());
		//ploc.getWorld().getBlockAt(ploc).setType(Material.YELLOW_WOOL);
		//rloc.getWorld().getBlockAt(rloc).setType(Material.RED_WOOL);
		
		// lanuch a fireball at the player
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
		fireball.setIsIncendiary(false);
		fireball.setGravity(false);
	}
}
