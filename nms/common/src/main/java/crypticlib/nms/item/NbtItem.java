package crypticlib.nms.item;

import com.google.gson.JsonObject;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.util.ItemUtil;
import crypticlib.util.JsonUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class NbtItem {

    private final ItemStack bukkit;
    private NbtTagCompound nbtTagCompound;

    public NbtItem(@NotNull ItemStack itemStack) {
        if (ItemUtil.isAir(itemStack)) {
            throw new IllegalArgumentException("Can not create an air item");
        }
        this.bukkit = itemStack;
        loadNbtFromBukkit();
    }

    public NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound) {
        this(material, nbtTagCompound, 1);
    }

    public NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound, @NotNull Integer amount) {
        this.bukkit = new ItemStack(material, amount);
        this.nbtTagCompound = nbtTagCompound;
    }

    protected ItemStack bukkit() {
        return bukkit;
    }

    public Integer amount() {
        return bukkit.getAmount();
    }

    public NbtItem setAmount(@NotNull Integer amount) {
        bukkit.setAmount(amount);
        return this;
    }

    public Material material() {
        return bukkit.getType();
    }

    public NbtItem setMaterial(@NotNull Material material) {
        this.bukkit.setType(material);
        return this;
    }

    public NbtTagCompound nbtTagCompound() {
        return nbtTagCompound;
    }

    public NbtItem setNbtTagCompound(NbtTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
        return this;
    }

    @NotNull
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material());
        map.put("nbt", nbtTagCompound().unwarppedMap());
        map.put("amount", amount());
        return map;
    }

    /**
     * 将物品序列化为json
     *
     * @return 序列化的json
     */
    @NotNull
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("material", bukkit.getType().toString());
        jsonObject.add("nbt", nbtTagCompound().toJson());
        jsonObject.addProperty("amount", bukkit.getAmount());
        return jsonObject;
    }

    @NotNull
    public HoverEvent toHover() {
        return new HoverEvent(
            HoverEvent.Action.SHOW_ITEM,
            new net.md_5.bungee.api.chat.hover.content.Item(
                bukkit.getType().getKey().toString(),
                bukkit.getAmount(),
                ItemTag.ofNbt(nbtTagCompound.toString()))
        );
    }

    @Override
    public String toString() {
        return JsonUtil.json2Str(toJson());
    }

    public abstract void loadNbtFromBukkit();

    /**
     * 将NBT保存到物品上,同时返回修改完成的物品
     * @return 修改完成的物品
     */
    public abstract ItemStack saveNbtToBukkit();

}
