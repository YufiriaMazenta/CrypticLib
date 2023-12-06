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

    private static final Map<String, Function<Entity, NbtEntity>> entityProviderMap;

    static {
        entityProviderMap = new ConcurrentHashMap<>();

        regProvider("v1_12_R1", V1_12_R1NbtEntity::new);
        regProvider("v1_13_R1", V1_13_R1NbtEntity::new);
        regProvider("v1_13_R2", V1_13_R2NbtEntity::new);
        regProvider("v1_14_R1", V1_14_R1NbtEntity::new);
        regProvider("v1_15_R1", V1_15_R1NbtEntity::new);
        regProvider("v1_16_R1", V1_16_R1NbtEntity::new);
        regProvider("v1_16_R2", V1_16_R2NbtEntity::new);
        regProvider("v1_16_R3", V1_16_R3NbtEntity::new);
        regProvider("v1_17_R1", V1_17_R1NbtEntity::new);
        regProvider("v1_18_R1", V1_18_R1NbtEntity::new);
        regProvider("v1_18_R2", V1_18_R2NbtEntity::new);
        regProvider("v1_19_R1", V1_19_R1NbtEntity::new);
        regProvider("v1_19_R2", V1_19_R2NbtEntity::new);
        regProvider("v1_19_R3", V1_19_R3NbtEntity::new);
        regProvider("v1_20_R1", V1_20_R1NbtEntity::new);
        regProvider("v1_20_R2", V1_20_R2NbtEntity::new);
        regProvider("v1_20_R3", V1_20_R3NbtEntity::new);
    }

    /**
     * 获取一个代理实体对象,用于直接修改NBT
     *
     * @param entity 需要代理的实体
     * @return 代理实体对象
     */
    public static NbtEntity entity(Entity entity) {
        return entityProviderMap.getOrDefault(CrypticLib.nmsVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(entity);
    }

    public static void regProvider(String nmsVersion, Function<Entity, NbtEntity> proxyEntityFunction) {
        entityProviderMap.put(nmsVersion, proxyEntityFunction);
    }

}
