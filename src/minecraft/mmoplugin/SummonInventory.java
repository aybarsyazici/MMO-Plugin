package minecraft.mmoplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SummonInventory
{
    public static void createInfoInventory(Plugin plugin, Player player, Necromancer.Summon summon)
    {
        Inventory inventory = plugin.getServer().createInventory(new SummonCustomInventoryHolder(), 27, ChatColor.GOLD + "" + ChatColor.BOLD + "SUMMON ENVANTERI");

        ItemStack glassPane = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassMeta.setLocalizedName(" ");
        glassPane.setItemMeta(glassMeta);

        List<ItemStack> items;
        if(summon.getNormalEntity() instanceof SkeletonHorse)
        {
            items = summon.getRidingSummon().getItems();
        }
        else
        {
            items = summon.getItems();
        }


        for(int i = 0; i < 27; i++)
        {
            if(i == 11)
                inventory.setItem(i,items.get(0));
            else if(i==12)
                inventory.setItem(i,items.get(1));
            else if(i==13)
                inventory.setItem(i,items.get(2));
            else if(i==14)
                inventory.setItem(i,items.get(3));
            else if(i==15)
                inventory.setItem(i,items.get(4));
            else
                inventory.setItem(i, glassPane);
        }

        player.openInventory(inventory);
    }

    public static void createControlInventory(Plugin plugin, Player player)
    {
        Inventory inventory = plugin.getServer().createInventory(new SummonControlInventoryHolder(), 9, ChatColor.GOLD + "" + ChatColor.BOLD + "SUMMON KONTROL MENÜSÜ");

        ItemStack glassPane = new ItemStack(Material.BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassMeta.setLocalizedName(" ");
        glassPane.setItemMeta(glassMeta);


        ItemStack summonIcon = new ItemStack(Material.WITHER_SKELETON_SKULL,1);
        ItemMeta summonIconMeta = summonIcon.getItemMeta();
        summonIconMeta.setDisplayName(ChatColor.GOLD + "Summonınızı çağırın!");
        summonIcon.setItemMeta(summonIconMeta);

        ItemStack passiveStance = new ItemStack(Material.BLUE_BANNER,1);
        ItemMeta passiveStanceMeta = passiveStance.getItemMeta();
        passiveStanceMeta.setDisplayName(ChatColor.BLUE + "Summonınızı passif stance'e çevirin.");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.LIGHT_PURPLE + "Bu stancede summonınız sadece sizin saldırdığınız");
        lore.add(ChatColor.LIGHT_PURPLE + "hedeflere saldıracaktır.");
        lore.add(ChatColor.YELLOW + "Summonınız zaten passif stancede ise buraya basmak");
        lore.add(ChatColor.YELLOW + "Summonınızın saldırmasını durdurur.");
        passiveStanceMeta.setLore(lore);
        lore.clear();
        passiveStance.setItemMeta(passiveStanceMeta);


        ItemStack aggressiveStance = new ItemStack(Material.RED_BANNER,1);
        ItemMeta aggressiveStanceMeta = aggressiveStance.getItemMeta();
        aggressiveStanceMeta.setDisplayName(ChatColor.RED + "Summonınızı agresif stance'e çevirin.");
        lore.add(ChatColor.LIGHT_PURPLE + "Bu stancede summonınız yakında");
        lore.add(ChatColor.LIGHT_PURPLE + "gördüğü kişilere saldıracaktır.");
        aggressiveStanceMeta.setLore(lore);
        lore.clear();
        aggressiveStance.setItemMeta(aggressiveStanceMeta);


        ItemStack heal = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta healMeta = heal.getItemMeta();
        healMeta.setDisplayName(ChatColor.GREEN + "Summonınızı iyileştirin.");
        lore.add(ChatColor.LIGHT_PURPLE + "Bu yeteneğin 2 dakika bekleme süresi vardır.");
        healMeta.setLore(lore);
        lore.clear();
        heal.setItemMeta(healMeta);

        ItemStack killSummon = new ItemStack(Material.BONE);
        ItemMeta killMeta = killSummon.getItemMeta();
        killMeta.setDisplayName(ChatColor.RED + "Summonınızı sıfırlayın.");
        lore.add("");
        lore.add(ChatColor.YELLOW + "Summonınızı öldürür, ve sıfırlar.");
        killMeta.setLore(lore);
        lore.clear();
        killSummon.setItemMeta(killMeta);



        for(int i = 0; i < 9; i++)
        {
            if(i == 0)
                inventory.setItem(i,summonIcon);
            else if(i == 2)
                inventory.setItem(i,killSummon);
            else if(i==4)
                inventory.setItem(i,passiveStance);
            else if(i==5)
                inventory.setItem(i,aggressiveStance);
            else if(i==8)
                inventory.setItem(i,heal);
            else
                inventory.setItem(i, glassPane);
        }

        player.openInventory(inventory);
    }

    public static class SummonCustomInventoryHolder implements InventoryHolder
    {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public static class SummonControlInventoryHolder implements InventoryHolder
    {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
