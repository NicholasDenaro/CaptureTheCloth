package denaro.nick.capturethecloth;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Magician implements Loadout
{
	public void setLoadout(Player player)
	{
		PlayerInventory inventory = player.getInventory();
		CaptureTheCloth.instance().resetPlayerInventory(player);
		
		inventory.setItem(0, new ItemStack(Material.BLAZE_ROD));
		ItemStack slowArrow = new ItemStack(Material.TIPPED_ARROW);
		PotionMeta meta = (PotionMeta) slowArrow.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, (int) (CaptureTheCloth.TICKS_PER_SECOND * 2), 5), true);
		slowArrow.setItemMeta(meta);
		inventory.setItem(1, slowArrow);
		inventory.setItem(2, new ItemStack(Material.EYE_OF_ENDER));
		inventory.setItem(3, new ItemStack(Material.STICK));
	}
	
	public void removeLoadout(Player player)
	{
		
	}
	
	/*@EventHandler
	public void onArrowLand(ProjectileHitEvent event)
	{
		if(event.getEntityType() == EntityType.TIPPED_ARROW)
		{
			TippedArrow arrow = (TippedArrow) event.getEntity();
			Location location = arrow.getLocation();
			if(location.getBlock().getType() == Material.AIR)
			{
				location.getBlock().setType(Material.WEB);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						location.getBlock().setType(Material.AIR);
					}
					
				}.runTaskLater(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 5);
			}
		}
	}*/
	
	@EventHandler
	public void onRightClickEntity(PlayerInteractAtEntityEvent event)
	{
		if(!CaptureTheCloth.instance().isSpawned(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		if(CaptureTheCloth.instance().isPlayerLoadout(event.getPlayer(), this))
		{
			ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
			if(item.getType() == Material.EYE_OF_ENDER)
			{
				if(item.getAmount() == 1)
				{
					Player player = event.getPlayer();
					Firework firework = (Firework) player.getLocation().getWorld().spawnEntity(event.getRightClicked().getLocation(), EntityType.FIREWORK);
					
					FireworkMeta meta = firework.getFireworkMeta();
					meta.setPower(1);
					firework.setFireworkMeta(meta);
					firework.setPassenger(event.getRightClicked());
					
					item.setAmount(10);
					CaptureTheCloth.cooldown(item);
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onHitByFlameArrow(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow) event.getDamager();
			if(arrow.getFireTicks() > 0)
			{
				Player shooter = (Player) arrow.getShooter();
				float distance = (float) arrow.getLocation().distance(shooter.getLocation());
				arrow.setFireTicks((int) Math.max(arrow.getFireTicks() - distance / 50 * CaptureTheCloth.TICKS_PER_SECOND, CaptureTheCloth.TICKS_PER_SECOND));
				event.setDamage(0);
			}
		}
	}
	
	private boolean touchingSolid(Block block)
	{
		return block.getRelative(BlockFace.NORTH).getType().isSolid()
				|| block.getRelative(BlockFace.SOUTH).getType().isSolid()
				|| block.getRelative(BlockFace.EAST).getType().isSolid()
				|| block.getRelative(BlockFace.WEST).getType().isSolid();
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent event)
	{
		if(!CaptureTheCloth.instance().isSpawned(event.getPlayer()))
		{
			event.setCancelled(true);
			return;
		}
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if(CaptureTheCloth.instance().isPlayerLoadout(event.getPlayer(), this))
			{
				ItemStack item = event.getItem();
				if(item != null)
				{
					if(item.getType() == Material.BLAZE_ROD && item.getAmount() == 1)
					{
						Location location = event.getPlayer().getEyeLocation().clone();
						location.add(location.getDirection().multiply(1.0f));
						location.add(location.getDirection());
						Arrow arrow = CaptureTheCloth.instance().getServer().getWorlds().get(0).spawnArrow(location, location.getDirection(), CaptureTheCloth.TICKS_PER_SECOND * 5, 0);
						arrow.setFireTicks(5);
						arrow.setShooter(event.getPlayer());
						item.setAmount(10);
						CaptureTheCloth.cooldown(item);
					}
					if(item.getType() == Material.TIPPED_ARROW && item.getAmount() == 1)
					{
						PotionMeta  potmeta = (PotionMeta) item.getItemMeta();
						Location location = event.getPlayer().getEyeLocation();
						location.add(location.getDirection().multiply(1.0f));
						float velocity = (float) location.getDirection().multiply(2.0f).dot(location.getDirection().multiply(2.0f));
						TippedArrow arrow = CaptureTheCloth.instance().getServer().getWorlds().get(0).spawnArrow(location, location.getDirection(), velocity, 0, TippedArrow.class);
						arrow.addCustomEffect(potmeta.getCustomEffects().get(0), true);
						arrow.setFallDistance(10000000.0f);
						arrow.setShooter(event.getPlayer());
						item.setAmount(10);
						CaptureTheCloth.cooldown(item);
					}
					if(item.getType() == Material.EYE_OF_ENDER)
					{
						event.setCancelled(true);
					}
					if(item.getType() == Material.STICK && item.getAmount() == 1)
					{
						Location clicked = event.getClickedBlock().getLocation();
						
						if(clicked.getBlock().getType().isSolid())
						{
							Block block = clicked.getBlock();
							if(block.getRelative(BlockFace.DOWN).getType().isSolid())
							{
								ArrayList<Block> above = new ArrayList<Block>();
								Block cur = block.getRelative(BlockFace.UP);
								for(int i = 0; i < 3; i++)
								{
									if(cur.getType() == Material.AIR && !touchingSolid(cur))
									{
										above.add(cur);
									}
									else
									{
										break;
									}
									cur = cur.getRelative(BlockFace.UP);
								}
								if(above.size() > 0)
								{
									new BukkitRunnable()
									{
										@Override
										public void run()
										{
											for(int i = 0; i < above.size(); i++)
											{
												above.get(i).setType(Material.CACTUS);
											}
										}
										
									}.runTaskLater(CaptureTheCloth.instance(), 1);
									
									Material oldType = block.getType();
									MaterialData oldData = block.getState().getData().clone();
									block.setType(Material.SAND);
									new BukkitRunnable()
									{
										@Override
										public void run()
										{
											for(int i = above.size() - 1; i >= 0; i--)
											{
												above.get(i).setType(Material.AIR);
											}
											block.setType(oldType);
											block.getState().setData(oldData);
											block.getState().update();
											
										}
										
									}.runTaskLater(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 5);
									
									item.setAmount(10);
									CaptureTheCloth.cooldown(item);
								}
							}
						}
					}
				}
			}
		}
	}
}
