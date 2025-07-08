package xerca.xercapaint.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static xerca.xercapaint.item.Items.CRAFTING_TAGLESS_SHAPED;

public class RecipeTaglessShaped extends ShapedRecipe {
    public RecipeTaglessShaped(ShapedRecipe shapedRecipe){
        super(shapedRecipe.getId(), shapedRecipe.getGroup(), shapedRecipe.category(), shapedRecipe.getWidth(), shapedRecipe.getHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem(RegistryAccess.EMPTY));
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(@NotNull CraftingContainer inv, @NotNull Level worldIn) {
        if(super.matches(inv, worldIn)){
            for(int j = 0; j < inv.getContainerSize(); ++j) {
                ItemStack stackInSlot = inv.getItem(j);
                if (!stackInSlot.isEmpty() && stackInSlot.hasTag()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess access) {
        ItemStack result = super.assemble(inv, access);
        if(!result.isEmpty()){
            for(int j = 0; j < inv.getContainerSize(); ++j) {
                ItemStack stackInSlot = inv.getItem(j);
                if (!stackInSlot.isEmpty() && stackInSlot.hasTag()) {
                    return ItemStack.EMPTY;
                }
            }

            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CRAFTING_TAGLESS_SHAPED;
    }

    public static class TaglessSerializer implements RecipeSerializer<RecipeTaglessShaped> {
        private static final Serializer shapedSerializer = new Serializer();

        public TaglessSerializer(){}

        public @NotNull RecipeTaglessShaped fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            ShapedRecipe shapedRecipe = shapedSerializer.fromJson(recipeId, json);
            return new RecipeTaglessShaped(shapedRecipe);
        }

        public @NotNull RecipeTaglessShaped fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
            ShapedRecipe shapedRecipe = shapedSerializer.fromNetwork(recipeId, buffer);
            return new RecipeTaglessShaped(shapedRecipe);
        }

        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull RecipeTaglessShaped recipe) {
            shapedSerializer.toNetwork(buffer, recipe);
        }

    }
}