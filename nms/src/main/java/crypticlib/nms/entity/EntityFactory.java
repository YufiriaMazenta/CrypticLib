package crypticlib.nms.entity;

import crypticlib.CrypticLib;
import crypticlib.nms.entity.v1_12_R1.V1_12_R1NbtEntity;
import crypticlib.nms.entity.v1_13_R1.V1_13_R1NbtEntity;
import crypticlib.nms.entity.v1_13_R2.V1_13_R2NbtEntity;
import crypticlib.nms.entity.v1_14_R1.V1_14_R1NbtEntity;
import crypticlib.nms.entity.v1_15_R1.V1_15_R1NbtEntity;
import crypticlib.nms.entity.v1_16_R1.V1_16_R1NbtEntity;
import crypticlib.nms.entity.v1_16_R2.V1_16_R2NbtEntity;
import crypticlib.nms.entity.v1_16_R3.V1_16_R3NbtEntity;
import crypticlib.nms.entity.v1_17_R1.V1_17_R1NbtEntity;
import crypticlib.nms.entity.v1_18_R1.V1_18_R1NbtEntity;
import crypticlib.nms.entity.v1_18_R2.V1_18_R2NbtEntity;
import crypticlib.nms.entity.v1_19_R1.V1_19_R1NbtEntity;
import crypticlib.nms.entity.v1_19_R2.V1_19_R2NbtEntity;
import crypticlib.nms.entity.v1_19_R3.V1_19_R3NbtEntity;
import crypticlib.nms.entity.v1_20_R1.V1_20_R1NbtEntity;
import crypticlib.nms.entity.v1_20_R2.V1_20_R2NbtEntity;
import crypticlib.nms.entity.v1_20_R3.V1_20_R3NbtEntity;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EntityFactory {

    private static final Map<String, Function<Entity, NbtEntity>> nbtEntityProviderMap;

    static {
        nbtEntityProviderMap = new ConcurrentHashMap<>();

        regNbtEntityProvider("v1_12_R1", V1_12_R1NbtEntity::new);
        regNbtEntityProvider("v1_13_R1", V1_13_R1NbtEntity::new);
        regNbtEntityProvider("v1_13_R2", V1_13_R2NbtEntity::new);
        regNbtEntityProvider("v1_14_R1", V1_14_R1NbtEntity::new);
        regNbtEntityProvider("v1_15_R1", V1_15_R1NbtEntity::new);
        regNbtEntityProvider("v1_16_R1", V1_16_R1NbtEntity::new);
        regNbtEntityProvider("v1_16_R2", V1_16_R2NbtEntity::new);
        regNbtEntityProvider("v1_16_R3", V1_16_R3NbtEntity::new);
        regNbtEntityProvider("v1_17_R1", V1_17_R1NbtEntity::new);
        regNbtEntityProvider("v1_18_R1", V1_18_R1NbtEntity::new);
        regNbtEntityProvider("v1_18_R2", V1_18_R2NbtEntity::new);
        regNbtEntityProvider("v1_19_R1", V1_19_R1NbtEntity::new);
        regNbtEntityProvider("v1_19_R2", V1_19_R2NbtEntity::new);
        regNbtEntityProvider("v1_19_R3", V1_19_R3NbtEntity::new);
        regNbtEntityProvider("v1_20_R1", V1_20_R1NbtEntity::new);
        regNbtEntityProvider("v1_20_R2", V1_20_R2NbtEntity::new);
        regNbtEntityProvider("v1_20_R3", V1_20_R3NbtEntity::new);
    }

    /**
     * 获取一个代理实体对象,用于直接修改NBT
     *
     * @param entity 需要代理的实体
     * @return 代理实体对象
     */
    public static NbtEntity entity(Entity entity) {
        return nbtEntityProviderMap.getOrDefault(CrypticLib.nmsVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(entity);
    }

    public static void regNbtEntityProvider(String nmsVersion, Function<Entity, NbtEntity> proxyEntityFunction) {
        nbtEntityProviderMap.put(nmsVersion, proxyEntityFunction);
    }

}
