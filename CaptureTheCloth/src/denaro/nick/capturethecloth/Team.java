package denaro.nick.capturethecloth;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Team
{
	private String name;
	private ArrayList<Player> players;
	private ItemStack block;
	private ItemStack banner;
	
	public Team(String name, ItemStack blockdata, ItemStack bannerdata)
	{
		this.name = name;
		this.block = blockdata;
		this.banner = bannerdata;
		players = new ArrayList<Player>();
	}
	
	public int getNumberOfPlayers()
	{
		return players.size();
	}
	
	public boolean hasPlayer(Player player)
	{
		return players.contains(player);
	}
	
	public void addPlayer(Player player)
	{
		players.add(player);
		PlayerInventory inv = player.getInventory();
		ItemStack stack = new ItemStack(block);
		stack.setAmount(1);
		inv.setHelmet(stack);
		
		//stack = new ItemStack(banner);
		//stack.setAmount(1);
		//inv.setItemInOffHand(stack);
	}
	
	public boolean removePlayer(Player player)
	{
		return players.remove(player);
	}
	
	public String getName()
	{
		return name;
	}
	
	public ItemStack getBlock()
	{
		return block;
	}
	
	public ItemStack getBanner()
	{
		return new ItemStack(banner);
	}
	
	public void makeInvisible(Player player)
	{
		for(Player p : players)
		{
			p.hidePlayer(player);
		}
	}
	
	public void makeVisible(Player player)
	{
		for(Player p : players)
		{
			p.showPlayer(player);
		}
	}
	
	public void updateVisibility(Player player, Set<Player> invisiblePlayers)
	{
		for(Player p : players)
		{
			if(p.getLocation().distance(player.getLocation()) < 3)
			{
				p.showPlayer(player);
				player.showPlayer(p);
			}
			else
			{
				if(invisiblePlayers.contains(player))
				{
					p.hidePlayer(player);
				}
				if(invisiblePlayers.contains(p))
				{
					player.hidePlayer(p);
				}
			}
		}
	}
	
	public void sendMessage(String message)
	{
		for(Player player : players)
		{
			player.sendMessage(message);
		}
	}
	
	public Team copy()
	{
		return new Team(name, block, banner);
	}
}
