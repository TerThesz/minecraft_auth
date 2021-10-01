package authentication;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mysql.jdbc.PreparedStatement;

import net.md_5.bungee.api.ChatColor;

public class Database {
	Main plugin = Main.getPlugin(Main.class);
	
	public boolean playerHasEntry(UUID uuid) {
		try {
			PreparedStatement statement = (PreparedStatement) MySQL.getConnection().prepareStatement("SELECT * FROM " + MySQL.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			if (results.next()) return true;
			else return false;
		} catch (SQLException e) {
			MySQL.exceptionMessage(e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean registerPlayer(Player p, String password) {
		UUID uuid = p.getUniqueId();
		
		if (playerHasEntry(uuid)) {
			p.sendMessage(ChatColor.DARK_RED + "You are already registered.");
			return false;
		}
			
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			
			String pswd_salt = Hash.salt();
			String ip_salt = Hash.salt();
			
			String hashedPassword = Hash.hash((password + pswd_salt));
			String hashedIp = Hash.hash((p.getAddress().toString().split(":")[0].replace("/", "") + ip_salt));
			
			try {
				PreparedStatement statement = (PreparedStatement) MySQL.getConnection().prepareStatement("SELECT * FROM " + MySQL.table + " WHERE UUID=?");
				statement.setString(1, uuid.toString());
				
				ResultSet results = statement.executeQuery();
				results.next();
				
				PreparedStatement insert = (PreparedStatement) MySQL.getConnection().prepareStatement("INSERT INTO `" + MySQL.table + "` VALUES (?, ?, ?, ?, ?, ?)");
				
				insert.setString(1, uuid.toString());
				insert.setString(2, hashedPassword);
				insert.setString(3, pswd_salt);
				insert.setString(4, hashedIp);
				insert.setString(5, ip_salt);
				insert.setInt(6, 0);
				insert.executeUpdate();
				
				return true;
				
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean sessionLogin(Player p) {
		UUID uuid = p.getUniqueId();
		String ip = "",
				   salt = "";
		
		if (!playerHasEntry(p.getUniqueId())) return false;
		if (!Main.leaveDates.containsKey(p.getUniqueId())) return false;
		
		try {
			PreparedStatement statement = (PreparedStatement) MySQL.getConnection().prepareStatement("SELECT ip_address,ip_salt FROM " + MySQL.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			results.next();
			
			ip = results.getString("ip_address");
			salt = results.getString("ip_salt");
			
			String hashedIp = Hash.hash((p.getAddress().toString().split(":")[0].replace("/", "") + salt));
			
			if (Main.leaveDates.containsKey(p.getUniqueId()) && Utils.timeAgo(new Date(), (Date) Main.leaveDates.get(p.getUniqueId())) <= plugin.getConfig().getInt("session_timeout")
					&& ip.equals(hashedIp)) {
				p.sendMessage(ChatColor.GREEN + "Your session hasn't expired yed. You don't have to authentificate.");
				return true;
			}
		} catch (SQLException e) {
			MySQL.exceptionMessage(e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean loginPlayer(Player p, String password) {
		UUID uuid = p.getUniqueId();
		String passwd = "",
			   password_salt = "",
			   ip = "",
			   ip_salt = "";
		Boolean premium = false;
		
		if (!playerHasEntry(uuid)) {
			p.sendMessage(ChatColor.DARK_RED + "You are not registered.");
			return false;
		}
		
		try {
			PreparedStatement statement = (PreparedStatement) MySQL.getConnection().prepareStatement("SELECT * FROM " + MySQL.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());
			
			ResultSet results = statement.executeQuery();
			results.next();
			
			passwd = results.getString("password");
			password_salt = results.getString("password_salt");
			
			ip = results.getString("ip_address");
			ip_salt = results.getString("ip_salt");
			
			if (results.getInt("premium") == 1)
				premium = true;
			else
				premium = false;
			
			String hashedPassword = Hash.hash((password + password_salt));
			
			if (hashedPassword.equals(passwd))
				return true;
		} catch (SQLException e) {
			MySQL.exceptionMessage(e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}
}
