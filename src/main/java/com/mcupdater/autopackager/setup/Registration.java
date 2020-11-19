package com.mcupdater.autopackager.setup;

import com.mcupdater.autopackager.tile.BlockPackager;
import com.mcupdater.autopackager.tile.ContainerPackager;
import com.mcupdater.autopackager.tile.TilePackager;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static com.mcupdater.autopackager.AutoPackager.MODID;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<BlockPackager> PACKAGERBLOCK = BLOCKS.register("autopackager", BlockPackager::new);
    public static final RegistryObject<Item> PACKAGERBLOCK_ITEM = ITEMS.register("autopackager", () -> new BlockItem(PACKAGERBLOCK.get(), new Item.Properties().group(ModSetup.ITEM_GROUP)));
    public static final RegistryObject<TileEntityType<TilePackager>> PACKAGERBLOCK_TILE = TILES.register("autopackager", () -> TileEntityType.Builder.create(TilePackager::new, PACKAGERBLOCK.get()).build(null));
    public static final RegistryObject<ContainerType<ContainerPackager>> PACKAGERBLOCK_CONTAINER = CONTAINERS.register("autopackager", () -> IForgeContainerType.create((windowId, inv, data) -> {
        BlockPos pos = data.readBlockPos();
        World world = inv.player.getEntityWorld();
        TilePackager te = (TilePackager) world.getTileEntity(pos);
        return new ContainerPackager(windowId, world, pos, inv, inv.player, te.modeData);
    }));

}
