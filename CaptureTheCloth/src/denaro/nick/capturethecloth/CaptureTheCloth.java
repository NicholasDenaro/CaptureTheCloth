package denaro.nick.capturethecloth;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.minecraft.server.v1_9_R1.Tuple;

public class CaptureTheCloth extends JavaPlugin
{
	private boolean firstLoad = true;
	
	public static final long TICKS_PER_SECOND = 20;
	
	private static CaptureTheCloth instance;
	private HashMap<String, Team> teams;
	private HashMap<String, Match> matches;
	private Location lobbyLocation;
	private HashMap<Player, Match> playersMatch;
	private HashMap<Player, Team> playersTeam;
	private HashMap<Player, Loadout> playersLoadouts;
	private HashMap<String, Loadout> loadouts;
	private HashMap<Location, Tuple<Match, Team>> buttonsSpawn;
	private HashMap<Location, Tuple<Match, Team>> buttonsRoom;
	private HashSet<Player> playerLimbo;
	private HashSet<Match> startedMatches;
	
	@Override
	public void onEnable()
	{
		if(firstLoad)instance = this;
		matches = new HashMap<String, Match>();
		playersMatch = new HashMap<Player, Match>();
		playersTeam = new HashMap<Player, Team>();
		playersLoadouts = new HashMap<Player, Loadout>();
		buttonsSpawn = new HashMap<Location, Tuple<Match, Team>>();
		buttonsRoom = new HashMap<Location, Tuple<Match, Team>>();
		playerLimbo = new HashSet<Player>();
		startedMatches = new HashSet<Match>();
		if(firstLoad)createConfig();
		loadTeams();
		loadMatches();
		if(firstLoad)loadCommands();
		loadSettings();
		if(firstLoad)registerListeners();
		System.out.println("CaptureTheCloth was enabled!");
		getServer().broadcastMessage(ChatColor.AQUA + "Enabled CTC");
		getServer().broadcastMessage(ChatColor.AQUA + "testing... CTC");
		
		firstLoad = false;
	}
	
	@Override
	public void onDisable()
	{
		for(Player player : playersMatch.keySet())
		{
			getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Made " + player.getName()+" leave match.");
			//leaveMatch(player);
			player.getInventory().clear();
			player.teleport(lobbyLocation);
		}
		System.out.println("CaptureTheCloth was disabled!");
		getServer().broadcastMessage(ChatColor.DARK_AQUA + "Diabled CTC");
	}
	
	private void registerListeners()
	{
		loadouts = new HashMap<String, Loadout>();
		this.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
		loadouts.put("Archer", new Archer());
		loadouts.put("Magician", new Magician());
		loadouts.put("Warrior", new Warrior());
		this.getServer().getPluginManager().registerEvents(loadouts.get("Archer"), this);
		this.getServer().getPluginManager().registerEvents(loadouts.get("Magician"), this);
		this.getServer().getPluginManager().registerEvents(loadouts.get("Warrior"), this);
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
		return lobbyLocation;
	}
	
	public boolean isMatchStarted(Player player)
	{
		return startedMatches.contains(playersMatch.get(player));
	}
	
	public boolean isSpawned(Player player)
	{
		return !playerLimbo.contains(player) && playersMatch.containsKey(player);
	}
	
	public void spawnPlayer(Player player)
	{
		player.teleport(getPlayerSpawn(player));
		new BukkitRunnable(){

			@Override
			public void run()
			{
				playerLimbo.remove(player);
			}
			
		}.runTaskLater(this, 1);
	}
	
	public Location getPlayerRoom(Player player)
	{
		Match match = playersMatch.get(player);
		Team team = playersTeam.get(player);
		if(match != null && team != null)
		{
			Location location = playersMatch.get(player).getTeamRoom(team);
			if(location == null)
			{
				player.sendMessage("team spawn is null.");
			}
			return location;
		}
		player.sendMessage("Not in a team or match.");
		return lobbyLocation;
	}
	
	public static void cooldown(ItemStack item)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(item.getAmount() > 1)
				{
					item.setAmount(item.getAmount() - 1);
				}
				
