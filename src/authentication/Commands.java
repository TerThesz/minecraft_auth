package authentication;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Commands implements CommandExecutor {
	private Plugin plugin = Main.getPlugin(Main.class);
	public static String[] commands = {"authentication", "login", "register", "logout", "unregister"};
	private Map attempts = new HashMap<UUID, Integer>();
	public static Map cooldown = new HashMap<UUID, Date>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String arg2, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			Database database = new Database();
			
			switch (cmd.getName().toLowerCase()) {
				case "login":
					if ((Boolean) Events.authenticationInfo.get(p.getUniqueId())) {
						p.sendMessage(ChatColor.GREEN + "You are already authenticated.");
						break;
					}
					
					if (args.length == 0) {
						p.sendMessage(ChatColor.DARK_RED + "Define a password.");
						break;
					}
					
					Boolean successfulLogin = database.loginPlayer(p, args[0]);
					
					if (!successfulLogin) {
						if (attempts.containsKey(p.getUniqueId())) {
							if ((Integer) attempts.get(p.getUniqueId()) >= plugin.getConfig().getInt("kick_after_unsuccessful_login_attempts")) {
								p.kickPlayer(ChatColor.RED + "You have exceeded your login attempts.\nTry again in: " + plugin.getConfig().getInt("rejoin_after_unsuccessful_login_attempts_cooldown") + "s");
								cooldown.put(p.getUniqueId(), new Date());
								
								Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
								     public void run() {
								    	 cooldown.remove(p.getUniqueId());
								    	 attempts.replace(p.getUniqueId(), 0);
								     }
								}, (plugin.getConfig().getInt("rejoin_after_unsuccessful_login_attempts_cooldown") * 20));
							
							} else
								attempts.replace(p.getUniqueId(), (Integer) attempts.get(p.getUniqueId()) + 1);
						} else
							attempts.put(p.getUniqueId(), 0);
						
						p.sendMessage(ChatColor.RED + "Invalid password. Try again.");
						break;
					}
					
					p.sendMessage(ChatColor.GREEN + "Successfuly loged in.");
					p.removePotionEffect(PotionEffectType.BLINDNESS);
					Events.authenticationInfo.replace(p.getUniqueId(), true);
					Events.protect.add(p.getUniqueId());

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							Events.protect.remove(p.getUniqueId());
						}
					}, (plugin.getConfig().getInt("protection_after_login") * 20));
					
					break;
				case "logout":
					if (!(Boolean) Events.authenticationInfo.containsKey(p.getUniqueId())) {
						p.sendMessage(ChatColor.RED + "You are not logged in.");
						break;
					}
					
					Events.authenticationInfo.replace(p.getUniqueId(), false);
					p.sendMessage(ChatColor.GREEN + "You have been successfuly logged out.");
					
					p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, plugin.getConfig().getInt("session_timeout") * 20, 1));
					String message = ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/login <password>" + ChatColor.YELLOW + " to login.";
					
					Boolean authenticated = false;
					
					final String msg = message;
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					     public void run() {
					    	 if (!(Boolean) Events.authenticationInfo.get(p.getUniqueId())) {
					    		 p.sendMessage(msg);
					    		 Events.authMessage(msg, 0, plugin.getConfig().getInt("auth_message_cooldown"), p);
					    	 }
					     }
					}, (3 * 20));
					break;
				case "unregister":
					// todo
					break;
				case "register":
					if ((Boolean) Events.authenticationInfo.get(p.getUniqueId())) {
						p.sendMessage(ChatColor.GREEN + "You are already authenticated.");
						break;
					}
					
					if (args.length <= 1) {
						p.sendMessage(ChatColor.DARK_RED + "Define the passwords.");
						break;
					}
					
					if (args[0].length() > plugin.getConfig().getInt("maximum_password_length") || args[0].length() < plugin.getConfig().getInt("minimum_password_length")) {
						p.sendMessage(ChatColor.DARK_RED + "Password must be longer than " + plugin.getConfig().getInt("minimum_password_length") + " characters and shorter than " + plugin.getConfig().getInt("maximum_password_length") + " characters.");
						break;
					}
					
					if (!args[0].equals(args[1])) {
						p.sendMessage(ChatColor.DARK_RED + "Passwords don't match.");
						break;
					}
					
					Boolean successfulRegister = database.registerPlayer(p, args[0]);
					
					if (!successfulRegister) {
						p.sendMessage(ChatColor.RED + "Invalid password. Try again.");
						break;
					}
					
					p.sendMessage(ChatColor.GREEN + "Successfuly registered in.");
					p.removePotionEffect(PotionEffectType.BLINDNESS);
					Events.authenticationInfo.replace(p.getUniqueId(), true);
					Events.protect.add(p.getUniqueId());

					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							Events.protect.remove(p.getUniqueId());
						}
					}, (plugin.getConfig().getInt("protection_after_login") * 20));
					
					break;
			}
		}
		
		switch (cmd.getName().toLowerCase()) {
			case "authentication":
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
						if (sender instanceof Player && !((Player)sender).hasPermission("auth.reload")) {
							((Player)sender).sendMessage(ChatColor.RED + "Insufficient permissions.");
							break;
						}
						
						plugin.reloadConfig();
						plugin.saveDefaultConfig();
						
						sendMessage(sender, ChatColor.GREEN + "Config successfuly reloaded.");
					}
				} else if (sender instanceof Player)
					((Player)sender).chat("/help " + plugin.getDescription().getName());
				break;
		}
		return true;
	}
	
	void sendMessage(CommandSender sender, String message) {
		if (sender instanceof Player)
			((Player)sender).sendMessage(message);
		else
			plugin.getLogger().info(message);
	}
}
