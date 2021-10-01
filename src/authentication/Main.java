package authentication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.mysql.jdbc.Statement;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	public static Map leaveDates = new HashMap<UUID, Date>();
	
	private Connection connection;
	public String host, username, password, database, table;
	public Integer port;
	
	@Override
	public void onEnable() {
		Bukkit.getConsoleSender().sendMessage("[Auth] Starting plugin. . .");
		
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
		MySQL.start();
		
		Commands commands = new Commands();
		for (String cmd : Commands.commands)
			this.getCommand(cmd).setExecutor(commands);
		
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);
		
		Database database = new Database();
		for (Player p : Bukkit.getOnlinePlayers()) {
			Events.authenticationInfo.put(p.getUniqueId(), false);
			UUID uuid = p.getUniqueId();
			
			String message = ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/register <password> <confirm password>" + ChatColor.YELLOW + " to register.";
			
			if (database.playerHasEntry(p.getUniqueId()))
				message = ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/login <password>" + ChatColor.YELLOW + " to login.";
			
			Boolean authenticated = false;
			
			final String msg = message;
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			     public void run() {
			    	 if (!(Boolean) Events.authenticationInfo.get(p.getUniqueId())) {
			    		 p.sendMessage(msg);
			    		 Events.authMessage(msg, 0, getConfig().getInt("try_message_cooldown"), p);
			    	 }
			     }
			}, (3 * 20));
		}
		
		Bukkit.getConsoleSender().sendMessage("[Auth] Setup finished.");
	}
	
	@Override
	public void onDisable() {
		saveDefaultConfig();
		
		MySQL.disconnect();
	}
}
