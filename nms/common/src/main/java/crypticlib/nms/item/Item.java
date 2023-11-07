package crypticlib.nms.item;

import com.google.gson.JsonObject;
import crypticlib.nms.nbt.AbstractNbtCompound;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public interface Item {

    String material();

    void setMaterial(String material);

    AbstractNbtCompound nbtCompound();

    void setNbtCompound(AbstractNbtCompound nbtCompound);

    ItemStack buildBukkit();

    Object buildNMS();

    default Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("material", material());
        map.put("nbt", nbtCompound().unwrappedValue());
        return map;
    }

    default JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("material", material());
        jsonObject.add("nbt", nbtCompound().toJson());
        return jsonObject;
    }

}
