package denaro.nick.capturethecloth;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface Loadout extends Listener
{
	public void setLoadout(Player player);
	
	public void removeLoadout(Player player);
}
