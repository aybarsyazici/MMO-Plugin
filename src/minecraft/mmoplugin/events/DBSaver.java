package minecraft.mmoplugin.events;

import net.minecraft.server.v1_16_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.math.BigInteger;

public class DBSaver
{
    public static String toBase64(ItemStack item){
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(outputStream);

            NBTTagList nbtTagListItems = new NBTTagList();
            NBTTagCompound nbtTagCompoundItem = new NBTTagCompound();

            net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);

            nmsItem.save(nbtTagCompoundItem);

            nbtTagListItems.add(nbtTagCompoundItem);
            NBTCompressedStreamTools.a(nbtTagCompoundItem, (DataOutput) dataOutput);
            return new BigInteger(1, outputStream.toByteArray()).toString(32);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Item from Base64
     * @param data
     * @return
     */
    public static ItemStack fromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());

            NBTTagCompound nbtTagCompoundRoot = NBTCompressedStreamTools.a(new DataInputStream(inputStream));
            net.minecraft.server.v1_16_R1.ItemStack nmsItem = net.minecraft.server.v1_16_R1.ItemStack.a(nbtTagCompoundRoot);
            ItemStack item = (ItemStack) CraftItemStack.asBukkitCopy(nmsItem);

            return item;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
