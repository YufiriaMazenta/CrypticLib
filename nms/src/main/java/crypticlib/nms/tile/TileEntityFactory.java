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

    private static final Map<String, Function<BlockState, NbtTileEntity>> nbtTileEntityProviderMap;

    static {
        nbtTileEntityProviderMap = new ConcurrentHashMap<>();

        regNbtTileEntityProvider("v1_12_R1", V1_12_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_13_R1", V1_13_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_13_R2", V1_13_R2NbtTileEntity::new);
        regNbtTileEntityProvider("v1_14_R1", V1_14_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_15_R1", V1_15_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_16_R1", V1_16_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_16_R2", V1_16_R2NbtTileEntity::new);
        regNbtTileEntityProvider("v1_16_R3", V1_16_R3NbtTileEntity::new);
        regNbtTileEntityProvider("v1_17_R1", V1_17_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_18_R1", V1_18_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_18_R2", V1_18_R2NbtTileEntity::new);
        regNbtTileEntityProvider("v1_19_R1", V1_19_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_19_R2", V1_19_R2NbtTileEntity::new);
        regNbtTileEntityProvider("v1_19_R3", V1_19_R3NbtTileEntity::new);
        regNbtTileEntityProvider("v1_20_R1", V1_20_R1NbtTileEntity::new);
        regNbtTileEntityProvider("v1_20_R2", V1_20_R2NbtTileEntity::new);
        regNbtTileEntityProvider("v1_20_R3", V1_20_R3NbtTileEntity::new);
    }

    public static NbtTileEntity tileEntity(BlockState blockState) {
        return nbtTileEntityProviderMap.getOrDefault(CrypticLib.nmsVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(blockState);
    }

    public static void regNbtTileEntityProvider(String nmsVersion, Function<BlockState, NbtTileEntity> nbtTileEntityProvider) {
        nbtTileEntityProviderMap.put(nmsVersion, nbtTileEntityProvider);
    }

}
