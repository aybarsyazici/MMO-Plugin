package minecraft.mmoplugin;

import com.comphenix.net.sf.cglib.asm.$MethodVisitor;
import com.google.common.collect.Lists;
import minecraft.mmoplugin.CustomInventory;
import minecraft.mmoplugin.events.DBSaver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.sqlite.core.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AuctionInventoryListener implements Listener
{
    Plugin plugin;

    AuctionInventoryListener(Plugin plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAuctionMenuClickEvent(InventoryClickEvent event)
    {
        Inventory clickedInventory = event.getClickedInventory();

        if(event.isCancelled())
            return;

        if(clickedInventory == null)
            return;

        Inventory menu = event.getInventory();

        if(menu.getHolder() instanceof CustomInventory.AuctionSearchHolder) //We are searching for items.
        {
            event.setCancelled(true);

            if(event.isShiftClick())
            {
                event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Lütfen shift tık kullanmayın.");
                return;
            }

            event.setCurrentItem(event.getCurrentItem());
            event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());



            if(menu != clickedInventory)
                return;

            int slot = event.getSlot();
            CustomInventory.AuctionSearchHolder holder = (CustomInventory.AuctionSearchHolder) menu.getHolder();


            switch (slot)
            {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 10:
                case 17:
                case 19:
                case 26:
                case 28:
                case 35:
                case 37:
                case 44:
                case 46:
                case 47:
                case 50:
                    //Do nothing as these are the glass indexes.
                    break;
                case 0:
                {
                    //Filter according to weapons.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    String tier = holder.getTier();
                    String sort = holder.getSort();
                    String kategori = holder.getCategory();
                    if(kategori == null)
                        kategori = "silahlar";
                    else if(kategori.equalsIgnoreCase("silahlar"))
                        kategori = null;
                    else
                        kategori = "silahlar";
                    CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,0,kategori,tier,sort, holder.getLikeCondition());
                    break;
                }
                case 9:
                {
                    //Filter according to armour
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    String tier = holder.getTier();
                    String sort = holder.getSort();
                    String kategori = holder.getCategory();
                    if(kategori == null)
                        kategori = "zırhlar";
                    else if(kategori.equalsIgnoreCase("zırhlar"))
                        kategori = null;
                    else
                        kategori = "zırhlar";
                    CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,0,kategori,tier,sort, holder.getLikeCondition());
                    break;
                }
                case 48:
                {
                    //Search by name.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                        String likeCondition = "";
                        for(String s : strings)
                        {
                            likeCondition = s;
                            break;
                        }
                        if(likeCondition.equalsIgnoreCase(""))
                        {

                        }
                        if(likeCondition.equalsIgnoreCase(""))
                        {
                            likeCondition = null;
                        }
                        String finalCondition = likeCondition;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            CustomInventory.createAuctionSearchMenu(player,plugin,0,holder.getCategory(),holder.getTier(),holder.getSort(),finalCondition);
                        },0);
                        return true;
                    })).open((Player) event.getWhoClicked());
                    break;
                }
                case 49:
                {
                    //Go back a page.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if(holder.getPage() > 0)
                    {
                        String tier = holder.getTier();
                        String sort = holder.getSort();
                        String kategori = holder.getCategory();
                        int page = holder.getPage();
                        CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,page-1,kategori,tier,sort,holder.getLikeCondition());
                    }
                    else
                    {
                        CustomInventory.createAuctionWelcomeMenu((Player) event.getWhoClicked(),plugin);
                    }
                    break;
                }
                case 51:
                {
                    //Tier filtering.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    String tier = holder.getTier();
                    String sort = holder.getSort();
                    String kategori = holder.getCategory();
                    if(tier == null)
                    {
                        tier = "Sıradan";
                    }
                    else
                    {
                        switch (tier)
                        {
                            case "Sıradan":
                                tier = "Nadir";
                                break;
                            case "Nadir":
                                tier = "Eşsiz";
                                break;
                            case "Eşsiz":
                                tier = "Olağanüstü";
                                break;
                            case "Olağanüstü":
                                tier = "Destansı";
                                break;
                            case "Destansı":
                                tier = null;
                                break;
                        }
                    }
                    CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,0,kategori,tier,sort,holder.getLikeCondition());
                    break;
                }
                case 52:
                {
                    //Ordering
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    String tier = holder.getTier();
                    String sort = holder.getSort();
                    String kategori = holder.getCategory();
                    String likeCondition = holder.getLikeCondition();
                    if(sort == null)
                    {
                        sort = "en ucuz";
                    }
                    else
                    {
                        switch (sort.toLowerCase())
                        {
                            case "en ucuz":
                                sort = "en pahallı";
                                break;
                            case "en pahallı":
                                sort = "yakında bitiyor";
                                break;
                            case "yakında bitiyor":
                                sort = null;
                                break;
                        }
                    }
                    CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,holder.getPage(),kategori,tier,sort,likeCondition);
                    break;
                }
                case 53:
                {
                    //Go a page forward.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    int page = holder.getPage();
                    if (page < 19)
                    {
                        String tier = holder.getTier();
                        String sort = holder.getSort();
                        String kategori = holder.getCategory();
                        CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,page+1,kategori,tier,sort,holder.getLikeCondition());
                    }
                    break;
                }
                default:
                    // We know that the player has clicked a slot where there is supposed to be an item.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    DateTime dt = new DateTime();
                    ItemStack clickedItem = menu.getItem(slot);
                    if (clickedItem == null)
                        return; //The clicked item slot is empty, so return.
                    if(clickedItem.getType().equals(Material.AIR))
                        return; //The clicked item slot is empty, so return.
                    try
                    {
                        ItemMeta clickedMeta = clickedItem.getItemMeta();
                        List<String> lore = clickedMeta.getLore();

                        int itemid = 0;
                        String maxBidder = null;
                        for(String s : lore)
                        {
                            if(s.contains("Eşya IDsi: "))
                            {
                                s = ChatColor.stripColor(s);
                                itemid = Integer.parseInt(s.substring(11,s.length()));
                            }
                            else if(s.contains("Teklif eden oyuncu: "))
                            {
                                s = ChatColor.stripColor(s);
                                maxBidder = s.substring(20,s.length());
                            }
                        }

                        if (itemid != 0)
                        {
                            PreparedStatement ps = MainClass.conn.prepareStatement("select Seller,currentBid from auction_items where id=?");
                            ps.setInt(1,itemid);
                            ResultSet rs = ps.executeQuery();
                            if(rs.next())
                            {
                                UUID uuid = UUID.fromString(rs.getString("Seller"));

                                CustomInventory.createBiddingMenu((Player) event.getWhoClicked(),plugin,itemid,0,rs.getInt("currentBid"),Bukkit.getOfflinePlayer(uuid).getName(),maxBidder);
                            }
                            rs.close();
                            rs = null;

                            ps.close();
                            ps = null;
                        }
                        else
                        {
                            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Nothing found");
                        }
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }
        else if(event.getView().getTitle().contains("Market Menüsü")) // We are in the main auction menu. Thus cancel all clicks.
        {
            event.setCancelled(true);
            int clickedSlot = event.getSlot();
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "CurrentItem: " + event.getCurrentItem());
            //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Item On Cursor: " + event.getWhoClicked().getItemOnCursor());
            event.setCurrentItem(event.getCurrentItem());
            event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());

            if(menu != clickedInventory)
                return;

            switch (clickedSlot)
            {
                case 11:
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createAuctionSearchMenu((Player) event.getWhoClicked(),plugin,0,null,null,null,null);
                    break;
                case 13:
                    //TODO SHOW PLAYERS BIDS
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createOldBidMenu((Player) event.getWhoClicked(),plugin,0);
                    break;
                case 15:
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createAuctionCreateMenu((Player) event.getWhoClicked(),plugin,12,1000, null);
                    break;
                case 31:
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createItemVault((Player) event.getWhoClicked(),plugin,0);
                    break;
            }
        }
        else if(event.getView().getTitle().contains("Ürün koy")) // We are in the menu where we are going to let the player create an auction.
        {
            CustomInventory.AuctionInventoryHolder holder = (CustomInventory.AuctionInventoryHolder) menu.getHolder();
            //plugin.getServer().getConsoleSender().sendMessage("Clicked inv is: " + event.getClickedInventory().getType());
            //plugin.getServer().getConsoleSender().sendMessage("Inv: " + event.getInventory().getType());
            /* *********************************************************************************** */
            List<String> lore = new ArrayList<>();
            ItemStack auctionItem = new ItemStack(Material.STONE_BUTTON);
            ItemMeta itemMeta = auctionItem.getItemMeta();
            itemMeta.setDisplayName(ChatColor.YELLOW + "Envanterinde bir eşyaya tıkla!");
            lore.add(ChatColor.GRAY + "Satmak istediğin eşyayı buraya koy!");
            lore.add(ChatColor.GRAY + "Shift ile de tıklayabilirsin!");
            itemMeta.setLore(lore);
            lore.clear();
            /* *********************************************************************************** */

            /*
            lore.add(ChatColor.GRAY + "Bu ürün markete başka"); INDEX 0
            lore.add(ChatColor.GRAY + "oyuncular tarafından satın"); INDEX 1
            lore.add(ChatColor.GRAY + "alınması için konulacak."); INDEX 2
            lore.add(ChatColor.GRAY + ""); INDEX 3
            lore.add(ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + "YOK"); INDEX 4
            lore.add(ChatColor.GRAY + "Süre: " + ChatColor.YELLOW + "12 saat"); INDEX 5
            lore.add(ChatColor.GRAY + "Başlangıç teklifi: " + ChatColor.GOLD + "1000 para"); INDEX 6
            lore.add(ChatColor.GRAY + ""); INDEX 7
            lore.add(ChatColor.GRAY + "Toplam extra ücret: " + ChatColor.GOLD + totalExtra + " para"); INDEX 8
            lore.add(ChatColor.GRAY + ""); INDEX 9
            lore.add(ChatColor.YELLOW + "Yaratmak için tıkla!"); INDEX 10
            */

            auctionItem.setItemMeta(itemMeta);
            if(event.getClick().isShiftClick())
            {
                event.setCancelled(true);
                if (event.getCurrentItem() != null && event.getClickedInventory().getType().equals(InventoryType.PLAYER) && menu.getItem(13).getItemMeta().getDisplayName().contains("Envanterinde bir eşyaya tıkla!"))
                { //We get in this if statement, when player tries to shift click an item from their inventory into the selling slot.
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                        ItemStack tryingToBePut = event.getCurrentItem();
                        menu.setItem(13,event.getCurrentItem());
                        event.setCurrentItem(new ItemStack(Material.AIR));
                        ItemStack sellingWool = menu.getItem(29);
                        ItemMeta woolMeta = sellingWool.getItemMeta();
                        List<String> woolLore = woolMeta.getLore();
                        String temp;
                        temp = ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + tryingToBePut.getI18NDisplayName();
                        woolLore.remove(4);
                        woolLore.add(4,temp);
                        woolMeta.setLore(woolLore);
                        sellingWool.setItemMeta(woolMeta);
                        holder.setAlreadyPut(tryingToBePut);
                    },0);
                }
                else if(event.getCurrentItem() != null && menu==clickedInventory && event.getSlot() == 13 && !menu.getItem(13).getItemMeta().getDisplayName().contains("Envanterinde bir eşyaya tıkla!"))
                { //We get in this if statement, when the player tries to shift click the item he is trying to sell back to his inventory.
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                        ItemStack itemToBeAdded = menu.getItem(13);
                        menu.setItem(13,auctionItem);
                        event.getWhoClicked().getInventory().addItem(itemToBeAdded);
                        ItemStack sellingWool = menu.getItem(29);
                        ItemMeta woolMeta = sellingWool.getItemMeta();
                        List<String> woolLore = woolMeta.getLore();
                        String temp;
                        temp = ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + "YOK";
                        woolLore.remove(4);
                        woolLore.add(4,temp);
                        woolMeta.setLore(woolLore);
                        sellingWool.setItemMeta(woolMeta);
                        holder.setAlreadyPut(null);
                    },0);
                }
                return;
            }

            if(menu != clickedInventory) //Now we know that the player has clicked on an item on the Market menu.
                return;

            int clickedSlot = event.getSlot();

            switch (clickedSlot)
            {
                case 13: {
                    //No problem.
                    if (event.getWhoClicked().getItemOnCursor().getType().equals(Material.AIR)) {
                        //We get in this if statement when the player tries to recover the item he put, by left clicking, and he does not put anything back.
                        event.setCancelled(true);

                        if(menu.getItem(13).getItemMeta().getDisplayName().contains("Envanterinde bir eşyaya tıkla!"))
                            return;

                        ItemStack itemBeingRecovered = event.getCurrentItem();
                        event.getWhoClicked().setItemOnCursor(itemBeingRecovered);
                        event.setCurrentItem(auctionItem);

                        ItemStack sellingWool = menu.getItem(29);
                        ItemMeta woolMeta = sellingWool.getItemMeta();
                        List<String> woolLore = woolMeta.getLore();
                        String temp;
                        temp = ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + "YOK";
                        woolLore.remove(4);
                        woolLore.add(4,temp);
                        woolMeta.setLore(woolLore);
                        sellingWool.setItemMeta(woolMeta);
                        holder.setAlreadyPut(null);

                    }
                    else
                    {
                        ItemStack itemBeingTriedToPut = event.getWhoClicked().getItemOnCursor();
                        //We get in this if when the player tries to put something into the 13th slot by clicking normally.
                        if(menu.getItem(13).getItemMeta().getDisplayName().contains("Envanterinde bir eşyaya tıkla!"))
                        {
                            event.setCancelled(true);

                            event.setCurrentItem(itemBeingTriedToPut);
                            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                        }
                        ItemStack sellingWool = menu.getItem(29);
                        ItemMeta woolMeta = sellingWool.getItemMeta();
                        List<String> woolLore = woolMeta.getLore();
                        String temp;
                        temp = ChatColor.GRAY + "Eşya: " + ChatColor.WHITE + itemBeingTriedToPut.getI18NDisplayName();
                        woolLore.remove(4);
                        woolLore.add(4,temp);
                        woolMeta.setLore(woolLore);
                        sellingWool.setItemMeta(woolMeta);
                        holder.setAlreadyPut(itemBeingTriedToPut);
                    }
                    break;
                }
                case 29:
                {
                    event.setCancelled(true);
                    if(menu.getItem(13).getItemMeta().getDisplayName().contains("Envanterinde bir eşyaya tıkla!"))
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Önce satıcak bir eşya koyun.");
                        return;
                    }
                    int extraStartingBid = holder.getStartingBid()*5/100;
                    int extraDuration = holder.getDuration()*20;
                    int total = extraDuration + extraStartingBid;
                    MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());
                    if(mmoClass.getCurrency() < total)
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paran bulunmuyor!");
                        return;
                    }
                    try
                    {
                        PreparedStatement ps = MainClass.conn.prepareStatement("insert into auction_items(Name,Quality,Quantity,Seller,currentBid,EndDate,category, base64String,enchants) values (?,?,?,?,?,?,?,?,?)");
                        ItemStack toBeStored = menu.getItem(13);
                        ItemMeta meta = toBeStored.getItemMeta();
                        List<String> itemsLore = meta.getLore();
                        String quality = null;
                        if (itemsLore != null)
                        {
                            for(String s : itemsLore)
                            {
                                if(s.contains("Enderlik"))
                                {
                                    s = ChatColor.stripColor(s); //Enderlik : Sıradan
                                    quality = s.substring(11,s.length());
                                    break;
                                }
                            }
                        }

                        ps.setString(1,toBeStored.getItemMeta().getDisplayName()); //Name
                        if(quality != null)
                            ps.setString(2,quality);
                        else
                            ps.setString(2,"Common"); //Quality
                        ps.setInt(3,toBeStored.getAmount()); //Quantity
                        ps.setString(4, String.valueOf(event.getWhoClicked().getUniqueId())); //Seller
                        DateTime dt = new DateTime().plusHours(holder.getDuration());
                        ps.setInt(5,holder.getStartingBid());
                        ps.setTimestamp(6, new Timestamp(dt.getMillis())); //End Date
                        ps.setString(7,CustomInventory.getItemCategoryString(toBeStored)); //Category
                        ps.setString(8,DBSaver.toBase64(toBeStored));
                        String enchs = "";
                        for(Enchantment e : toBeStored.getEnchantments().keySet())
                        {
                            enchs += e.getName()+"_"+toBeStored.getEnchantments().get(e);
                        }
                        ps.setString(9,enchs);
                        ps.executeUpdate();

                        menu.setItem(13,auctionItem);
                        holder.setChangingWindows(true);
                        holder.setAlreadyPut(null);
                        event.getWhoClicked().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                        mmoClass.reduceCurrency(total);
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Eşyanız başarıyla markete koyuldu!");
                    }
                    catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                }
                case 31:
                {
                    event.setCancelled(true);
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                        String amount = "";
                        for(String s : strings)
                        {
                            amount = s;
                            break;
                        }
                        String finalAmount = amount;
                        int bid = holder.getStartingBid();
                        try {
                            bid = Integer.parseInt(amount);
                            if(bid < 0)
                            {
                                bid = 0;
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            //Player entered a string inside of a number.
                        }
                        int duration = holder.getDuration();
                        ItemStack alreadyPut = holder.getAlreadyPut();
                        final int finalBid = bid;
                        holder.setChangingWindows(true);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            CustomInventory.createAuctionCreateMenu(player,plugin,duration, finalBid,alreadyPut);
                        },0);
                        return true;
                    })).open((Player) event.getWhoClicked());
                    break;
                }
                case 33:
                {
                    event.setCancelled(true);
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    holder.setChangingWindows(true);
                    CustomInventory.createDurationSelectionMenu((Player) event.getWhoClicked(),plugin,holder.getDuration(),holder.getStartingBid(),holder.getAlreadyPut());
                    break;
                }
                case 49:
                {
                    event.setCancelled(true);
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createAuctionWelcomeMenu((Player) event.getWhoClicked(),plugin);
                }
                default:
                    event.setCancelled(true);
            }
        }
        else if(event.getView().getTitle().contains("Zaman seç")) // We are going to choose the time.
        {
            event.setCancelled(true);

            if(menu != clickedInventory)
                return;

            CustomInventory.AuctionInventoryHolder holder = (CustomInventory.AuctionInventoryHolder) menu.getHolder();

            int slot = event.getSlot();
            int bid = holder.getStartingBid();
            ItemStack alreadyPut = holder.getAlreadyPut();
            holder.setChangingWindows(true);
            switch (slot)
            {
                case 10:
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createAuctionCreateMenu((Player) event.getWhoClicked(),plugin,2,bid,alreadyPut);
                    break;
                }
                case 12:
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createAuctionCreateMenu((Player) event.getWhoClicked(),plugin,12,bid,alreadyPut);
                    break;
                }
                case 14:
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    CustomInventory.createAuctionCreateMenu((Player) event.getWhoClicked(),plugin,48,bid,alreadyPut);
                    break;
                }
                case 16: //Custom time selection.
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                        String amount = "";
                        for(String s : strings)
                        {
                            amount = s;
                            break;
                        }
                        String finalAmount = amount;
                        int defaultDuration = holder.getDuration();
                        try {
                            defaultDuration = Integer.parseInt(amount);
                        }
                        catch (NumberFormatException e)
                        {
                            //Player entered a string inside of a number.
                        }
                        int finalBid = holder.getStartingBid();
                        int finalDefaultDuration = defaultDuration;
                        if (finalDefaultDuration <= 0)
                            finalDefaultDuration = 1;
                        else if(finalDefaultDuration > 72)
                        {
                            finalDefaultDuration = 72;
                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Maximum izin verilen süre 72 saat.");
                        }
                        final int finalDuration = finalDefaultDuration;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            CustomInventory.createAuctionCreateMenu(player,plugin, finalDuration, finalBid,alreadyPut);
                        },0);
                        return true;
                    })).open((Player) event.getWhoClicked());
                    break;
                }
                case 31:
                {
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    int duration = holder.getDuration();
                    holder.setChangingWindows(true);
                    CustomInventory.createAuctionCreateMenu((Player) event.getWhoClicked(),plugin,duration,bid,alreadyPut);
                    break;
                }
            }
        }
        else if(menu.getHolder() instanceof CustomInventory.BiddingMenuHolder) //We are in the bidding menu
        {
            event.setCancelled(true);

            if(menu != clickedInventory)
                return;

            event.setCurrentItem(event.getCurrentItem());
            event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());

            CustomInventory.BiddingMenuHolder holder = (CustomInventory.BiddingMenuHolder) menu.getHolder();

            //Now we know for sure that the player is clicking somewhere in the bidding menu.
            int slot = event.getSlot();
            switch (slot) //Now we will see which slot the player has clicked.
            {
                case 28:
                    //We will go back a page.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if (holder.getPage() > 0)
                    {
                        CustomInventory.createBiddingMenu((Player) event.getWhoClicked(),plugin,holder.getItemId(),holder.getPage()-1,holder.getMaxBid(),holder.getSellerName(),holder.getMaxBidder());
                    }
                    else
                    {
                        CustomInventory.createAuctionWelcomeMenu((Player) event.getWhoClicked(),plugin);
                    }
                    break;
                case 31:
                    //Player will put a new bid.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if (!event.getWhoClicked().getName().equals(holder.getSellerName()))
                    {
                        if (!holder.getMaxBidder().equals(event.getWhoClicked().getName()))
                        {
                            MainClass.signMenu.newMenu(new ArrayList<>()).reopenIfFail().response(((player, strings) -> {
                                String amount = "";
                                for(String s : strings)
                                {
                                    amount = s;
                                    break;
                                }
                                String finalAmount = amount;
                                int integerAmount = 0;
                                try {
                                    integerAmount = Integer.parseInt(finalAmount);
                                }
                                catch (NumberFormatException e)
                                {
                                    //Player entered a string inside of a number.
                                }
                                int currentMaxBid = holder.getMaxBid();
                                final int finalIntegerAmount = integerAmount;
                                int page = holder.getPage();
                                if(finalIntegerAmount > currentMaxBid)
                                {
                                    MMOClass mmoClass = MainClass.classObjectMap.get(event.getWhoClicked().getUniqueId());
                                    if (mmoClass.getCurrency() >= finalIntegerAmount)
                                    {
                                        try
                                        {
                                            PreparedStatement ps = MainClass.conn.prepareStatement("select bidAmount from items_bidders where item_id=? and player_id=?");
                                            ps.setInt(1, holder.getItemId());
                                            ps.setString(2, String.valueOf(event.getWhoClicked().getUniqueId()));
                                            ResultSet rs = ps.executeQuery();
                                            int oldBid = 0;
                                            if(rs.next())
                                                oldBid = rs.getInt("bidAmount");
                                            ps.close();
                                            rs.close();
                                            rs = null;

                                            ps = MainClass.conn.prepareStatement("delete from items_bidders where item_id=? and player_id=?");
                                            ps.setInt(1,holder.getItemId());
                                            ps.setString(2, String.valueOf(event.getWhoClicked().getUniqueId()));
                                            ps.executeUpdate();

                                            ps.close();

                                            ps = MainClass.conn.prepareStatement("insert into items_bidders values(?,?,?)");
                                            ps.setInt(1,holder.getItemId());
                                            ps.setString(2, String.valueOf(event.getWhoClicked().getUniqueId()));
                                            ps.setInt(3, finalIntegerAmount);
                                            ps.executeUpdate();

                                            ps.close();

                                            ps = MainClass.conn.prepareStatement("update auction_items set currentBid=?,EndDate=DATE_ADD(EndDate,interval 30 minute) where id=?");
                                            ps.setInt(1,finalIntegerAmount);
                                            ps.setInt(2,holder.getItemId());
                                            ps.executeUpdate();

                                            ps.close();

                                            ps = null;

                                            holder.setMaxBid(finalIntegerAmount);
                                            page = 0;
                                            mmoClass.reduceCurrency(finalIntegerAmount-oldBid);
                                            player.sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Teklifiniz başarıyla koyuldu!");

                                        }
                                        catch (SQLException e)
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                    else
                                    {
                                        player.sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Yeterli paranız bulunmuyor.");
                                    }
                                }
                                else
                                {
                                    player.sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "Minimum önerebileceğiniz miktar: " + ChatColor.GOLD + currentMaxBid);
                                }
                                int finalPage = page;
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                                    CustomInventory.createBiddingMenu(player,plugin,holder.getItemId(), finalPage,holder.getMaxBid(),holder.getSellerName(),holder.getMaxBidder());
                                },0);
                                return true;
                            })).open((Player) event.getWhoClicked());
                        }
                        else
                        {
                            event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "En yüksek teklif zaten sizin.");
                            return;
                        }
                    }
                    else
                    {
                        event.getWhoClicked().sendMessage(MainClass.getPluginPrefix() + ChatColor.RED + "Kendi koyduğun bir ürüne teklif veremezsin.");
                        return;
                    }
                    break;
                case 34:
                    //We will go forward a page.
                    ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                    if (holder.getPage() < 19)
                    {
                        CustomInventory.createBiddingMenu((Player) event.getWhoClicked(),plugin,holder.getItemId(),holder.getPage()+1,holder.getMaxBid(),holder.getSellerName(),holder.getMaxBidder());
                    }
                    break;
            }
        }
        else if(menu.getHolder() instanceof CustomInventory.VaultHolder) //Player is trying to retrieve items
        {
            if (clickedInventory == menu) //Player is clicking the inventory.
            {
                CustomInventory.VaultHolder holder = (CustomInventory.VaultHolder) menu.getHolder();
                int slot = event.getSlot();

                switch (slot)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 17:
                    case 18:
                    case 26:
                    case 27:
                    case 35:
                    case 36:
                    case 37:
                    case 38:
                    case 40:
                    case 42:
                    case 43:
                    case 44:
                        event.setCancelled(true); //These are all the filler glass pane indexes, thus cancel the event.
                        ItemStack oldItem = event.getCurrentItem();
                        ItemStack cursorItem = event.getWhoClicked().getItemOnCursor();
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()->{
                            event.setCurrentItem(oldItem);
                            event.getWhoClicked().setItemOnCursor(cursorItem);
                            ((Player)event.getWhoClicked()).updateInventory();
                        },0);
                        break;
                    case 39:
                        //Go back a page.
                        event.setCancelled(true);
                        event.setCurrentItem(event.getCurrentItem());
                        event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());
                        ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        if(holder.getPage()>0)
                            CustomInventory.createItemVault((Player) event.getWhoClicked(),plugin,holder.getPage()-1);
                        else
                            CustomInventory.createAuctionWelcomeMenu((Player) event.getWhoClicked(),plugin);
                        break;
                    case 41:
                        //Go a page forward.
                        event.setCancelled(true);
                        event.setCurrentItem(event.getCurrentItem());
                        event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());
                        ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        if(holder.getPage() < 4)
                            CustomInventory.createItemVault((Player) event.getWhoClicked(),plugin,holder.getPage()+1);
                        break;
                    default:
                        //Player is clicking on an item on the vault menu.
                        if(event.isShiftClick()) //Player is shiftclicking to retrieve, no problem.
                            return;
                        //Player is not shift clicking, so we check the following:
                        //Does player have anything on his cursor?
                        if(!event.getWhoClicked().getItemOnCursor().getType().equals(Material.AIR))
                        {
                            event.setCancelled(true);
                            //Player is trying to put an item into the vault! Cancel it!
                        }
                        //If not, allow the click as player is just retrieving.
                        break;
                }
            }
            else
            {
                if (event.getClick().isShiftClick())  //Player is trying to do a shift click from his inventory, thus cancel it.
                {
                    event.setCancelled(true);
                }
            }
        }
        else if(menu.getHolder() instanceof CustomInventory.OldBidMenuHolder) //Player is checking his old bids.
        {
            CustomInventory.OldBidMenuHolder holder = (CustomInventory.OldBidMenuHolder) menu.getHolder();
            event.setCancelled(true);
            event.setCurrentItem(event.getCurrentItem());
            event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());
            if (!event.getClick().isShiftClick() && clickedInventory == menu) //Player is clicking the inventory.
            {

                int slot = event.getSlot();

                switch (slot)
                {
                    case 39:
                        //Go back a page.
                        event.getWhoClicked().setItemOnCursor(event.getWhoClicked().getItemOnCursor());
                        ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        if(holder.getPage()>0)
                            CustomInventory.createOldBidMenu((Player) event.getWhoClicked(),plugin,holder.getPage()-1);
                        else
                            CustomInventory.createAuctionWelcomeMenu((Player) event.getWhoClicked(),plugin);
                        break;
                    case 41:
                        //Go a page forward.
                        ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        if(holder.getPage() < 19)
                            CustomInventory.createOldBidMenu((Player) event.getWhoClicked(),plugin,holder.getPage()+1);
                        break;
                    default:
                        ((Player)event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK,1.0f,1.0f);
                        //Player is clicking on an item on the bid menu.
                        ItemStack clicked = menu.getItem(slot);
                        if(clicked != null && !clicked.getType().equals(Material.AIR))
                        {
                            ItemMeta clickedMeta = clicked.getItemMeta();
                            List<String> lore = clickedMeta.getLore();

                            int itemid = 0;
                            String maxBidder = null;
                            for(String s : lore)
                            {
                                if(s.contains("Eşya IDsi: "))
                                {
                                    s = ChatColor.stripColor(s);
                                    itemid = Integer.parseInt(s.substring(11,s.length()));
                                }
                                else if(s.contains("Teklif eden oyuncu: "))
                                {
                                    s = ChatColor.stripColor(s);
                                    maxBidder = s.substring(20,s.length());
                                }
                            }

                            try {
                                if (itemid != 0)
                                {

                                    //Now we have the id of the item.

                                    PreparedStatement ps = MainClass.conn.prepareStatement("select Seller,currentBid from auction_items where id=?");
                                    ps.setInt(1,itemid);
                                    ResultSet rs = ps.executeQuery();
                                    if(rs.next())
                                    {
                                        UUID uuid = UUID.fromString(rs.getString("Seller"));

                                        CustomInventory.createBiddingMenu((Player) event.getWhoClicked(),plugin,itemid,0,rs.getInt("currentBid"),Bukkit.getOfflinePlayer(uuid).getName(),maxBidder);
                                    }
                                    rs.close();
                                    rs = null;

                                    ps.close();
                                    ps = null;
                                }
                            }
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                            }

                            //Player clicked on an item that is not null.
                        }
                        break;
                }
            }
        }
    }

    @EventHandler
    public void onAuctionMenuClose(InventoryCloseEvent event)
    {
        if(event.getInventory().getHolder() instanceof CustomInventory.AuctionSearchHolder)
        {
            //plugin.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "Insıde the AuctionHolder close event.");
            CustomInventory.AuctionSearchHolder holder = (CustomInventory.AuctionSearchHolder) event.getInventory().getHolder();
            if(holder.getUpdateItemMeta() == null)
                return;
            holder.getUpdateItemMeta().removeFromHolderList(holder);
            holder.setUpdateItemMeta(null);
            holder = null;
        }
        else if(event.getInventory().getHolder() instanceof CustomInventory.OldBidMenuHolder)
        {
            //plugin.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "Insıde the BiddingHolder close event.");
            CustomInventory.OldBidMenuHolder holder = (CustomInventory.OldBidMenuHolder) event.getInventory().getHolder();
            if(holder.getUpdateItemMeta() == null)
                return;
            holder.getUpdateItemMeta().removeFromHolderList(holder);
            holder.setUpdateItemMeta(null);
            holder = null;
        }
        else if(event.getInventory().getHolder() instanceof CustomInventory.VaultHolder)
        {
            Inventory menu = event.getInventory();
            CustomInventory.VaultHolder holder = (CustomInventory.VaultHolder) menu.getHolder();
            try {
                //Player's vault is closed,
                //First remove the items from the db.
                PreparedStatement ps = MainClass.conn.prepareStatement("delete from player_boughtItems where player_id=? and page=?");
                ps.setString(1, String.valueOf(event.getPlayer().getUniqueId()));
                ps.setInt(2,(holder.getPage()+1));
                ps.executeUpdate();
                ps.close();
                //Then save them back into the db.
                for(int itemIndex = 10; itemIndex < 35;)
                {
                    //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + "Looping: " + itemIndex);
                    ItemStack item = menu.getItem(itemIndex);
                    if(item != null && !item.getType().equals(Material.AIR))
                    {
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.GREEN + "Found Item: " + item);
                        //The index is not empty! Push it to the DB.
                         ps = MainClass.conn.prepareStatement("insert into player_boughtItems values (?,?,?)");
                        String base64String = DBSaver.toBase64(item);
                        ps.setString(1, String.valueOf(event.getPlayer().getUniqueId()));
                        ps.setString(2,base64String);
                        ps.setInt(3,(holder.getPage()+1));
                        ps.executeUpdate();
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.YELLOW + "Page: " + (holder.getPage()+1));
                        //plugin.getServer().getConsoleSender().sendMessage(MainClass.getPluginPrefix() + ChatColor.BLUE + "______________________________________");
                    }

                    itemIndex++;
                    if(itemIndex == 17)
                        itemIndex = 19;
                    if(itemIndex == 26)
                        itemIndex = 28;
                }

                ps.close();

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        else if(event.getInventory().getHolder() instanceof CustomInventory.AuctionInventoryHolder)
        {
            CustomInventory.AuctionInventoryHolder holder = (CustomInventory.AuctionInventoryHolder) event.getInventory().getHolder();
            if(!holder.isChangingWindows() && holder.getAlreadyPut() != null && !holder.getAlreadyPut().getType().equals(Material.AIR))
            {
                event.getPlayer().getInventory().addItem(holder.getAlreadyPut());
            }
        }
    }

    @EventHandler
    public void itemDragToMarketMenu(InventoryDragEvent event)
    {
        if(event.isCancelled())
            return;

        if(event.getInventory().getHolder() instanceof CustomInventory.AuctionSearchHolder)
        {
            event.setCancelled(true);
        }
        else if(event.getView().getTitle().contains("Ürün koy"))
        {
            Inventory inv = event.getInventory();
            for(int index : event.getNewItems().keySet())
            {
                if(index != 13)
                    event.setCancelled(true);
            }
        }
        else if(event.getView().getTitle().contains("Market Menüsü"))
        {
            event.setCancelled(true);
        }
        else if(event.getInventory().getHolder() instanceof CustomInventory.VaultHolder)
        {
            event.setCancelled(true); //We do not want anything to be dragged into this inventory. As this inventory is just for retrieval purposes.
        }
    }
}
