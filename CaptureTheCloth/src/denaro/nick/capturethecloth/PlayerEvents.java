package denaro.nick.capturethecloth;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.block.CraftBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import net.minecraft.server.v1_9_R1.BlockBanner;

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
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
		{
			if(event.hasBlock())
			{
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler( priority = EventPriority.LOWEST )
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		Match match = CaptureTheCloth.instance().getMatch(player);
		if(match != null)
		{
			match.updateVisibility(player);
			match.pickupFlag(player);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Location spawn = CaptureTheCloth.instance().getPlayerSpawn(event.getPlayer());
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
		
		Player player = event.getEntity();
		
		Match match = CaptureTheCloth.instance().getMatch(player);
		if(match != null)
		{
			match.dropFlag(player);
		}
	}
}
