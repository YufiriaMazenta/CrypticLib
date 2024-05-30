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

    private static final Map<Integer, Function<Entity, NbtEntity>> nbtEntityProviderMap;

    static {
        nbtEntityProviderMap = new ConcurrentHashMap<>();

        regNbtEntityProvider(11200, V1_12_R1NbtEntity::new);
        regNbtEntityProvider(11201, V1_12_R1NbtEntity::new);
        regNbtEntityProvider(11202, V1_12_R1NbtEntity::new);
        regNbtEntityProvider(11300, V1_13_R1NbtEntity::new);
        regNbtEntityProvider(11301, V1_13_R1NbtEntity::new);
        regNbtEntityProvider(11302, V1_13_R2NbtEntity::new);
        regNbtEntityProvider(11400, V1_14_R1NbtEntity::new);
        regNbtEntityProvider(11401, V1_14_R1NbtEntity::new);
        regNbtEntityProvider(11402, V1_14_R1NbtEntity::new);
        regNbtEntityProvider(11403, V1_14_R1NbtEntity::new);
        regNbtEntityProvider(11404, V1_14_R1NbtEntity::new);
        regNbtEntityProvider(11500, V1_15_R1NbtEntity::new);
        regNbtEntityProvider(11501, V1_15_R1NbtEntity::new);
        regNbtEntityProvider(11502, V1_15_R1NbtEntity::new);
        regNbtEntityProvider(11600, V1_16_R1NbtEntity::new);
        regNbtEntityProvider(11601, V1_16_R1NbtEntity::new);
        regNbtEntityProvider(11602, V1_16_R1NbtEntity::new);
        regNbtEntityProvider(11603, V1_16_R2NbtEntity::new);
        regNbtEntityProvider(11604, V1_16_R3NbtEntity::new);
        regNbtEntityProvider(11605, V1_16_R3NbtEntity::new);
        regNbtEntityProvider(11700, V1_17_R1NbtEntity::new);
        regNbtEntityProvider(11701, V1_17_R1NbtEntity::new);
        regNbtEntityProvider(11800, V1_18_R1NbtEntity::new);
        regNbtEntityProvider(11801, V1_18_R1NbtEntity::new);
        regNbtEntityProvider(11802, V1_18_R2NbtEntity::new);
        regNbtEntityProvider(11900, V1_19_R1NbtEntity::new);
        regNbtEntityProvider(11901, V1_19_R1NbtEntity::new);
        regNbtEntityProvider(11902, V1_19_R2NbtEntity::new);
        regNbtEntityProvider(11903, V1_19_R3NbtEntity::new);
        regNbtEntityProvider(11904, V1_19_R3NbtEntity::new);
        regNbtEntityProvider(12000, V1_20_R1NbtEntity::new);
        regNbtEntityProvider(12001, V1_20_R1NbtEntity::new);
        regNbtEntityProvider(12002, V1_20_R2NbtEntity::new);
        regNbtEntityProvider(12003, V1_20_R3NbtEntity::new);
        regNbtEntityProvider(12004, V1_20_R3NbtEntity::new);
    }

    /**
     * 获取一个代理实体对象,用于直接修改NBT
     *
     * @param entity 需要代理的实体
     * @return 代理实体对象
     */
    public static NbtEntity entity(Entity entity) {
        return nbtEntityProviderMap.getOrDefault(CrypticLib.minecraftVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(entity);
    }

    public static void regNbtEntityProvider(Integer minecraftVersion, Function<Entity, NbtEntity> proxyEntityFunction) {
        nbtEntityProviderMap.put(minecraftVersion, proxyEntityFunction);
    }

}
