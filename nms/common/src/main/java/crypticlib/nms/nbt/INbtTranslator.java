package crypticlib.nms.nbt;

public interface INbtTranslator {

    INbtTag<?> translateNmsNbt(Object nmsNbt);

    INbtTag<?> translateObject(Object object);

}
