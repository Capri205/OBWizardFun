package net.obmc.OBWizardFun;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;


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
		String entityuuid = event.getEntity().getUniqueId().toString();
		String targetuuid = "notarget";
		if ( event.getTarget() != null ) {
			targetuuid = event.getTarget().getUniqueId().toString();
		}
	}
	
	@EventHandler
	public void damageEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType().equals(EntityType.BEE) || event.getDamager().getType().equals(EntityType.SPLASH_POTION)) {
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
		}
		if (event.getCause().equals(DamageCause.FLY_INTO_WALL)) {
			event.setCancelled(true);
		}
	}
}
