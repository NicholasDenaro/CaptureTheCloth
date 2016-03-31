package denaro.nick.capturethecloth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBanner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Match
{
	private String name;
	private long time;
	private int maxTeamSize;
	private ArrayList<Team> teams;
	private HashMap<Team, Location> spawns;
	private HashMap<Team, Location> flags;
	private HashMap<Team, Location> currentFlags;
	private HashMap<Player, Player> invisiblePlayers;
	private HashMap<Player, Team> heldFlags;
	
	private HashMap<Team, Integer> score;
	
	private boolean started;
	
	public Match(String name, long time, int maxTeamSize)
	{
		this.name = name;
		this.time = time;
		this.maxTeamSize = maxTeamSize;
		invisiblePlayers = new HashMap<Player, Player>();
		score = new HashMap<Team, Integer>();
		teams = new ArrayList<Team>();
		spawns = new HashMap<Team, Location>();
		flags = new HashMap<Team, Location>();
		currentFlags = new HashMap<Team, Location>();
		heldFlags = new HashMap<Player, Team>();
		started = false;
	}
	
	public boolean start()
	{
		if(started)
		{
			return false;
		}
		
		for(Team team : teams)
		{
			replaceFlag(team);
			currentFlags.put(team, flags.get(team));
			score.put(team, 0);
			
			team.sendMessage(ChatColor.GOLD + "Match start!");
		}
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run()
			{
				ArrayList<Team> winners = new ArrayList<Team>();
				int point = 0;
				for(Team team : teams)
				{
					if(score.get(team) > point)
					{
						point = score.get(team);
						winners.clear();
						winners.add(team);
					}
					else if(score.get(team) == point)
					{
						winners.add(team);
					}
				}
				for(Team team : teams)
				{
					team.sendMessage(ChatColor.GOLD + "Match finished.");
					if(winners.size() != 1)
					{
						team.sendMessage(ChatColor.GOLD + "Draw!");
						for(Team winner : winners)
						{
							team.sendMessage(ChatColor.GOLD + "-"+winner.getName());
						}
					}
					else
					{
						team.sendMessage(ChatColor.GOLD + "-"+winners.get(0).getName());
					}
				}
			}
			
		}, time);
		
		started = true;
		return true;
	}
	
	public long getTime()
	{
		return time;
	}
	
	public int getTeamSize()
	{
		return maxTeamSize;
	}
	
	public Location getTeamSpawn(Team team)
	{
		return spawns.get(team);
	}
	
	public Location getTeamSpawn(String teamName)
	{
		for(Team team : teams)
		{
			if(team.getName().equals(teamName))
			{
				return spawns.get(team);
			}
		}
		
		return null;
	}
	
	public void setTeamSpawn(Team team, Location spawn)
	{
		spawns.put(team, spawn);
	}
	
	public void setTeamFlag(Team team, Location flag)
	{
		flags.put(team, flag);
	}
	
	public Location getTeamFlag(String teamName)
	{
		for(Team team : teams)
		{
			if(team.getName().equals(teamName))
			{
				return flags.get(team);
			}
		}
		
		return null;
	}
	
	public String[] getTeamNames()
	{
		String[] names = new String[teams.size()];
		for(int i = 0; i < teams.size(); i++)
		{
			names[i] = teams.get(i).getName();
		}
		
		return names;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean addTeam(Team team)
	{
		if(teams.contains(team))
		{
			return false;
		}
		
		teams.add(team);
		return true;
	}
	
	public void setLocation(String teamName, Location location)
	{
		for(Team team : teams)
		{
			if(team.getName().equals(teamName))
			{
				spawns.put(team, location);
			}
		}
	}
	
	public boolean sameTeam(Player p1, Player p2)
	{
		for(Team team : teams)
		{
			if(team.hasPlayer(p1) && team.hasPlayer(p2))
			{
				return true;
			}
		}
		return false;
	}
	
	public Team getTeam(Player player)
	{
		for(Team team : teams)
		{
			if(team.hasPlayer(player))
			{
				return team;
			}
		}
		return null;
	}
	
	public boolean addPlayer(String teamName, Player player)
	{
		for(Team team : teams)
		{
			if(teamName.equals(team.getName()))
			{
				if(team.getNumberOfPlayers() < maxTeamSize)
				{
					team.addPlayer(player);
					System.out.println("Added " + player.getName() + " to team " + team.getName() + ".");
					player.sendMessage(ChatColor.AQUA + "Joined team " + teamName);
					return true;
				}
				else
				{
					System.out.println(team.getName() + " is full!");
					System.out.println(team.getNumberOfPlayers() + " : " + maxTeamSize);
					player.sendMessage(ChatColor.RED + "Failed to join team " + teamName + ". Team is full.");
					return false;
				}
			}
		}
		
		System.out.println("Team " + teamName +  " does not exist in this match.");
		
		return false;
	}
	
	public boolean removePlayer(Player player)
	{
		for(Team team : teams)
		{
			if(team.hasPlayer(player))
			{
				return team.removePlayer(player);
			}
		}
		return true;
	}
	
	public void updateVisibility(Player player)
	{
		for(Team team : teams)
		{
			if(!team.hasPlayer(player))
			{
				team.updateVisibility(player, invisiblePlayers.keySet());
			}
		}
		
	}
	
	public void makeInvisible(Player player)
	{
		invisiblePlayers.put(player,player);
		for(Team team : teams)
		{
			if(!team.hasPlayer(player))
			{
				team.makeInvisible(player);
			}
		}
	}
	
	public void makeVisible(Player player)
	{
		invisiblePlayers.remove(player);
		for(Team team : teams)
		{
			if(!team.hasPlayer(player))
			{
				team.makeVisible(player);
			}
		}
	}

	public void pickupFlag(Player player)
	{
		for(Team team : teams)
		{
			Location flag = currentFlags.get(team);
			if(flag != null)
			{
				if(flag.distance(player.getLocation()) < 1)
				{
					if(!team.hasPlayer(player))
					{
						if(heldFlags.get(player) == null)
						{
							player.getInventory().setHelmet(new ItemStack(team.getBanner()));
							flag.getBlock().setType(Material.AIR);
							heldFlags.put(player, team);
							currentFlags.remove(team);
							
							player.getLocation().getWorld().playEffect(player.getLocation(), Effect.CLICK1, 1);
						}
					}
					else
					{
						if(flag == flags.get(team) && heldFlags.get(player) != null)
						{
							scoreFlag(player);
						}
					}
				}
			}
		}
	}
	
	public void scoreFlag(Player player)
	{
		Team team = CaptureTheCloth.instance().getTeam(player);
		score.put(team, score.get(team) + 1);
		
		player.getInventory().setHelmet(new ItemStack(team.getBlock()));
		
		Team otherTeam = heldFlags.get(player);
		replaceFlag(otherTeam);
		heldFlags.remove(player);
		currentFlags.put(otherTeam, flags.get(otherTeam));
		
		Firework firework = (Firework) player.getLocation().getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
		
		FireworkMeta meta = firework.getFireworkMeta();
		FireworkEffect.Builder builder = FireworkEffect.builder();
		builder.trail(true);
		builder.flicker(true);
		builder.withColor(Color.WHITE);
		meta.addEffect(builder.build());
		meta.setPower(3);
		firework.setFireworkMeta(meta);
	}
	
	public void dropFlag(Player player)
	{
		if(heldFlags.get(player) != null)
		{
			Team otherTeam = heldFlags.get(player);
			replaceFlag(otherTeam, player.getLocation());
			heldFlags.remove(player);
			currentFlags.put(otherTeam, player.getLocation());
			
			Team team = CaptureTheCloth.instance().getTeam(player);
			player.getInventory().setHelmet(new ItemStack(team.getBlock()));
		}
	}
	
	public void replaceFlag(Team team)
	{
		Location location = flags.get(team);
		replaceFlag(team, location);
	}
	
	public void replaceFlag(Team team, Location location)
	{
		
		World world = CaptureTheCloth.instance().getServer().getWorlds().get(0);
		Block block = world.getBlockAt(location);
		
		block.setType(Material.STANDING_BANNER);
		
		new BukkitRunnable(){

			@Override
			public void run()
			{
				Block block = world.getBlockAt(location);
				BlockState state = block.getState();
				CraftBanner blockmeta = (CraftBanner) state;
				
				ItemStack banner = team.getBanner();
				ItemMeta banstate = banner.getItemMeta();
				BannerMeta itemmeta = (BannerMeta) banstate;
				
				blockmeta.setBaseColor(itemmeta.getBaseColor());
				blockmeta.setPatterns(itemmeta.getPatterns());
				state.update();
			}
			
		}.runTask(CaptureTheCloth.instance());
	}
}
