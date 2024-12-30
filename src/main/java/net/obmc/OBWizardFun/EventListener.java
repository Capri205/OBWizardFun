package net.obmc.OBWizardFun;

import java.util.logging.Logger;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;


public class EventListener implements Listener
{
	static Logger log = Logger.getLogger("Minecraft");
  
	@EventHandler
	public void targetEvent(EntityTargetEvent event) {
		Entity entity = event.getEntity();
		entity.getUniqueId().toString();
		String entityName = "";
		if (entity.customName() != null ) {
		    entityName = PlainTextComponentSerializer.plainText().serialize(entity.customName());
		}
		if (entity.getType().equals(EntityType.BEE) || entity.getType().equals(EntityType.WITCH) ||
				entity.getType().equals(EntityType.WOLF) || entity.getType().equals(EntityType.WARDEN)) {
			if ( !entityName.contains("Evil Witch") && !entityName.contains("Angry Bee") &&
					!entityName.contains("Rabid Wolf") && !entityName.contains("Wizards Warden")) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void damageEvent(EntityDamageByEntityEvent event) {
		event.setDamage(0);
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		event.setDamage(0);
		event.setCancelled(true);
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