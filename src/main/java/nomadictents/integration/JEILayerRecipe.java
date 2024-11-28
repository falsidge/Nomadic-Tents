package nomadictents.integration;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import nomadictents.item.TentItem;
import nomadictents.recipe.TentLayerRecipe;
import nomadictents.util.Tent;

import java.util.List;
import java.util.function.Consumer;

public class JEILayerRecipe implements ICraftingCategoryExtension{

    private final Consumer<ItemStack> layerConsumer;
    private final TentLayerRecipe tentLayerRecipe;

    public JEILayerRecipe(TentLayerRecipe recipe) {
        super();
        tentLayerRecipe = recipe;
        final byte layer = (byte) Math.max(0, recipe.getLayer() - 1);
        layerConsumer = i -> {
            if (i.getItem() instanceof TentItem) {
                i.getOrCreateTag().putByte(Tent.LAYERS, layer);
            }
        };
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        List<List<ItemStack>> inputs = tentLayerRecipe.getIngredients().stream()
                .map(ingredient -> List.of(ingredient.getItems()))
                .toList();
        inputs.forEach(list -> list.forEach(layerConsumer));
        ItemStack resultItem = tentLayerRecipe.getResultItem(RegistryAccess.EMPTY);

        craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, inputs, getWidth(), getHeight());
        craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, List.of(resultItem));
    }
}
