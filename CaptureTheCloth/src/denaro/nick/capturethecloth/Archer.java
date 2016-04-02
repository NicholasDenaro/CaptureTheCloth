package denaro.nick.capturethecloth;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Vine;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Archer implements Listener
{
	public static final int MAX_ARROW_RAIN_ARROWS = 20;
	
	public static final int BUFF_ARROW_SLOT = 3;
	
	private HashMap<Player, BukkitRunnable> bowDraw = new HashMap<Player, BukkitRunnable>();
	private HashMap<Player, BukkitRunnable> reloadBuffShot = new HashMap<Player, BukkitRunnable>();
	
	public static void setLoadout(Player player)
	{
		PlayerInventory inventory = player.getInventory();
		CaptureTheCloth.instance().resetPlayerInventory(player);
		
		inventory.setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
		inventory.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		boots.addEnchantment(Enchantment.PROTECTION_FALL, 4);
		inventory.setBoots(boots);
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		inventory.setItem(0, bow);
		

		bow = new ItemStack(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
		bow.addUnsafeEnchantment(Enchantment.LUCK, 1);
		inventory.setItem(1, bow);
		
		inventory.setItemInOffHand(new ItemStack(Material.ARROW));
		
		inventory.setItem(2,  new ItemStack(Material.WOOD_AXE));
		
		ItemStack poisonArrow = new ItemStack(Material.TIPPED_ARROW);
		PotionMeta meta = (PotionMeta) poisonArrow.getItemMeta();
		meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, (int) (CaptureTheCloth.TICKS_PER_SECOND * 2), 1), true);
		poisonArrow.setItemMeta(meta);
		inventory.setItem(BUFF_ARROW_SLOT, poisonArrow);
		
		CaptureTheCloth.instance().setPlayerLoadout(player, Archer.class);
	}
	
	@EventHandler
	public void onChangeHolding(PlayerItemHeldEvent event)
	{
		if(bowDraw.containsKey(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onTippedArrowHit(ProjectileHitEvent event)
	{
		if(event.getEntityType() == EntityType.TIPPED_ARROW)
		{
			event.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onArcherClimb(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		if(CaptureTheCloth.instance().isPlayerLoadout(player, this.getClass()))
		{
			if(event.getFrom().getBlock().getType() == Material.VINE)
			{
				event.getFrom().getBlock().setType(Material.AIR);
			}
			Block block = event.getTo().getBlock();
			if(block.isEmpty())
			{
				Location loc = event.getTo().clone();
				loc.setPitch(0.0f);
				Vector blockDirection = loc.getDirection().toBlockVector();
				Block facingBlock = player.getLocation().add(blockDirection).getBlock();
				
				if(facingBlock.getLocation().distance(block.getLocation()) <= 1)
				{
					if(facingBlock.getType().isSolid() && facingBlock.getState().getLightLevel() == 0)
					{
						//player.sendMessage("light level: " + facingBlock.getState().getLightLevel());
						BlockFace face = block.getFace(facingBlock);
						if(face != null)
						{
							//face = face.getOppositeFace();
							block.setType(Material.VINE);
							byte data=0;
							switch(face)
							{
								case SOUTH:
									data = 1;
								break;
								case WEST:
									data = 2;
								break;
								case NORTH:
									data = 4;
								break;
								case EAST:
									data = 8;
								break;
								default:
									data = 0;
							}
							block.setData(data);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onDrawBow(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack hand = player.getInventory().getItemInMainHand();
		if(hand.getType() == Material.BOW && hand.getEnchantmentLevel(Enchantment.LUCK) == 1)
		{
			if(hand.getAmount() == 1)
			{
				if(bowDraw.get(player) == null)
				{
					ItemStack arrows = player.getInventory().getItemInOffHand();
					BukkitRunnable runnable = new BukkitRunnable(){
		
						@Override
						public void run()
						{
							if(arrows.getAmount() < MAX_ARROW_RAIN_ARROWS)
							{
								arrows.setAmount(arrows.getAmount() + 1);
							}
						}
						
					};
					runnable.runTaskTimer(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 1, (long) (CaptureTheCloth.TICKS_PER_SECOND * 0.3));
					bowDraw.put(player, runnable);
				}
			}
			else
			{
				event.setCancelled(true);
			}
		}
		else if(hand.getType() == Material.BOW)
		{
			bowDraw.put(player, null);
			if(!reloadBuffShot.containsKey(player))
			{
				BukkitRunnable runnable = new BukkitRunnable(){

					@Override
					public void run()
					{
						if(player.getInventory().getItem(BUFF_ARROW_SLOT).getAmount() > 1)
						{
							player.getInventory().getItem(BUFF_ARROW_SLOT).setAmount(player.getInventory().getItem(BUFF_ARROW_SLOT).getAmount() - 1);
						}
						if(player.getInventory().getItem(BUFF_ARROW_SLOT).getAmount() == 1)
						{
							this.cancel();
							reloadBuffShot.remove(player);
						}
					}
					
				};
				reloadBuffShot.put(player, runnable);
				player.getInventory().setItemInOffHand(player.getInventory().getItem(BUFF_ARROW_SLOT).clone());
			}
		}
	}
	
	@EventHandler
	public void onShootBow(EntityShootBowEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			ItemStack bow = event.getBow();
			Vector up = new Vector();
			up.setY(1);
			float spread = 10.0f;
			if(bow.getItemMeta().getEnchantLevel(Enchantment.LUCK) == 1)
			{
				if(bow.getAmount() == 1)
				{
					Projectile arrow = (Projectile) event.getProjectile();
					Vector direction = arrow.getVelocity().normalize();
					Vector vect = arrow.getVelocity();
					Location location = arrow.getLocation();
					spread -= 9.0f * Math.abs(up.dot(direction));
					int numShoot = player.getInventory().getItemInOffHand().getAmount();
					
					float speed = (float) Math.sqrt(vect.dot(vect));
					
					//speed = speed * (1.0f - 0.7f * (float)Math.abs(up.dot(direction)));
					
					arrow.setVelocity(direction.multiply(speed));
					
					//player.sendMessage(ChatColor.GOLD + "Arrow speed: " + speed); 
					
					for(int i = 0; i < numShoot; i ++)
					{
						CaptureTheCloth.instance().getServer().getWorlds().get(0).spawnArrow(location.add(vect), direction, speed, spread);
					}
					player.getInventory().getItemInOffHand().setAmount(1);
					bowDraw.get(player).cancel();
					bowDraw.remove(player);
					
					bow.setAmount(numShoot * 2);
					CaptureTheCloth.cooldown(bow);
				}
			}
			else 
			{
				if(event.getProjectile().getType() == EntityType.TIPPED_ARROW)
				{
					if(reloadBuffShot.containsKey(player))
					{
						player.getInventory().getItem(BUFF_ARROW_SLOT).setAmount(11);
						reloadBuffShot.get(player).runTaskTimer(CaptureTheCloth.instance(), CaptureTheCloth.TICKS_PER_SECOND * 1, CaptureTheCloth.TICKS_PER_SECOND * 1);
						player.getInventory().setItemInOffHand(new ItemStack(Material.ARROW));
					}
				}
			}
			
			bowDraw.remove(player);
		}
	}
}
