package crypticlib.particle.pobject;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class Point extends ParticleObject {

    /**
     * 构造一个点
     * @param origin 点的原始坐标
     */
    public Point(@NotNull Location origin) {
        this.originLocation = origin;
    }

    @Override
    public List<Location> calculateLocations() {
        Location loc = getOriginLocation().clone();
        loc.add(getIncrementX(), getIncrementY(), getIncrementZ());
        return Collections.singletonList(loc);
    }

    @Override
    public void show() {
        spawnParticle(getOriginLocation().clone());
    }

}
