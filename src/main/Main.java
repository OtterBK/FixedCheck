package main;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import check.Check;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener{

	public static String mainMS = "��f[ ��e��ǥ ��f] ";
	
	private static Economy econ = null;
	
	private ArrayList<Check> checkList = new ArrayList<Check>();
	
	public void onEnable() {
		if (!setupEconomy() ) {
            getServer().getLogger().info(mainMS+"Vault �÷������� ã�� �� �����ϴ�! �÷������� ��Ȱ��ȭ�մϴ�...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		initChecks(); //��ǥ�� �ۼ�
		
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getLogger().info("��ǥ �÷����� �ε� �Ϸ�");
	}
	
	private void initChecks() {
		checkList.add(new Check(Material.IRON_INGOT, 1000));
		checkList.add(new Check(Material.GOLD_INGOT, 5000));
		checkList.add(new Check(Material.DIAMOND, 10000));
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			getServer().getLogger().info("xxx");
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}
	
	private void giveMoney(UUID uuid, double money) {
		OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
		econ.depositPlayer(offPlayer, money);
		if(offPlayer.isOnline()) {
			Player p = offPlayer.getPlayer();
			p.sendMessage(mainMS+"��b"+money+"����f�� ������ϴ�.");
		}
	}
	
	private boolean takeMoney(UUID uuid, double money) {
		OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
		if(econ.has(offPlayer, money)) {
			econ.withdrawPlayer(offPlayer, money);
			return true;
		} else {
			return false;
		}
	}
	
	private void showHelpMessage(Player p) {
		p.sendMessage(" ");
		p.sendMessage(mainMS+"��a/��ǥ ��ȯ ��6<1000/5000/10000>");
		p.sendMessage(" ");
	}
	
	private boolean isCanAddItem(Player p, ItemStack item) {
		if(p.getInventory().firstEmpty() == -1) {
			
			for(ItemStack tmpItem : p.getInventory().getContents()) {
				if(tmpItem != null) {
					if(compareItem(item, tmpItem)) {
						if(tmpItem.getAmount() != 64) {
							return true;
						}
					}
				}
			}
			
			return false;
		} else {
			return true;
		}
	}
	
	public boolean takeItem(Player p, ItemStack item, int amt) {
		int tamt = amt;
		for (int i = 0; i < p.getInventory().getSize(); i++) {
			if (tamt > 0) {
				ItemStack pitem = p.getInventory().getItem(i);
				if (pitem != null && pitem.equals(item)) {
					tamt -= pitem.getAmount();
					if (tamt <= 0) {
						removeItem(p, item, amt);
						return true;
					}
				}
			}
		}

		return false;
	}

	public void removeItem(Player p, ItemStack item, int amt) { //������ ������ �����ϸ� ������ ��ü  ������ ��ü
		for (int i = 0; i < p.getInventory().getSize(); i++) {
			if (amt > 0) {
				ItemStack pitem = p.getInventory().getItem(i);
				if (pitem != null && pitem.equals(item)) {
					if (pitem.getAmount() >= amt) {
						int itemamt = pitem.getAmount() - amt;
						pitem.setAmount(itemamt);
						p.getInventory().setItem(i, amt > 0 ? pitem : null);
						p.updateInventory();
						return;
					} else {
						amt -= pitem.getAmount();
						p.getInventory().setItem(i, null);
						p.updateInventory();
					}
				}
			} else {
				return;
			}
		}
	}
	
	private boolean compareItem(ItemStack item1, ItemStack item2) {
		String item1Name = "";
		String item2Name = "";
		
		if(item1.getType() == item2.getType()) {
			if(item1.hasItemMeta() && item1.getItemMeta().hasDisplayName()) {
				item1Name = item1.getItemMeta().getDisplayName();
			} 
			if(item2.hasItemMeta() && item2.getItemMeta().hasDisplayName()) {
				item2Name = item2.getItemMeta().getDisplayName();
			} 
			
			return item1Name.equals(item2Name);
		} else {
			return false;
		}	

	}
	
	private boolean createCheck(Player p, String strAmout) {
		
		double money = 0;
		
		try {
			money = Double.parseDouble(strAmout);	
		} catch (Exception e) {
			p.sendMessage(mainMS+"���ڸ� �Է����ּ���.");
			return false;
		}
						
		Check check = null; //�Է��� ��ġ�� �ش��ϴ� ��ǥ�� �����ϴ���?
		
		for(Check tmpCheck : checkList) {
			if(tmpCheck.getMoney() == money) {
				check = tmpCheck;
				break;
			}
		}
		
		if(check == null) { //������ ��ǥ ��ġ�� �Է��� �� �ƴ϶��	
			p.sendMessage(mainMS+"<1000, 5000, 10000> �߿����� ���� �����մϴ�.");
		} else {
			ItemStack checkItem = check.getItem();

			if (isCanAddItem(p, checkItem)) {

				if (takeMoney(p.getUniqueId(), money)) {
					p.getInventory().addItem(check.getItem());
					return true;
				} else {
					p.sendMessage(mainMS + "���� �����մϴ�.");
				}

			} else {
				p.sendMessage(mainMS + "�κ��丮�� ������ �����մϴ�.");
			}			
		}
		
		return false;
	}
	
	@EventHandler
	public void onCommandInput(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		
		String[] args = e.getMessage().split(" ");
		String cmd = args[0];
		
		if(cmd.equalsIgnoreCase("/��ǥ")) {
			if(args.length == 1) {
				showHelpMessage(p);
			} else {
				if(args[1].equalsIgnoreCase("��ȯ")) {
					if(args.length == 2) {
						p.sendMessage(mainMS+"<1000, 5000, 10000> �߿��� �����Ͽ� �Է����ּ���.");
					} else {
						createCheck(p, args[2]); 
					}
				} else {
					showHelpMessage(p);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		
		ItemStack handItem = p.getInventory().getItemInMainHand();
		
		if(handItem != null) {
			if(handItem.hasItemMeta()) {
				for(Check check : checkList) {
					if(compareItem(handItem, check.getItem())) {
						double money = check.getMoney();
						if(takeItem(p, handItem, 1)) {
							giveMoney(p.getUniqueId(), money);	
							break;
						}
					}
				}
			}
		}
	}

}
