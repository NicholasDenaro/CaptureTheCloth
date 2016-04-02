package denaro.nick.capturethecloth;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class Warrior implements Listener
{
	public static void setLoadout(Player player)
	{
		PlayerInventory inventory = player.getInventory();
		CaptureTheCloth.instance().resetPlayerInventory(player);
		
		inventory.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		inventory.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		inventory.setBoots(new ItemStack(Material.IRON_BOOTS));
		inventory.setItem(0, new ItemStack(Material.DIAMOND_SWORD));
		
		ItemStack shield = new ItemStack(Material.SHIELD);
		inventory.setItemInOffHand(shield);
		
		CaptureTheCloth.instance().setPlayerLoadout(player, Warrior.class);
	}
	
	@EventHandler//(priority = EventPriority.HIGHEST)
	public void onShieldDefend(EntityDamageByEntityEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			player.sendMessage("Blocking modifier: "+event.getDamage(DamageModifier.BLOCKING));
			if(event.getDamage(DamageModifier.BLOCKING) < 0)
			{
				if(player.isBlocking())
				{
					//new BukkitRunnable()
					//{
						//@Override
						//public void run()
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
						
					//}.runTaskLater(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 2);
				}
				else
				{
					player.sendMessage("Why didn't you block?");
				}
			}
		}
	}
	
	/*@EventHandler
	public void onShield(PlayerInteractEvent event)
	{
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(CaptureTheCloth.instance().isPlayerLoadout(event.getPlayer(), this.getClass()))
			{
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						event.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								event.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
							}
							
						}.runTaskLater(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 5);
					}
					
				}.runTaskLater(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 2);
			}
		}
	}*/
}
