package crypticlib.nms.tile;

import crypticlib.CrypticLib;
import crypticlib.nms.tile.v1_12_R1.V1_12_R1NbtTileEntity;
import crypticlib.nms.tile.v1_13_R1.V1_13_R1NbtTileEntity;
import crypticlib.nms.tile.v1_13_R2.V1_13_R2NbtTileEntity;
import crypticlib.nms.tile.v1_14_R1.V1_14_R1NbtTileEntity;
import crypticlib.nms.tile.v1_15_R1.V1_15_R1NbtTileEntity;
import crypticlib.nms.tile.v1_16_R1.V1_16_R1NbtTileEntity;
import crypticlib.nms.tile.v1_16_R2.V1_16_R2NbtTileEntity;
import crypticlib.nms.tile.v1_16_R3.V1_16_R3NbtTileEntity;
import crypticlib.nms.tile.v1_17_R1.V1_17_R1NbtTileEntity;
import crypticlib.nms.tile.v1_18_R1.V1_18_R1NbtTileEntity;
import crypticlib.nms.tile.v1_18_R2.V1_18_R2NbtTileEntity;
import crypticlib.nms.tile.v1_19_R1.V1_19_R1NbtTileEntity;
import crypticlib.nms.tile.v1_19_R2.V1_19_R2NbtTileEntity;
import crypticlib.nms.tile.v1_19_R3.V1_19_R3NbtTileEntity;
import crypticlib.nms.tile.v1_20_R1.V1_20_R1NbtTileEntity;
import crypticlib.nms.tile.v1_20_R2.V1_20_R2NbtTileEntity;
import crypticlib.nms.tile.v1_20_R3.V1_20_R3NbtTileEntity;
import org.bukkit.block.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class TileEntityFactory {

    private static final Map<Integer, Function<BlockState, NbtTileEntity>> nbtTileEntityProviderMap;

    static {
        nbtTileEntityProviderMap = new ConcurrentHashMap<>();

        regNbtTileEntityProvider(11200, V1_12_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11201, V1_12_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11202, V1_12_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11300, V1_13_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11301, V1_13_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11302, V1_13_R2NbtTileEntity::new);
        regNbtTileEntityProvider(11400, V1_14_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11401, V1_14_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11402, V1_14_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11403, V1_14_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11404, V1_14_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11500, V1_15_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11501, V1_15_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11502, V1_15_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11600, V1_16_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11601, V1_16_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11602, V1_16_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11603, V1_16_R2NbtTileEntity::new);
        regNbtTileEntityProvider(11604, V1_16_R3NbtTileEntity::new);
        regNbtTileEntityProvider(11605, V1_16_R3NbtTileEntity::new);
        regNbtTileEntityProvider(11700, V1_17_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11701, V1_17_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11800, V1_18_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11801, V1_18_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11802, V1_18_R2NbtTileEntity::new);
        regNbtTileEntityProvider(11900, V1_19_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11901, V1_19_R1NbtTileEntity::new);
        regNbtTileEntityProvider(11902, V1_19_R2NbtTileEntity::new);
        regNbtTileEntityProvider(11903, V1_19_R3NbtTileEntity::new);
        regNbtTileEntityProvider(11904, V1_19_R3NbtTileEntity::new);
        regNbtTileEntityProvider(12000, V1_20_R1NbtTileEntity::new);
        regNbtTileEntityProvider(12001, V1_20_R1NbtTileEntity::new);
        regNbtTileEntityProvider(12002, V1_20_R2NbtTileEntity::new);
        regNbtTileEntityProvider(12003, V1_20_R3NbtTileEntity::new);
    }

    public static NbtTileEntity tileEntity(BlockState blockState) {
        return nbtTileEntityProviderMap.getOrDefault(CrypticLib.minecraftVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(blockState);
    }

    public static void regNbtTileEntityProvider(Integer minecraftVersion, Function<BlockState, NbtTileEntity> nbtTileEntityProvider) {
        nbtTileEntityProviderMap.put(minecraftVersion, nbtTileEntityProvider);
    }

}
