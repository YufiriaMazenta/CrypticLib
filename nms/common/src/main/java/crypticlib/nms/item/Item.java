package crypticlib.nms.item;

import com.google.gson.JsonObject;
import crypticlib.nms.nbt.NbtTagCompound;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public abstract class Item {

    private String material;
    private NbtTagCompound nbtTagCompound;

    public Item(String material, NbtTagCompound nbtTagCompound) {
        this.material = material;
        this.nbtTagCompound = nbtTagCompound;
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
        return jsonObject;
    }

}
