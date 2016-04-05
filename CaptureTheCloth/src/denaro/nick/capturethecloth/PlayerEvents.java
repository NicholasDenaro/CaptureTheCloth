package denaro.nick.capturethecloth;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.material.Door;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerEvents implements Listener
{
	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		if(CaptureTheCloth.instance().getLobbyLocation() != null)
		{
			player.teleport(CaptureTheCloth.instance().getLobbyLocation());
		}
	}
	
	@EventHandler
	public void onPlayerDiconnect(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		CaptureTheCloth.instance().leaveMatch(player);
		if(CaptureTheCloth.instance().getLobbyLocation() != null)
		{
			player.getInventory().clear();
			player.teleport(CaptureTheCloth.instance().getLobbyLocation());
		}
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event)
	{
		DamageCause cause = event.getCause();
		Entity defender = event.getEntity();
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event)
	{
		DamageCause cause = event.getCause();
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player)
		{
			Player attacker = (Player) event.getDamager();
			Player defender = (Player) event.getEntity();
			if(CaptureTheCloth.instance().sameTeam(attacker,defender))
			{
				event.setCancelled(true);
			}
			else
			{
				if(defender.getHealth() <= 0)
				{
					attacker.setLevel(attacker.getLevel() + 1);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInventoryClick(InventoryClickEvent event)
	{
		if(event.getWhoClicked().getGameMode() != GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerSwapHand(PlayerSwapHandItemsEvent event)
	{
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPickupEvent(PlayerPickupItemEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onDropEvent(PlayerDropItemEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onOpenDoor(PlayerInteractEvent event)
	{
		if(event.getClickedBlock().getState().getData() instanceof Door)
		{
			event.setCancelled(!CaptureTheCloth.instance().isSpawned(event.getPlayer()));
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			if(event.getAction() == Action.PHYSICAL)
			{
				event.getPlayer().sendMessage(ChatColor.DARK_GRAY + "InteractEvent: Action == PHYSICAL");
				event.setCancelled(true);
			}
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if(CaptureTheCloth.instance().isTeamRoomButton(event.getClickedBlock().getLocation()))
				{
					if(CaptureTheCloth.instance().joinMatchByButton(event.getClickedBlock().getLocation(), event.getPlayer()))
					{
						event.getPlayer().sendMessage(ChatColor.GREEN + "Joined match.");
					}
					else
					{
						event.getPlayer().sendMessage(ChatColor.RED + "Failed to join match.");
					}
				}
				else if(CaptureTheCloth.instance().isTeamSpawnButton(event.getClickedBlock().getLocation()))
				{
					if(CaptureTheCloth.instance().isMatchStarted(event.getPlayer()))
					{
						CaptureTheCloth.instance().spawnPlayer(event.getPlayer());
						event.setCancelled(true);
						//TODO: make player invincible for ~ 2 seconds
					}
				}
			}
			
			if(event.getAction() == Action.LEFT_CLICK_BLOCK)
			{
				if(event.getClickedBlock().getType() == Material.WALL_SIGN)
				{
					Sign sign = (Sign) event.getClickedBlock().getState();
					String loadoutName = sign.getLine(1);
					CaptureTheCloth.instance().setPlayerLoadout(event.getPlayer(), loadoutName);
				}
				event.setCancelled(true);
				if(event.getClickedBlock().getType() == Material.DOUBLE_PLANT)
				{
					new BukkitRunnable()
					{

						@Override
						public void run()
						{
							Block block = event.getClickedBlock();
							//event.getPlayer().sendMessage(ChatColor.AQUA + "" + event.getClickedBlock().getType());
							//event.getPlayer().sendMessage(ChatColor.AQUA + "" + event.getClickedBlock().getData());
							Location loc = block.getLocation();
							Location under = loc.clone();
							under.setY(under.getY() - 1);
							Location over = loc.clone();
							over.setY(over.getY() + 1);
							if(under.getBlock().getType() == Material.GRASS)
							{
								block.setType(Material.DOUBLE_PLANT);
								block.setData((byte) 2);
								over.getBlock().setType(Material.DOUBLE_PLANT);
								over.getBlock().setData((byte) 10);
							}
							else
							{
								under.getBlock().setType(Material.DOUBLE_PLANT);
								under.getBlock().setData((byte) 2);
								block.setType(Material.DOUBLE_PLANT);
								block.setData((byte) 10);
							}
						}
						
					}.runTaskLater(CaptureTheCloth.instance(), 1);
					
				}
				else
				{
					//event.getPlayer().sendMessage(ChatColor.AQUA + "" + event.getClickedBlock().getType());
				}
			}
			//event.setCancelled(true);
		}
	}
	
	@EventHandler( priority = EventPriority.LOWEST )
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Match match = CaptureTheCloth.instance().getMatch(player);
		if(match != null)
		{
			if(!player.isDead())
			{
				if(event.getTo().getBlock().getType() == Material.LONG_GRASS)
				{
					match.makeInvisible(player);
				}
				else
				{
					match.makeVisible(player);
				}
				match.updateVisibility(player);
				match.pickupFlag(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerHeal(EntityRegainHealthEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			if(event.getRegainReason() == RegainReason.SATIATED)
			{
				//event.setAmount(0);
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerFoodChange(FoodLevelChangeEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Location spawn = CaptureTheCloth.instance().getPlayerRoom(event.getPlayer());
		if(spawn == null)
		{
			event.getPlayer().sendMessage("null location.");
		}
		else
		{
			event.setRespawnLocation(spawn);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		event.setKeepInventory(true);
		event.setKeepLevel(true);
		event.setDroppedExp(0);
		
		Player player = event.getEntity();
		
		Match match = CaptureTheCloth.instance().getMatch(player);
		if(match != null)
		{
			match.dropFlag(player);
		}
	}
}
