package fr.utarwyn.skypvp.listeners;

import com.utaria.utariaapi.managers.AdminModeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener{

    @EventHandler
    public void onSignCreated(SignChangeEvent e){
        String line1 = e.getLine(0);
        String item  = e.getLine(1);

        if(line1.equals("[Item]")){
            e.setLine(0, "§5[Item]");
            e.setLine(1, e.getLine(2));
            e.setLine(2, e.getLine(3));
            e.setLine(3, "§b[" + item + "]");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Block b = e.getClickedBlock();

        if(b != null && (b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN))){
            Sign sign = (Sign) b.getState();

            if(sign.getLine(0).equals("§5[Item]")){
                String itemStr = sign.getLine(3).substring(3, sign.getLine(3).length() - 1);
                byte data = (byte) 0;

                if(itemStr.contains(":")){
                    String[] split = itemStr.split(":");
                    itemStr = split[0];
                    data = Byte.valueOf(split[1]);
                }

                Integer itemId = Integer.valueOf(itemStr);
                String itemName = sign.getLine(1) + " " + sign.getLine(2);

                Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST, "§8Item : " + itemName);

                for(int i = 0; i < inv.getSize(); i++){
                    Material mat = Material.getMaterial(itemId);
                    inv.setItem(i, new ItemStack(mat, mat.getMaxStackSize(), (short) 0, data));
                }

                player.openInventory(inv);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if(AdminModeManager.hasAdminMode(player) && e.getInventory().getName().contains("§8Item :"))
            e.setCancelled(true);
    }
}
