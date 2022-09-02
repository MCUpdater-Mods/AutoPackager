package com.mcupdater.autopackager.setup;

import com.mcupdater.autopackager.block.BlockPackager;
import com.mcupdater.autopackager.block.PackagerEntity;
import com.mcupdater.autopackager.block.PackagerMenu;
import com.mcupdater.mculib.helpers.DataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.mcupdater.autopackager.AutoPackager.MODID;
import static com.mcupdater.mculib.setup.ModSetup.MCULIB_ITEM_GROUP;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CONTAINERS.register(modEventBus);
    }

    public static final RegistryObject<BlockPackager> PACKAGERBLOCK = BLOCKS.register("autopackager", BlockPackager::new);
    public static final RegistryObject<Item> PACKAGERBLOCK_ITEM = ITEMS.register("autopackager", () -> new BlockItem(PACKAGERBLOCK.get(), new Item.Properties().tab(MCULIB_ITEM_GROUP)));
    public static final RegistryObject<BlockEntityType<PackagerEntity>> PACKAGERBLOCK_ENTITY = BLOCK_ENTITIES.register("autopackager", () -> BlockEntityType.Builder.of(PackagerEntity::new, PACKAGERBLOCK.get()).build(null));
    public static final RegistryObject<MenuType<PackagerMenu>> PACKAGERBLOCK_MENU = CONTAINERS.register("autopackager", () -> IForgeMenuType.create((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        Level level = inv.player.level;
        PackagerEntity te = (PackagerEntity) level.getBlockEntity(pos);
        return new PackagerMenu(windowId, level, pos, inv, inv.player, te.modeData, DataHelper.readDirectionMap(data));
    }));

}
