package net.obmc.OBWizardFun;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
			if (args.length < 2) {
				Usage(sender);
				return true;
			}
			if (!OBWizardFun.getInstance().isSpell(args[0].toUpperCase())) {
				sender.sendMessage(chatmsgprefix + ChatColor.RED + "Invalid spell provided");
				sender.sendMessage(chatmsgprefix + "Valid spells are: FIRE, FIREWORK, EXPLOSION, LIGHTNING, SOAK, WEIRD,");
				sender.sendMessage(chatmsgprefix + "    FROST, PEE, GEYSER, FIREBALL, SOUNDEFFECT, EVILWITCH, ANGRYBEES");
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
