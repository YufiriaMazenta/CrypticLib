package crypticlib.nms.item;

import com.google.gson.JsonObject;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.util.JsonUtil;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Item {

    private String material;
    private NbtTagCompound nbtTagCompound;
    private Integer amount;

    public Item(String material, NbtTagCompound nbtTagCompound) {
        this(material, nbtTagCompound, 1);
    }

    public Item(String material, NbtTagCompound nbtTagCompound, Integer amount) {
        this.material = material;
        this.nbtTagCompound = nbtTagCompound;
        this.amount = amount;
    }

    /**
     * 获取物品的类型
     * @return 物品的类型
     */
    public String material() {
        return material;
    }

    /**
     * 设置物品的类型
     * @param material 物品的类型
     */
    public Item setMaterial(String material) {
        this.material = material;
        return this;
    }

    /**
     * 获取物品的Nbt,可以直接修改
     * @return 物品的nbt
     */
    public NbtTagCompound nbtTagCompound() {
        return nbtTagCompound;
    }

    /**
     * 设置物品的nbt,会覆盖原有的nbt
     * @param nbtCompound 需要设置过来的nbt
     */
    public Item setNbtTagCompound(NbtTagCompound nbtCompound) {
        this.nbtTagCompound = nbtCompound;
        return this;
    }

    /**
     * 获取物品的数量
     * @return 物品的数量
     */
    public Integer amount() {
        return amount;
    }

    /**
     * 设置物品的数量
     * @param amount 设置的数量
     */
    public Item setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    /**
     * 构建一个Bukkit物品
     * @return 构建的bukkit物品
     */
    public abstract ItemStack buildBukkit();

    /**
     * 构建一个NMS的物品
     * @return 构建的nms物品
     */
    public abstract Object buildNMS();

    /**
     * 将物品序列化为map
     * @return 序列化的map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material());
        map.put("nbt", nbtTagCompound().unwarppedMap());
        map.put("amount", amount());
        return map;
    }

    /**
     * 将物品序列化为json
     * @return 序列化的json
     */
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("material", material());
        jsonObject.add("nbt", nbtTagCompound().toJson());
        jsonObject.addProperty("amount", amount());
        return jsonObject;
    }

    public HoverEvent toHover() {
        return new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new net.md_5.bungee.api.chat.hover.content.Item(
                        Objects.requireNonNull(Material.matchMaterial(material)).getKey().toString(),
                        amount,
                        ItemTag.ofNbt(nbtTagCompound.toString()))
        );
    }

    @Override
    public String toString() {
        return JsonUtil.json2Str(toJson());
    }

}
