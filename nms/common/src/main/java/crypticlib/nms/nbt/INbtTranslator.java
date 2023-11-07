package crypticlib.nms.nbt;

public interface INbtTranslator {

    INbtTag<?> fromNms(Object nmsNbt);

    INbtTag<?> fromObject(Object object);

}
