package net.obmc.OBWizardFun;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;


public class EventListener implements Listener
{
	static Logger log = Logger.getLogger( "Minecraft" );
  
	@EventHandler
	public void entityTargetEntity(EntityTargetLivingEntityEvent event) {
		String entityuuid = event.getEntity().getUniqueId().toString();
		String targetuuid = "notarget";
		if ( event.getTarget() != null ) {
			targetuuid = event.getTarget().getUniqueId().toString();
		}
	}
	
	@EventHandler
	public void targetEvent( EntityTargetEvent event ) {
		Entity entity = event.getEntity();
		String entityuuid = entity.getUniqueId().toString();
		String targetuuid = "notarget";
		String entityname = "";
		if (entity.getCustomName() != null ) {
			entityname = ChatColor.stripColor(entity.getCustomName());
		}
		if (entity.getType().equals(EntityType.BEE) || entity.getType().equals(EntityType.WITCH) || entity.getType().equals(EntityType.WOLF)) {
			if ( !entityname.contains("Evil Witch") && !entityname.contains("Angry Bee") || entityname.contains("Rabid Wolf")) {
				event.setCancelled(true);
			}
		}
		if ( event.getTarget() != null ) {
			targetuuid = event.getTarget().getUniqueId().toString();
		}
	}
	
	@EventHandler
	public void damageEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType().equals(EntityType.BEE) || event.getDamager().getType().equals(EntityType.WOLF) || event.getDamager().getType().equals(EntityType.SPLASH_POTION)) {
			event.setDamage(0.01);
		} else if (event.getDamager().getType().equals(EntityType.AREA_EFFECT_CLOUD)) {
			event.setDamage(0.05);
		} else if (event.getDamager().getType().equals(EntityType.SPLASH_POTION)) {
			event.setDamage(0.03);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Player player = null;
		if (event.getEntity() instanceof Player) {
			player = (Player) event.getEntity();
		}
		if (event.getCause().equals(DamageCause.POISON)) {
			event.setDamage(0.03);
		} else if (event.getCause().equals(DamageCause.FLY_INTO_WALL) || event.getCause().equals(DamageCause.ENTITY_EXPLOSION)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		// remove lingering effects and fill health and hunger bars when player joins
		Player player = event.getPlayer();
		if (player.hasPotionEffect(PotionEffectType.POISON)) {
			player.removePotionEffect(PotionEffectType.POISON);
		}
		player.setHealth(20.0);
		player.setFoodLevel(20);
	}
}