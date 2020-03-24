package net.dries007.tfc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import net.dries007.tfc.network.ChunkDataRequestPacket;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.types.TFCTypeManager;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataCapability;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.vein.VeinTypeRegistry;

@Mod.EventBusSubscriber(modid = TerraFirmaCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventHandler
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCreateWorldSpawn(WorldEvent.CreateSpawnPosition event)
    {
        if (event.getWorld().getWorldInfo().getGenerator() == TerraFirmaCraft.getWorldType())
        {
            // todo: handle this better
            event.getWorld().getWorldInfo().setSpawn(new BlockPos(0, 100, 0));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesChunk(AttachCapabilitiesEvent<Chunk> event)
    {
        World world = event.getObject().getWorld();
        if (world.getWorldType() == TerraFirmaCraft.getWorldType())
        {
            // Add the rock data to the chunk capability, for long term storage
            ChunkPos pos = event.getObject().getPos();
            ChunkData data;
            ChunkDataProvider chunkDataProvider = ChunkDataProvider.get(world);
            if (chunkDataProvider != null)
            {
                data = chunkDataProvider.get(event.getObject());
            }
            else
            {
                data = new ChunkData();
            }

            event.addCapability(ChunkDataCapability.KEY, data);
        }
    }

    @SubscribeEvent
    public static void beforeServerStart(FMLServerAboutToStartEvent event)
    {
        LOGGER.debug("Before Server Start");

        // Initializes json data listeners
        TFCTypeManager.init(event.getServer().getResourceManager());
        event.getServer().getResourceManager().addReloadListener(VeinTypeRegistry.INSTANCE);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event)
    {
        LOGGER.info("Common Setup");

        ChunkDataCapability.setup();
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event)
    {
        /*ChunkPos pos = event.getPos();
        if (pos != null)
        {
            //TerraFirmaCraft.getLog().info("[CHUNK TEST] Watch: " + pos);
            ChunkData.get(event.getWorld().getChunk(pos.asBlockPos())).ifPresent(data -> {
                // Update server side climate cache
                ClimateTFC.update(pos, data.getRegionalTemp(), data.getRainfall());

                // Update client side data
                PacketHandler.get().send(PacketDistributor.PLAYER.with(event::getPlayer), new ChunkDataPacket(pos.x, pos.z, data));
            });
        }*/
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        // Send updates of all loaded chunks to the player logging in
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event)
    {
        //TerraFirmaCraft.getLog().info("[CHUNK TEST] Load: {}", load.getChunk().getPos());
        if (event.getWorld().isRemote())
        {
            // Client
            // Ask the server for the chunk data and climate information
            ChunkPos pos = event.getChunk().getPos();
            PacketHandler.get().send(PacketDistributor.SERVER.noArg(), new ChunkDataRequestPacket(pos.x, pos.z));
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event)
    {
        //TerraFirmaCraft.getLog().info("[CHUNK TEST] Unload: {}", unload.getChunk().getPos());
    }
}