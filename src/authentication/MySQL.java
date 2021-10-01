package authentication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

import net.md_5.bungee.api.ChatColor;

public class MySQL {

    public static Integer port = 3306;
    public static Connection connection;
    public static String table = null,
		      	  host = null,
		      	  database = null,
		      	  username = null,
		      	  password = null;

    static ConsoleCommandSender console = Bukkit.getConsoleSender();
    private static Plugin plugin = Main.getPlugin(Main.class);
    
    public static void start() {
    	console.sendMessage("Starting MySQL setup.");
    	
    	host = plugin.getConfig().getString("host");
		username = plugin.getConfig().getString("username");
		password = plugin.getConfig().getString("password");
		database = plugin.getConfig().getString("database");
		table = plugin.getConfig().getString("table");
		port = plugin.getConfig().getInt("port");
		
		console.sendMessage("- Retrieved information from config.");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("com.mysql.jdbc.Statement");
			Class.forName("com.mysql.jdbc.PreparedStatement");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		connect();
		createTableIfNotExists();
		
		console.sendMessage("Finished MySQL setup.");
    }

    private static void connect() {
        if (!isConnected()) {
            try {
            	synchronized (plugin) {
                    connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
                    console.sendMessage("- Connected to MySQL.");
    			}
            } catch (SQLException e) {
            	exceptionMessage(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                console.sendMessage("Disconnected from MySQL.");
            } catch (SQLException e) {
            	exceptionMessage(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    static void createTableIfNotExists() {
		try {
		    String sqlCreate = "CREATE TABLE IF NOT EXISTS `" + table + "` ("
            + "  `UUID` CHAR(36) NOT NULL,"
	    	+ "  `password` VARCHAR(64) NOT NULL,"
	    	+ "  `password_salt` CHAR(5) NOT NULL,"
	    	+ "  `ip_address` VARCHAR(64) NOT NULL,"
	    	+ "  `ip_salt` CHAR(5) NOT NULL,"
	    	+ "  `premium` INT(1) NOT NULL,"
    		+ "  PRIMARY KEY (`UUID`)"
	    	+ ")";
		    
		    Statement stmt = (Statement) getConnection().createStatement();
		    stmt.execute(sqlCreate);
		    
		    console.sendMessage("- Created table if it doesn't exist.");
		} catch (SQLException e) {
			exceptionMessage(e.getMessage());
			e.printStackTrace();
		}
	}

    public static boolean isConnected() {
        return (connection == null ? false : true);
    }

    public static Connection getConnection() {
        return connection;
    }
    
	public static void exceptionMessage(String exceptionMessage) {
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.isOp()) p.sendMessage(ChatColor.DARK_RED + "Check console for MySQL exception.");
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[Auth] ERROR --> " + exceptionMessage);
	}
}