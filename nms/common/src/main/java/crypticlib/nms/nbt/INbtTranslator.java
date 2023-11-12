package crypticlib.nms.nbt;

public interface INbtTranslator {

    /**
     * 翻译nms的nbt对象为CrypticLib的nbt对象
     * @param nmsNbt nms的nbt对象
     * @return 转化的CrypticLib nbt对象
     */
    INbtTag<?> translateNmsNbt(Object nmsNbt);

    /**
     * 翻译Object为CrypticLib的nbt对象
     * 只支持基础类型包装类,String,基础类型数组,nms的nbt对象和CrypticLib本身的nbt对象
     * @param object 传入的object
     * @return 转化的CrypticLib nbt对象
     */
    INbtTag<?> translateObject(Object object);

}
