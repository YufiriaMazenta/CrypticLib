package crypticlib.nms.item;

import com.google.gson.JsonObject;
import crypticlib.nms.nbt.AbstractNbtTagCompound;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public interface Item {

    /**
     * 获取物品的类型
     * @return 物品的类型
     */
    String material();

    /**
     * 设置物品的类型
     * @param material 物品的类型
     */
    void setMaterial(String material);

    /**
     * 获取物品的Nbt,可以直接修改
     * @return 物品的nbt
     */
    AbstractNbtTagCompound nbtTagCompound();

    /**
     * 设置物品的nbt,会覆盖原有的nbt
     * @param nbtCompound 需要设置过来的nbt
     */
    void setNbtTagCompound(AbstractNbtTagCompound nbtCompound);

    /**
     * 构建一个Bukkit物品
     * @return 构建的bukkit物品
     */
    ItemStack buildBukkit();

    /**
     * 构建一个NMS的物品
     * @return 构建的nms物品
     */
    Object buildNMS();

    /**
     * 将物品序列化为map
     * @return 序列化的map
     */
    default Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material());
        map.put("nbt", nbtTagCompound().unwarppedMap());
        return map;
    }

    /**
     * 将物品序列化为json
     * @return 序列化的json
     */
    default JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("material", material());
        jsonObject.add("nbt", nbtTagCompound().toJson());
        return jsonObject;
    }

}
