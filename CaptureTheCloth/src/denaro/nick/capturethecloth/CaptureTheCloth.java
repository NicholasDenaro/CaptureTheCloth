package denaro.nick.capturethecloth;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CaptureTheCloth extends JavaPlugin
{
	private static CaptureTheCloth instance;
	private HashMap<String, Team> teams;
	private HashMap<String, Match> matches;
	private Location lobbyLocation;
	private HashMap<Player, Match> playersMatch;
	private HashMap<Player, Team> playersTeam;
	
	@Override
	public void onEnable()
	{
		instance = this;
		matches = new HashMap<String, Match>();
		playersMatch = new HashMap<Player, Match>();
		playersTeam = new HashMap<Player, Team>();
		createConfig();
		loadTeams();
		loadMatches();
		loadCommands();
		registerListeners();
		System.out.println("CaptureTheCloth was enabled!");
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("CaptureTheCloth was disabled!");
	}
	
	private void registerListeners()
	{
		this.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
	}
	
	public Location getLobbyLocation()
	{
		return lobbyLocation != null ? lobbyLocation : this.getServer().getWorlds().get(0).getSpawnLocation();
	}
	
	public static CaptureTheCloth instance()
	{
		return instance;
	}
	
	public Location getPlayerSpawn(Player player)
	{
		Match match = playersMatch.get(player);
		Team team = playersTeam.get(player);
		if(match != null && team != null)
		{
			Location location = playersMatch.get(player).getTeamSpawn(team);
			if(location == null)
			{
				player.sendMessage("team spawn is null.");
			}
			return location;
		}
		player.sendMessage("Not in a team or match.");
		return null;
	}
	
	private void loadCommands()
	{
		CommandEvents commander = new CommandEvents();
		this.getCommand("create-team").setExecutor(commander);
		this.getCommand("create-match").setExecutor(commander);
		this.getCommand("set-team-spawn").setExecutor(commander);
		this.getCommand("set-team-flag").setExecutor(commander);
		this.getCommand("join-match").setExecutor(commander);
		this.getCommand("leave-match").setExecutor(commander);
		this.getCommand("start-match").setExecutor(commander);
		this.getCommand("save-match").setExecutor(commander);
		this.getCommand("get-team").setExecutor(commander);
		this.getCommand("invisible").setExecutor(commander);
	}
	
	private void createConfig()
	{
		File file = new File(getDataFolder(), "config.yml");
        if (!file.exists())
        {
            getLogger().info("config.yml not found, creating!");
            saveDefaultConfig();
        }
        else
        {
            getLogger().info("config.yml found, loading!");
        }
	}
	
	public boolean sameTeam(Player p1, Player p2)
	{
		if(playersMatch.get(p1) != playersMatch.get(p2))
		{
			p1.sendMessage(ChatColor.DARK_BLUE + "Not in the same match you twat.");
			return false;
		}
		Match match = playersMatch.get(p1);
		
		return match.sameTeam(p1, p2);
	}
	
	public boolean setTeamSpawn(Player player)
	{
		Team team = playersTeam.get(player);
		Match match = playersMatch.get(player);
		
		if(match == null || team == null)
		{
			return false;
		}
		
		match.setTeamSpawn(team, player.getLocation());
		return true;
	}
	
	public boolean setTeamFlag(Player player)
	{
		Team team = playersTeam.get(player);
		Match match = playersMatch.get(player);
		
		if(match == null || team == null)
		{
			return false;
		}
		
		match.setTeamFlag(team, player.getLocation());
		return true;
	}
	
	public Match getMatch(Player player)
	{
		return playersMatch.get(player);
	}
	
	public Team getTeam(Player player)
	{
		return playersTeam.get(player);
	}
	
	public boolean startMatch(String name)
	{
		for(Match game: matches.values())
		{
			if(game.getName().equals(name))
			{
				return game.start();
			}
		}
		
		return false;
	}
	
	public boolean joinMatch(String matchName, String teamName, Player player)
	{
		for(Match match: matches.values())
		{
			if(match.getName().equals(matchName))
			{
				if(match.addPlayer(teamName, player))
				{
					playersMatch.put(player, match);
					playersTeam.put(player,match.getTeam(player));
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		return false;
	}
	
	public boolean leaveMatch(Player player)
	{
		if(playersMatch.get(player) == null)
		{
			return false;
		}
		else
		{
			Match game = playersMatch.get(player);
			for(Player p : this.getServer().getOnlinePlayers())
			{
				p.showPlayer(player);
				player.showPlayer(p);
			}
			if(game.removePlayer(player))
			{
				playersMatch.put(player, null);
				player.teleport(getLobbyLocation());
				player.getInventory().clear();
				return true;
			}
		}
		return false;
	}
	
	public boolean createMatch(String name, long time, int maxTeamSize, String... teamNames)
	{
		if(time <= 0 || maxTeamSize < 1)
		{
			return false;
		}
		for(String teamName : teamNames)
		{
			if(!teams.containsKey(teamName))
			{
				return false;
			}
		}
		
		for(Match game: matches.values())
		{
			if(game.getName().equals(name))
			{
				return false;
			}
		}
		
		Match match = new Match(name, time, maxTeamSize);
		matches.put(name, match);
		
		for(String teamName : teamNames)
		{
			Team team = teams.get(teamName);
			if(!match.addTeam(team.copy()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean createTeam(String name, ItemStack blockdata, ItemStack bannerdata)
	{
		for(String teamName: teams.keySet())
		{
			if(teamName.equals(name))
			{
				return false;
			}
		}
		
		Team team = new Team(name, blockdata, bannerdata);
		teams.put(team.getName(), team);
		saveTeams();
		return true;
	}
	
	public boolean saveMatch(String matchName)
	{
		Match match = matches.get(matchName);
		if(match == null)
		{
			return false;
		}
		
		FileConfiguration config = this.getConfig();
		config.set("matches."+match.getName()+".time", match.getTime());
		config.set("matches."+match.getName()+".teamSize", match.getTeamSize());
		
		String[] teamNames = match.getTeamNames();
		
		for(String teamName : teamNames)
		{
			config.set("matches."+match.getName()+".teams."+teamName+".spawn", match.getTeamSpawn(teamName));
			config.set("matches."+match.getName()+".teams."+teamName+".flag", match.getTeamFlag(teamName));
		}
		
		try
		{
			File file = new File(getDataFolder(), "config.yml");
			config.save(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return true;
	}
	
	public void loadMatches()
	{
		matches = new HashMap<String, Match>();
		FileConfiguration config = this.getConfig();
		ConfigurationSection section = config.getConfigurationSection("matches");
		if(section != null)
		{
			Set<String> keys = section.getKeys(false);
			for(String key : keys)
			{
				long time = section.getLong(key+".time");
				int teamSize = section.getInt(key+".teamSize");
				
				System.out.println("match: " + key + "\ntime: " + time + "\nsize: " + teamSize);
				
				
				Match match = new Match(key, time, teamSize);
				
				ConfigurationSection teamSection = section.getConfigurationSection(key + ".teams");
				Set<String> teamKeys = teamSection.getKeys(false);
				for(String teamKey : teamKeys)
				{
					Team team = teams.get(teamKey).copy();
					if(team != null)
					{
						match.addTeam(team);
						Location spawn = (Location) teamSection.get(teamKey + ".spawn");
						Location flag = (Location) teamSection.get(teamKey + ".flag");
						match.setTeamSpawn(team, spawn);
						match.setTeamFlag(team, flag);
					}
					else
					{
						System.out.println("Team '"+ teamKey +"' does not exist.");
					}
				}
				
				matches.put(key, match);
			}
		}
	}
	
	public void saveTeams()
	{
		FileConfiguration config = this.getConfig();
		
		for(Team team : teams.values())
		{
			config.set("teams." + team.getName() + ".block", team.getBlock());
			config.set("teams." + team.getName() + ".banner", team.getBanner());
		}
		
		try
		{
			File file = new File(getDataFolder(), "config.yml");
			config.save(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void loadTeams()
	{
		teams = new HashMap<String, Team>();
		FileConfiguration config = this.getConfig();
		ConfigurationSection section = config.getConfigurationSection("teams");
		if(section != null)
		{
			Set<String> keys = section.getKeys(false);
			for(String key : keys)
			{
				ItemStack block = section.getItemStack(key+".block");
				ItemStack banner = section.getItemStack(key+".banner");
				
				teams.put(key, new Team(key, block, banner));
			}
		}
		else
		{
			System.out.println("teams was null");
		}
	}
}
