package minecraft.mmoplugin;

import minecraft.mmoplugin.customItems.CommonItems;
import minecraft.mmoplugin.customItems.CustomItems;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;

public class MarketNPCInventoryListener implements Listener
{

    Plugin plugin;

    MarketNPCInventoryListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event)
    {
        Inventory inventory = event.getClickedInventory();

        if(event.isCancelled())
            return;

        if(inventory == null)
            return;

        Inventory menu = event.getInventory();

        if(inventory != menu)
            return;

        if(event.getInventory().getHolder() instanceof CustomInventory.NPCWelcomeMenuHolder)
        {

            event.setCancelled(true);

            int slot = event.getSlot();

            CustomInventory.NPCWelcomeMenuHolder holder = (CustomInventory.NPCWelcomeMenuHolder) menu.getHolder();

            switch (slot)
            {
                case 11:
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    switch (holder.getNPCName().toLowerCase())
                    {
                        case "demirci":
                            CustomInventory.createDemirciInventory((Player) event.getWhoClicked(),plugin);
                            break;
                        case "oduncu":
                            CustomInventory.createOduncuInventory((Player) event.getWhoClicked(),plugin);
                            break;
                        case "madenci":
                            CustomInventory.createMadenciInventory((Player) event.getWhoClicked(),plugin);
                            break;
                        case "biyolog":
                            CustomInventory.createBiyologInventory((Player) event.getWhoClicked(),plugin);
                            break;
                    }
                    break;
                }
                case 13:
                case 15:
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Pek yakında geliyor!");
                    break;
                }
            }

            return;
        }

        if(!MainClass.classObjectMap.containsKey(event.getWhoClicked().getUniqueId()))
        {
            event.setCancelled(true);

            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "NPC'lerden alışveriş yapmadan önce sınıfını seçmiş olman gerekir.");
            return;
        }

        if(event.getView().getTitle().contains("Demirci"))
        {

            event.setCancelled(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());

            int slot = event.getSlot();

            Player whoClicked = (Player) event.getWhoClicked();

            switch (slot)
            {
                case 11: //Stone sword
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonStoneSwordPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonStoneSwordPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonStoneSword());
                    break;
                case 12: //Chain Helmet
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonChainHelmetPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonChainHelmetPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonChainHelmet());
                    break;
                case 13: //Chain plate
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonChainPlatePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonChainPlatePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonChainPlate());
                    break;
                case 14: //Chain leggings
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonChainLeggingsPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonChainLeggingsPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonChainLeggings());
                    break;
                case 15: //Chain boots
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonChainBootsPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonChainBootsPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonChainBoots());
                    break;
                case 29: //Iron Sword
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronSwordPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronSwordPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonIronSword());
                    break;
                case 30: //Iron Helmet
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronHelmetPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronHelmetPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonIronHelmet());
                    break;
                case 31: //Iron Plate
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronPlatePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronPlatePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonIronPlate());
                    break;
                case 32: //Iron Leggings
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronLeggingsPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronLeggingsPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonIronLeggings());
                    break;
                case 33: //Iron Boots
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronBootsPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronBootsPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonIronBoots());
                    break;
                case 34: //Shield
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonShieldPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonShieldPrice);
                    event.getWhoClicked().getInventory().addItem(CustomItems.createCustomShield("Kalkan", CustomItems.Enderlik.SIRADAN));
                    break;
        }
        }
        else if(event.getView().getTitle().contains("Oduncu"))
        {
            event.setCancelled(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());

            int slot = event.getSlot();

            Player whoClicked = (Player) event.getWhoClicked();

            switch (slot)
            {
                case 14: //Birch log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.BIRCH_LOG,"Huş Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.BIRCH_LOG,"Huş Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 15: //Jungle log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.JUNGLE_LOG,"Orman Ağacı Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.JUNGLE_LOG,"Orman Ağacı Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 16: //Acacia log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.ACACIA_LOG,"Akasya Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.ACACIA_LOG,"Akasya Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 20: //Stone Axe
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonStoneAxePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonStoneAxePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonStoneAxe());
                    break;
                case 21: //Iron Axe
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronAxePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronAxePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.Equipment.commonIronAxe());
                    break;
                case 23: //Spruce log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.SPRUCE_LOG,"Ladin Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.SPRUCE_LOG,"Ladin Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 24: //Oak log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.OAK_LOG,"Meşe Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.OAK_LOG,"Meşe Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 25: //Dark oak log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.DARK_OAK_LOG,"Koyu Meşe Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.DARK_OAK_LOG,"Koyu Meşe Kütüğü",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 29: //Apple
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonApplePrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonApplePrice*10);
                        ItemStack multiple = CommonItems.createElma();
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonApplePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonApplePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createElma());
                    break;
                case 32: //Warped stem
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.WARPED_STEM,"Çarpık Kök",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.WARPED_STEM,"Çarpık Kök",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
                case 33: //Crimon log
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice*10)
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice*10);
                        ItemStack multiple = CommonItems.createCommonItem(Material.CRIMSON_STEM,"Kızıl Kök",false,0,0,false,CommonItems.ItemPrices.commonLogPrice);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonLogPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLogPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.CRIMSON_STEM,"Kızıl Kök",false,0,0,false,CommonItems.ItemPrices.commonLogPrice));
                    break;
            }
        }
        else if(event.getView().getTitle().contains("Madenci"))
        {
            event.setCancelled(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());

            int slot = event.getSlot();

            Player whoClicked = (Player) event.getWhoClicked();

            switch (slot)
            {
                case 10: //Stone Pickaxe
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonStonePickAxePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonStonePickAxePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.STONE_PICKAXE,"Taş Kazma",true,3,1.2,false,CommonItems.ItemPrices.commonStonePickAxePrice));
                    break;
                case 11: //Stone Shovel
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonStoneShovelPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonStoneShovelPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.STONE_SHOVEL,"Taş Kürek",true,3.5,1,false,CommonItems.ItemPrices.commonStoneShovelPrice));
                    break;
                case 12: //Stone Hoe
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonStoneHoePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonStoneHoePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.STONE_HOE,"Taş Çapa",true,1,2,false,CommonItems.ItemPrices.commonStoneHoePrice));
                    break;
                case 28: //Iron Pickaxe
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronPickAxePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronPickAxePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.IRON_PICKAXE,"Demir Kazma",true,4,1.2,false,CommonItems.ItemPrices.commonIronPickAxePrice));
                    break;
                case 29: //Iron Shovel
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronShovelPrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronShovelPrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.IRON_SHOVEL,"Demir Kürek",true,4.5,1,false,CommonItems.ItemPrices.commonIronShovelPrice));
                    break;
                case 30: //Iron Hoe
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronHoePrice)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronHoePrice);
                    event.getWhoClicked().getInventory().addItem(CommonItems.createCommonItem(Material.IRON_HOE,"Demir Çapa",true,1,3,false,CommonItems.ItemPrices.commonIronHoePrice));
                    break;
                case 15: //Red Stone
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    ItemStack multiple = CommonItems.createCommonItem(Material.REDSTONE, "Kızıl Taş Tozu", false, 0, 0, false, CommonItems.ItemPrices.commonRedStonePrice);
                    if (event.isRightClick()) {
                        if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonRedStonePrice * 10) {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonRedStonePrice * 10);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonRedStonePrice) {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonRedStonePrice);
                    event.getWhoClicked().getInventory().addItem(multiple);
                    break;
                }
                case 16: //Lapis Lazuli
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    ItemStack multiple = CommonItems.createCommonItem(Material.LAPIS_LAZULI,"Lapis Lazuli",false,0,0,false,CommonItems.ItemPrices.commonLapisPrice);
                    if (event.isRightClick()) {
                        if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonLapisPrice * 10) {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLapisPrice * 10);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonLapisPrice) {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonLapisPrice);
                    event.getWhoClicked().getInventory().addItem(multiple);
                    break;
                }
                case 24: //Coal
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    ItemStack multiple = CommonItems.createCommonItem(Material.COAL, "Kömür", false, 0, 0, false, CommonItems.ItemPrices.commonCoalPrice);
                    if (event.isRightClick()) {
                        if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonCoalPrice * 10) {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonCoalPrice * 10);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonCoalPrice) {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonCoalPrice);
                    event.getWhoClicked().getInventory().addItem(multiple);
                    break;
                }
                case 25: //Gold Ingot
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    ItemStack multiple = CommonItems.createCommonItem(Material.GOLD_INGOT, "Altın Külçesi", false, 0, 0, false, CommonItems.ItemPrices.commonGoldIngotPrice);
                    if (event.isRightClick()) {
                        if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonGoldIngotPrice * 10) {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonGoldIngotPrice * 10);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonGoldIngotPrice) {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonGoldIngotPrice);
                    event.getWhoClicked().getInventory().addItem(multiple);
                    break;
                }
                case 33: //Diamond
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    ItemStack multiple = CommonItems.createCommonItem(Material.DIAMOND,"Elmas",false,0,0,false,CommonItems.ItemPrices.commonDiamondPrice);
                    if (event.isRightClick()) {
                        if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonDiamondPrice * 10) {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonDiamondPrice * 10);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonDiamondPrice) {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonDiamondPrice);
                    event.getWhoClicked().getInventory().addItem(multiple);
                    break;
                }
                case 34: //Iron Ingot
                {
                    whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    ItemStack multiple = CommonItems.createCommonItem(Material.IRON_INGOT,"Demir Külçesi",false,0,0,true,CommonItems.ItemPrices.commonIronIngotPrice);
                    if (event.isRightClick()) {
                        if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronIngotPrice * 10) {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronIngotPrice * 10);
                        multiple.setAmount(10);
                        event.getWhoClicked().getInventory().addItem(multiple);
                        return;
                    }
                    if (mmoClass.getCurrency() < CommonItems.ItemPrices.commonIronIngotPrice) {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonIronIngotPrice);
                    event.getWhoClicked().getInventory().addItem(multiple);
                    break;
                }
            }
        }
        else if(event.getView().getTitle().contains("Biyolog"))
        {
            event.setCancelled(true);

            MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());

            int slot = event.getSlot();

            Player whoClicked = (Player) event.getWhoClicked();

            ItemStack clickedItem = menu.getItem(slot);
            if(clickedItem != null && !clickedItem.getType().equals(Material.AIR) && !clickedItem.getType().equals(Material.LIME_STAINED_GLASS_PANE))
            {
                //We know the clicked item is NOT a glass pane and is not null.
                whoClicked.playSound(whoClicked.getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                if(slot == 30)
                {
                    if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonHealPotionPrice)
                    {
                        whoClicked.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız yok!");
                        return;
                    }
                    mmoClass.reduceCurrency(CommonItems.ItemPrices.commonHealPotionPrice);
                    whoClicked.getInventory().addItem(CustomItems.createCustomPotion("Ani İyileştirme İksiri", PotionType.INSTANT_HEAL, CustomItems.Enderlik.SIRADAN,Material.POTION));
                    return;
                }
                ItemStack toBeGiven = CommonItems.createCommonItem(clickedItem.getType(),clickedItem.getItemMeta().getDisplayName(),false,0,0,false,0);
                if(clickedItem.getType().toString().contains("SAPLING"))
                {
                    //Clicked on a sapling.
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonSaplingPrice*10)
                        {
                            whoClicked.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterince paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonSaplingPrice*10);
                        toBeGiven.setAmount(10);
                        whoClicked.getInventory().addItem(toBeGiven);
                    }
                    else
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonSaplingPrice)
                        {
                            whoClicked.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterince paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonSaplingPrice);
                        whoClicked.getInventory().addItem(toBeGiven);
                    }
                }
                else
                {
                    //Clicked on a misc.
                    if(event.isRightClick())
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonBiyologMiscPrice*10)
                        {
                            whoClicked.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterince paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonBiyologMiscPrice*10);
                        toBeGiven.setAmount(10);
                        whoClicked.getInventory().addItem(toBeGiven);
                    }
                    else
                    {
                        if(mmoClass.getCurrency() < CommonItems.ItemPrices.commonBiyologMiscPrice)
                        {
                            whoClicked.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterince paranız yok!");
                            return;
                        }
                        mmoClass.reduceCurrency(CommonItems.ItemPrices.commonBiyologMiscPrice);
                        whoClicked.getInventory().addItem(toBeGiven);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDragEvent(InventoryDragEvent event)
    {
        Inventory menu = event.getInventory();
        if(menu.getHolder() instanceof CustomInventory.NPCWelcomeMenuHolder)
        {
            event.setCancelled(true);
        }
        else if(menu.getHolder() instanceof CustomInventory.NPCShopMenuHolder)
        {
            event.setCancelled(true);
        }
    }
}
