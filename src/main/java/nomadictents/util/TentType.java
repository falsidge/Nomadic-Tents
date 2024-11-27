package nomadictents.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public enum TentType implements StringRepresentable {
    YURT("yurt"),
    TEPEE("tepee"),
    BEDOUIN("bedouin"),
    INDLU("indlu"),
    SHAMIYANA("shamiyana");

    public static final Codec<TentType> CODEC = Codec.STRING.comapFlatMap(TentType::getByName, TentType::getSerializedName).stable();

    private final String name;

    TentType(String name) {
        this.name = name;
    }

    public static DataResult<TentType> getByName(String id) {
        for (final TentType t : values()) {
            if (t.getSerializedName().equals(id)) {
                return DataResult.success(t);
            }
        }

        Supplier<String> supplier = () -> "Failed to parse tent size '" + id + "'";
        return DataResult.error(supplier);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
