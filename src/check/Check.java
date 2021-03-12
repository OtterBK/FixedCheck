package check;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Check {

	private ItemStack checkItem;
	private double money;
	
	public Check(Material checkMaterial, double money) {
		
		ItemStack item = new ItemStack(checkMaterial, 1);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("¡×f[ ¡×b"+(int)money+"¿ø ¡×f]");
		item.setItemMeta(meta);
		
		this.checkItem = item;
		this.money = money;
		
	}
	
	public double getMoney() {
		return this.money;
	}
	
	public ItemStack getItem() {
		return this.checkItem;
	}
	
}
