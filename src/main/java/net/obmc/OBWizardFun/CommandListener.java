package net.obmc.OBWizardFun;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {

	static Logger log = Logger.getLogger("Minecraft");
	
	private String chatmsgprefix = null;
	private String logmsgprefix = null;
	
	public CommandListener() {
		chatmsgprefix = OBWizardFun.getInstance().getChatMsgPrefix();
		logmsgprefix = OBWizardFun.getInstance().getLogMsgPrefix();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		// usage if no arguments passed
		if (args.length == 0) {
			Usage(sender);
			return true;
		}

		// process the command and any arguments
		if (command.getName().equalsIgnoreCase("cast")) {
			//TODO: remove when testing done
			if (args[0].equals("show")) {
				Iterator<Entity> eit = Bukkit.getWorld("world").getEntities().iterator();
				while (eit.hasNext()) {
					Entity entity = eit.next();
					log.log(Level.INFO, "debug - " + entity.getType().toString() +", " + entity.getCustomName() + ", " + entity.getLocation().getX() + ", " + entity.getLocation().getY() + ", " + entity.getLocation().getZ());
				}
				return true;
			}
			
			//TODO: remove when testing done
			if (args[0].equals("spawn")) {
				Player player = (Player) sender;
				try {
					player.getWorld().spawnEntity(player.getLocation(), EntityType.valueOf(args[1]));
					log.log(Level.INFO, "debug - spawned a " + args[1] + " at " + player.getName() + "'s location");
				} catch (Exception e) {
					log.log(Level.INFO, "debug - failed to spawn a " + args[1] + " at " + player.getName() + "'s location");
				}
				return true;
			}
			
			//TODO: remove when testing done
			if (args[0].equals("killall")) {
				Iterator<Entity> eit = Bukkit.getWorld("world").getEntities().iterator();
				int killed = 0;
				while (eit.hasNext()) {
					Entity entity = eit.next();
					String entityname = null;
					try {
						entityname = entity.getCustomName();
					} catch (NullPointerException e) {
					}
					if (entity.getType().equals(EntityType.valueOf(args[1].toUpperCase())) && entityname == null) {
						entity.remove();
						killed++;
					}
				}
				return true;
			}

			//TODO: remove when testing done
			if (args[0].equals("killnamed")) {
				Iterator<Entity> eit = Bukkit.getWorld("world").getEntities().iterator();
				int killed = 0;
				while (eit.hasNext()) {
					Entity entity = eit.next();
					String entityname = "";
					try {
						entityname = entity.getCustomName();
						entityname = ChatColor.stripColor(entityname);
						String mobname = args[1];
						for (int i = 2; i<args.length; i++) {
							mobname = mobname + " " + args[i];
						}
						if (entityname.contains(mobname)) {
							log.log(Level.INFO, "debug - killnamed - removing " + mobname + ", uuid: " +entity.getUniqueId().toString());
							entity.remove();
							killed++;
						}
					} catch (Exception e) {
					}
				}
				return true;
			}

			if (args.length < 2) {
				Usage(sender);
				return true;
			}
			if (!OBWizardFun.getInstance().isSpell(args[0].toUpperCase())) {
				sender.sendMessage(chatmsgprefix + ChatColor.RED + "Invalid spell provided");
				sender.sendMessage(chatmsgprefix + "Valid spells are:");
				sender.sendMessage(chatmsgprefix + "    FIRE, FIREWORK, EXPLOSION, LIGHTNING, SOAK, WEIRD,");
				sender.sendMessage(chatmsgprefix + "    FROST, PEE, GEYSER, FIREBALL, SOUNDEFFECT,");
				sender.sendMessage(chatmsgprefix + "    EVILWITCH, ANGRYBEES, RABIDWOLVES, WRATHWARDEN,");
				sender.sendMessage(chatmsgprefix + "    BATTYBATS, DANCINGENTITY");
				return true;
			}
			boolean playeronline = false;
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().equals(args[1])) {
					playeronline = true;
					break;
				}
			}
			if (!playeronline) {
				sender.sendMessage(chatmsgprefix + ChatColor.RED + "Player " + ChatColor.WHITE + args[1] + ChatColor.RED + " is not online!");
				return true;
			}

   			OBWizardFun.getInstance().castSpell(OBWizardFun.getInstance().getSpell(args[0].toUpperCase()), false, true, (Player) sender, Bukkit.getPlayerExact(args[1]));
		
		}
		return true;
	}

    void Usage(CommandSender sender) {
    	sender.sendMessage(chatmsgprefix + "/cast <spelltype> <player>" + ChatColor.GOLD + " - Cast a spell on a player");
    }
}
