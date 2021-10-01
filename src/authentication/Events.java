package authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;

public class Events implements Listener {
	private static Main plugin = Main.getPlugin(Main.class);
	private Database database = new Database();
	public static Map authenticationInfo = new HashMap<UUID, Boolean>();
	public static List<UUID> protect = new ArrayList<UUID>();
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		
		Location from = event.getFrom();
		Location to = event.getTo();
		
		if (!(Boolean) authenticationInfo.get(p.getUniqueId()) && from.getZ() != event.getTo().getZ() && from.getX() != event.getTo().getX())
			event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onChatMessage(AsyncPlayerChatEvent event) {
		if (!(Boolean) authenticationInfo.get(event.getPlayer().getUniqueId()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		Boolean found = false;
		if (!(Boolean) authenticationInfo.get(event.getPlayer().getUniqueId())) {
			for (String s : plugin.getConfig().getStringList("allowed_commands_before_login")) {
				if (event.getMessage().toLowerCase().startsWith("/" + s))
					found = true;
			}
			
			if (!found)
				event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && ( !(Boolean) authenticationInfo.get( ( (Player) event.getEntity() ).getUniqueId() ) || protect.contains( ( (Player) event.getEntity() ).getUniqueId() ) ) )
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		
		if ((Boolean) authenticationInfo.get(p.getUniqueId())) {
			authenticationInfo.remove(p.getUniqueId());
			
			if (Main.leaveDates.containsKey(p.getUniqueId()))
				Main.leaveDates.replace(p.getUniqueId(), new Date());
			else
				Main.leaveDates.put(p.getUniqueId(), new Date());
		} else if (Main.leaveDates.containsKey(p.getUniqueId()))
			Main.leaveDates.remove(p.getUniqueId());
	}
	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		
		if (Commands.cooldown.containsKey(p.getUniqueId())) {
			event.disallow(Result.KICK_OTHER,ChatColor.RED + "You have exceeded your login attempts.\nTry again in: " + (plugin.getConfig().getInt("rejoin_after_unsuccessful_login_attempts_cooldown") - Utils.timeAgo(new Date(), (Date) Commands.cooldown.get(p.getUniqueId()))) + "s");
			return;
		}
		
		authenticationInfo.put(event.getPlayer().getUniqueId(), false);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		UUID uuid = p.getUniqueId();
		
		if (database.sessionLogin(p)) {
			authenticationInfo.replace(p.getUniqueId(), true);
			protect.add(p.getUniqueId());
			
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					protect.remove(p.getUniqueId());
				}
			}, (plugin.getConfig().getInt("protection_after_login") * 20));
			return;
		}
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, plugin.getConfig().getInt("session_timeout") * 20, 1));
		String message = ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/register <password> <confirm password>" + ChatColor.YELLOW + " to register.";
		
		if (database.playerHasEntry(p.getUniqueId()))
			message = ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/login <password>" + ChatColor.YELLOW + " to login.";
		
		Boolean authenticated = false;
		
		final String msg = message;
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		     public void run() {
		    	 if (!(Boolean) authenticationInfo.get(p.getUniqueId())) {
		    		 p.sendMessage(msg);
		    		 authMessage(msg, 0, plugin.getConfig().getInt("auth_message_cooldown"), p);
		    	 }
		     }
		}, (3 * 20));
	}
	
	public static void authMessage(String message, Integer _count, Integer wait, Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		     public void run() {
		    	 Integer count = _count + 1;
		    	 if (!(Boolean) authenticationInfo.get(p.getUniqueId())) {
		    		 p.sendMessage(message);
		    		 if (count < plugin.getConfig().getInt("kick_after_auth_messages"))
		    			 authMessage(message, count, wait, p);
		    		 else
		    			 p.kickPlayer(ChatColor.RED + "You didn't login in time.");
		    	 }
		     }
		}, (wait * 20));
	}
}
