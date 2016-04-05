package denaro.nick.capturethecloth;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Warrior implements Loadout
{
	public static final int BULLDOZE_SLOT = 1;
	
	public void setLoadout(Player player)
	{
		PlayerInventory inventory = player.getInventory();
		CaptureTheCloth.instance().resetPlayerInventory(player);
		
		inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
		inventory.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
		
		inventory.setItem(1, new ItemStack(Material.SUGAR));
		
		ItemStack shield = new ItemStack(Material.SHIELD);
		inventory.setItemInOffHand(shield);
	}
	
	public void removeLoadout(Player player)
	{
		
	}
	
	@EventHandler
	public void onPlayerSprint(PlayerToggleSprintEvent event)
	{
		if(!CaptureTheCloth.instance().isSpawned(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		if(event.isSprinting())
		{
			Player player = (Player) event.getPlayer();
			if(CaptureTheCloth.instance().isPlayerLoadout(player, this))
			{
				ItemStack sprint = player.getInventory().getItem(BULLDOZE_SLOT);
				if(sprint.getAmount() == 1)
				{
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (CaptureTheCloth.TICKS_PER_SECOND * 0.25), 10));
					sprint.setAmount(11);
					CaptureTheCloth.cooldown(sprint);
				}
			}
		}
	}
	
	@EventHandler//(priority = EventPriority.HIGHEST)
	public void onShieldDefend(EntityDamageByEntityEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			if(!CaptureTheCloth.instance().isSpawned(player))
			{
				event.setCancelled(true);
				return;
			}
			if(CaptureTheCloth.instance().isPlayerLoadout(player, this))
			{
				if(event.getDamage(DamageModifier.BLOCKING) < 0)
				{
					if(player.isBlocking())
					{
						player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
							}
							
						}.runTaskLater(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 3);
					}
				}
			}
		}
	}
}
