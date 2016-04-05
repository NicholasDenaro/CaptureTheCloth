package denaro.nick.capturethecloth;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class CommandEvents implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		Player player = null;
		if(sender instanceof Player)
			player = (Player) sender;
		if(sender.isOp())
		{
			if("ctc-reload".equals(command.getName()))
			{
				player.sendMessage(ChatColor.GRAY + "This command is disabled.");
				
				if(true)return true;
				
				PluginLoader loader = CaptureTheCloth.instance().getPluginLoader();
				loader.disablePlugin(CaptureTheCloth.instance());
				try
				{
					Plugin plugin = loader.loadPlugin(new File("plugins/CaptureTheCloth.jar"));
					loader.enablePlugin(plugin);
				}
				catch(UnknownDependencyException | InvalidPluginException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if("create-team".equals(command.getName()))
			{
				if(args.length == 1)
				{
					if(player !=null)
					{
						ItemStack block = player.getInventory().getItemInMainHand();
						ItemStack banner = player.getInventory().getItemInOffHand();
						if(block == null)
						{
							player.sendMessage(ChatColor.RED + "Need block in main hand.");
							return true;
						}
						if(banner == null)
						{
							player.sendMessage(ChatColor.RED + "Need banner in off hand.");
							return true;
						}
						
						if(!block.getType().isBlock())
						{
							sender.sendMessage(ChatColor.RED + "Main hand must be a block.");
							return true;
						}
						else 
						{
							if(banner.getType() != Material.BANNER)
							{
								sender.sendMessage(ChatColor.RED + "Off hand must be a banner.");
							}
							else if(CaptureTheCloth.instance().createTeam(args[0], block, banner))
							{
								sender.sendMessage(ChatColor.GREEN + "Team "+args[0]+" created.");
							}
							else
							{
								sender.sendMessage(ChatColor.YELLOW + "Team already exists with this name.");
							}
						}
					}
					else
					{
						sender.sendMessage("Only players may use this command.");
					}
				}
				else
				{
					sender.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
			}
			else if("set-team-room".equals(command.getName()))
			{
				if(CaptureTheCloth.instance().setTeamRoom(player))
				{
					sender.sendMessage(ChatColor.YELLOW + "Spawn set.");
				}
				else
				{
					sender.sendMessage(ChatColor.YELLOW + "Not in match/team.");
				}
			}
			else if("set-team-spawn".equals(command.getName()))
			{
				if(CaptureTheCloth.instance().setTeamSpawn(player))
				{
					sender.sendMessage(ChatColor.YELLOW + "Spawn set.");
				}
				else
				{
					sender.sendMessage(ChatColor.YELLOW + "Not in match/team.");
				}
			}
			else if("set-team-flag".equals(command.getName()))
			{
				if(CaptureTheCloth.instance().setTeamFlag(player))
				{
					sender.sendMessage(ChatColor.YELLOW + "Flag set.");
				}
				else
				{
					sender.sendMessage(ChatColor.YELLOW + "Not in match/team.");
				}
			}
			else if("get-team".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				}
				else
				{
					Match match = CaptureTheCloth.instance().getMatch(player);
					if(match == null)
					{
						player.sendMessage(ChatColor.GOLD + "Not in a match.");
						return true;
					}
					//Team team = match.getTeam(player);
					Team playerTeam = CaptureTheCloth.instance().getTeam(player);
					player.sendMessage(ChatColor.GOLD + playerTeam.getName());
				}
			}
			else if("invisible".equals(command.getName()))
			{
				if(true)
					return false;
				if(args.length != 1)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
				else
				{
					Match match = CaptureTheCloth.instance().getMatch(player);
					if(match != null)
					{
						if("on".equals(args[0]))
						{
							match.makeInvisible(player);
						}
						else if("off".equals(args[0]))
						{
							match.makeVisible(player);
						}
						else
						{
							player.sendMessage(ChatColor.RED + "Incorrect argument.");
							return false;
						}
					}
				}
			}
			else if("archer".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				}
				else
				{
					Match match = CaptureTheCloth.instance().getMatch(player);
					if(match == null)
					{
						player.sendMessage(ChatColor.GOLD + "Not in a match.");
						return true;
					}
					CaptureTheCloth.instance().setPlayerLoadout(player, "Archer");
				}
			}else if("magician".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				}
				else
				{
					Match match = CaptureTheCloth.instance().getMatch(player);
					if(match == null)
					{
						player.sendMessage(ChatColor.GOLD + "Not in a match.");
						return true;
					}
					CaptureTheCloth.instance().setPlayerLoadout(player, "Magician");
				}
			}
			else if("warrior".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
				}
				else
				{
					Match match = CaptureTheCloth.instance().getMatch(player);
					if(match == null)
					{
						player.sendMessage(ChatColor.GOLD + "Not in a match.");
						return true;
					}
					CaptureTheCloth.instance().setPlayerLoadout(player, "Warrior");
				}
			}
			else if("create-match".equals(command.getName()))
			{
				if(args.length < 5)
				{
					player.sendMessage(ChatColor.RED + "Not enough arguments.");
				}
				else
				{
					String name = args[0];
					long time = new Long(args[1]) * 1000;
					int teamSize = new Integer(args[2]);
					String[] teams = new String[args.length - 3];
					
					for(int i = 0; i < teams.length; i++)
					{
						teams[i] = args[3 + i];
					}
					
					if(CaptureTheCloth.instance().createMatch(name, time, teamSize, teams))
					{
						player.sendMessage(ChatColor.GREEN + "Created match " + name + ".");
					}
					else
					{
						player.sendMessage(ChatColor.RED + "Failed to created match.");
					}
				}
			}
			else if("join-match".equals(command.getName()))
			{
				if(args.length != 2)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
				else
				{
					if(CaptureTheCloth.instance().getMatch(player) != null)
					{
						player.sendMessage(ChatColor.RED + "Already in match.");
					}
					else if(CaptureTheCloth.instance().joinMatch(args[0], args[1], player))
					{
						player.sendMessage(ChatColor.GREEN + "Join match!");
					}
					else
					{
						player.sendMessage(ChatColor.RED + "Failed to join match.");
					}
				}
			}
			else if("leave-match".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
				else
				{
					if(CaptureTheCloth.instance().leaveMatch(player))
					{
						player.sendMessage(ChatColor.GREEN + "Match left.");
					}
					else
					{
						player.sendMessage(ChatColor.RED + "Failed to leave match.");
					}
				}
			}
			else if("start-match".equals(command.getName()))
			{
				if(args.length != 1)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
				else
				{
					Player pl = player;
					long time = CaptureTheCloth.TICKS_PER_SECOND * 20;
					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							if(CaptureTheCloth.instance().startMatch(args[0]))
							{
								pl.sendMessage(ChatColor.GREEN + "Match started.");
							}
							else
							{
								pl.sendMessage(ChatColor.RED + "Failed to start match (it is likely started!).");
							}
						}
						
					}.runTaskLater(CaptureTheCloth.instance(), time);
					CaptureTheCloth.instance().getServer().broadcastMessage(ChatColor.GOLD + args[0] + " is starting in " + time / CaptureTheCloth.TICKS_PER_SECOND + " seconds!");
					//CaptureTheCloth.instance().announceStarting(args[0], time / CaptureTheCloth.TICKS_PER_SECOND);
				}
			}
			else if("save-match".equals(command.getName()))
			{
				if(args.length != 1)
				{
					player.sendMessage(ChatColor.RED + "Incorrect number of arguments.");
					return false;
				}
				else
				{
					if(CaptureTheCloth.instance().saveMatch(args[0]))
					{
						player.sendMessage(ChatColor.GREEN + "Match saved.");
					}
					else
					{
						player.sendMessage(ChatColor.RED + "Failed to save match.");
					}
				}
			}
			else if("set-lobby".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "This command takes no arguments.");
					return false;
				}
				else
				{
					CaptureTheCloth.instance().setLobby(player);
					player.sendMessage(ChatColor.GREEN + "Lobby set.");
				}
			}
			else if("link-button-spawn".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "This command takes no arguments.");
					return false;
				}
				else
				{
					Set<Material> mats = new HashSet<Material>();
					mats.add(Material.WOOD_BUTTON);
					Block button = player.getTargetBlock(mats, 1);
					if(button == null)
					{
						return false;
					}
					else
					{
						if(CaptureTheCloth.instance().setTeamSpawnButton(player, button.getLocation()))
						{
							player.sendMessage(ChatColor.GREEN + "Linked button to team: "+CaptureTheCloth.instance().getTeam(player));
						}
						else
						{
							player.sendMessage(ChatColor.RED + "Failed to link button.");
						}
					}
				}
			}
			else if("link-button-room".equals(command.getName()))
			{
				if(args.length != 0)
				{
					player.sendMessage(ChatColor.RED + "This command takes no arguments.");
					return false;
				}
				else
				{
					Block button = player.getTargetBlock((Set<Material>)null, 5);
					if(button == null)
					{
						player.sendMessage(ChatColor.RED + "Found no button.");
						return false;
					}
					else
					{
						if(CaptureTheCloth.instance().setTeamRoomButton(player, button.getLocation()))
						{
							player.sendMessage(ChatColor.GREEN + "Linked button to team: "+CaptureTheCloth.instance().getTeam(player));
						}
						else
						{
							player.sendMessage(ChatColor.RED + "Failed to link button.");
						}
					}
				}
			}
		}
		else //non-op
		{
		}
		return true;
	}
}