				if(item.getAmount() == 1)
				{
					this.cancel();
				}
			}
			
		}.runTaskTimer(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 1, CaptureTheCloth.TICKS_PER_SECOND * 1);
	}
	
	private void loadCommands()
	{
		CommandEvents commander = new CommandEvents();
		
		getCommand("set-lobby").setExecutor(commander);
		getCommand("create-team").setExecutor(commander);
		getCommand("create-match").setExecutor(commander);
		getCommand("set-team-room").setExecutor(commander);
		getCommand("set-team-spawn").setExecutor(commander);
		getCommand("set-team-flag").setExecutor(commander);
		getCommand("join-match").setExecutor(commander);
		getCommand("leave-match").setExecutor(commander);
		getCommand("start-match").setExecutor(commander);
		getCommand("save-match").setExecutor(commander);
		getCommand("get-team").setExecutor(commander);
		getCommand("link-button-spawn").setExecutor(commander);
		getCommand("link-button-room").setExecutor(commander);
		getCommand("invisible").setExecutor(commander);
		getCommand("archer").setExecutor(commander);
		getCommand("magician").setExecutor(commander);
		getCommand("warrior").setExecutor(commander);
		getCommand("ctc-reload").setExecutor(commander);
	}
	
	private void saveSettings()
	{
		FileConfiguration config = this.getConfig();
		
		config.set("lobby", lobbyLocation);
		
		saveConf(config);
	}
	
	private void loadSettings()
	{
		FileConfiguration config = this.getConfig();
		
		lobbyLocation = (Location) config.get("lobby");
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
	
	public void resetPlayerInventory(Player player)
	{
		player.getInventory().clear();
		player.getInventory().setHelmet(playersTeam.get(player).getBlock());
	}
	
	public boolean sameTeam(Player p1, Player p2)
	{
		if(playersMatch.get(p1) != playersMatch.get(p2))
		{
			return false;
		}
		//Match match = playersMatch.get(p1);
		
		//return match.sameTeam(p1, p2);
		return playersTeam.get(p1) == playersTeam.get(p2) && playersTeam.get(p1) != null;
	}
	
	public boolean isTeamSpawnButton(Location button)
	{
		return buttonsSpawn.containsKey(button);
	}
	
	public boolean isTeamRoomButton(Location button)
	{
		return buttonsRoom.containsKey(button);
	}
	
	public boolean joinMatchByButton(Location button, Player player)
	{
		Tuple<Match, Team> info = buttonsRoom.get(button);
		if(info == null)
		{
			return false;
		}
		String matchName = info.a().getName();
		String teamName = info.b().getName();
		if(joinMatch(matchName, teamName, player))
		{
			playerLimbo.add(player);
			return true;
		}
		return false;
	}
	
	public void setLobby(Player player)
	{
		lobbyLocation = player.getLocation();
		saveSettings();
	}
	
	public boolean setTeamSpawnButton(Player player, Location button)
	{
		Team team = playersTeam.get(player);
		Match match = playersMatch.get(player);
		
		if(match == null || team == null)
		{
			return false;
		}
		
		buttonsSpawn.put(button, new Tuple<Match, Team>(match, team));
		
		return true;
	}
	
	public boolean setTeamRoomButton(Player player, Location button)
	{
		Team team = playersTeam.get(player);
		Match match = playersMatch.get(player);
		
		if(match == null || team == null)
		{
			return false;
		}
		
		buttonsRoom.put(button, new Tuple<Match, Team>(match, team));
		
		return true;
	}
	
	public boolean setTeamRoom(Player player)
	{
		Team team = playersTeam.get(player);
		Match match = playersMatch.get(player);
		
		if(match == null || team == null)
		{
			return false;
		}
		
		match.setTeamRoom(team, player.getLocation());
		return true;
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
				if(game.start())
				{
					startedMatches.add(game);
					return true;
				}
				return false;
			}
		}
		
		return false;
	}
	
	public boolean endMatch(Match match)
	{
		startedMatches.remove(match);
		for(Player player : playersMatch.keySet())
		{
			if(match == playersMatch.get(player))
			{
				leaveMatch(player);
			}
		}
		
		return false;
	}
	
	public void announceStarting(String name, long time)
	{
		for(Match game: matches.values())
		{
			if(game.getName().equals(name))
			{
				game.broadcastMessage(ChatColor.GOLD + "Match starting in " + time + " seconds.");
			}
		}
	}
	
	public boolean joinMatch(String matchName, String teamName, Player player)
	{
		if(playersMatch.containsKey(player))
		{
			player.sendMessage(ChatColor.RED + "Already in match.");
			return false;
		}
		for(Match match: matches.values())
		{
			if(match.getName().equals(matchName))
			{
				if(match.addPlayer(teamName, player))
				{
					playersMatch.put(player, match);
					playersTeam.put(player, match.getTeam(teamName));
					return true;
				}
				else
				{
					player.sendMessage(ChatColor.RED + "failed to join match.");
					return false;
				}
			}
		}
		player.sendMessage(ChatColor.RED + "Match with team doesn't exist.");
		return false;
	}
	
	public boolean leaveMatch(Player player)
	{
		if(playersMatch.get(player) == null)
		{
			System.out.println("match was null.");
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
				player.teleport(getLobbyLocation());
				player.getInventory().clear();
				removePlayerLoadout(player);
				player.sendMessage("Left match.");
				
				playersMatch.remove(player);
				playersTeam.remove(player);
				
				return true;
			}
			player.sendMessage(ChatColor.RED + "Failed to leave match?!");
		}
		return false;
	}
	
	public void removePlayersTeam(Player player)
	{
		playersTeam.remove(player);
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
			config.set("matches."+match.getName()+".teams."+teamName+".room", match.getTeamRoom(teamName));
			config.set("matches."+match.getName()+".teams."+teamName+".spawn", match.getTeamSpawn(teamName));
			config.set("matches."+match.getName()+".teams."+teamName+".flag", match.getTeamFlag(teamName));
		}
		
		for(Location button : buttonsSpawn.keySet())
		{
			Tuple<Match, Team> info = buttonsSpawn.get(button);
			if(info.a().getName().equals(matchName))
			{
				config.set("matches."+match.getName()+".teams."+info.b().getName()+".button.spawn", button);
			}
		}
		
		for(Location button : buttonsRoom.keySet())
		{
			Tuple<Match, Team> info = buttonsRoom.get(button);
			if(info.a().getName().equals(matchName))
			{
				config.set("matches."+match.getName()+".teams."+info.b().getName()+".button.room", button);
			}
		}
		
		saveConf(config);
		
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
						Location room = (Location) teamSection.get(teamKey + ".room");
						Location spawn = (Location) teamSection.get(teamKey + ".spawn");
						Location flag = (Location) teamSection.get(teamKey + ".flag");
						Location spawnButton = (Location) teamSection.get(teamKey + ".button.spawn");
						Location roomButton = (Location) teamSection.get(teamKey + ".button.room");
						if(room != null)
							match.setTeamRoom(team, room);
						match.setTeamSpawn(team, spawn);
						match.setTeamFlag(team, flag);
						buttonsSpawn.put(spawnButton, new Tuple<Match, Team>(match,team));
						buttonsRoom.put(roomButton, new Tuple<Match, Team>(match,team));
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
		
		saveConf(config);
	}
	
	private boolean saveConf(FileConfiguration config)
	{
		try
		{
			File file = new File(getDataFolder(), "config.yml");
			config.save(file);
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
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
	
	public Loadout getLoadout(String name)
	{
		return loadouts.get(name);
	}
	
	public void removePlayerLoadout(Player player)
	{
		Loadout loadout = playersLoadouts.remove(player);
		if(loadout != null)
		{
			loadout.removeLoadout(player);
		}
	}
	
	public boolean setPlayerLoadout(Player player, String name)
	{
		if(loadouts.containsKey(name))
		{
			removePlayerLoadout(player);
			playersLoadouts.put(player, loadouts.get(name));
			loadouts.get(name).setLoadout(player);
			return true;
		}
		
		return false;
	}

	public boolean isPlayerLoadout(Player player, Loadout loadout)
	{
		return playersLoadouts.get(player) == loadout;
	}
}
