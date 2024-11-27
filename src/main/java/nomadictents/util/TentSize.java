package nomadictents.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public enum TentSize implements StringRepresentable {
    TINY("tiny", ChatFormatting.RED),
    SMALL("small", ChatFormatting.BLUE),
    MEDIUM("medium", ChatFormatting.DARK_GREEN),
    LARGE("large", ChatFormatting.YELLOW),
    GIANT("giant", ChatFormatting.DARK_PURPLE),
    MEGA("mega", ChatFormatting.AQUA);

    public static final Codec<TentSize> CODEC = Codec.STRING.comapFlatMap(TentSize::getByName, TentSize::getSerializedName).stable();

    private final String name;
    private final ChatFormatting color;

    TentSize(String name, ChatFormatting color) {
        this.name = name;
        this.color = color;
    }

    public static DataResult<TentSize> getByName(String id) {
        for (final TentSize t : values()) {
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

    public ChatFormatting getColor() {
        return color;
    }
}
