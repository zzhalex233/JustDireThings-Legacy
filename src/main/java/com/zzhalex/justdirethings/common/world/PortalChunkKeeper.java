package com.zzhalex.justdirethings.common.world;

import com.zzhalex.justdirethings.JustDireThingsLegacy;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PortalChunkKeeper {

    private static final ConcurrentMap<UUID, Set<String>> TRACKED_CHUNKS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<UUID, TicketHandle> ACTIVE_TICKETS = new ConcurrentHashMap<>();
    private static volatile boolean initialized;

    private PortalChunkKeeper() {
    }

    public static void initialize() {
        if (initialized || JustDireThingsLegacy.INSTANCE == null) {
            return;
        }
        ForgeChunkManager.setForcedChunkLoadingCallback(JustDireThingsLegacy.INSTANCE, PortalChunkKeeper::ticketsLoaded);
        initialized = true;
    }

    public static String chunkKey(BlockPos pos) {
        return (pos.getX() >> 4) + "," + (pos.getZ() >> 4);
    }

    public static void track(UUID portalId, BlockPos pos) {
        if (portalId == null || pos == null) {
            return;
        }
        TRACKED_CHUNKS.put(portalId, Collections.singleton(chunkKey(pos)));
    }

    public static void track(UUID portalId, World world, BlockPos pos) {
        track(portalId, pos);
        if (portalId == null || world == null || world.isRemote || pos == null || JustDireThingsLegacy.INSTANCE == null) {
            return;
        }

        ChunkPos targetChunk = new ChunkPos(pos);
        TicketHandle current = ACTIVE_TICKETS.get(portalId);
        if (current != null && current.ticket.world != world) {
            releaseTicket(portalId, current);
            current = null;
        }

        if (current == null) {
            ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(JustDireThingsLegacy.INSTANCE, world, ForgeChunkManager.Type.NORMAL);
            if (ticket == null) {
                JustDireThingsLegacy.LOGGER.warn("Unable to acquire portal chunk ticket for {}", portalId);
                return;
            }
            ticket.setChunkListDepth(1);
            current = new TicketHandle(ticket, null);
            ACTIVE_TICKETS.put(portalId, current);
        }

        if (current.chunkPos != null && !current.chunkPos.equals(targetChunk)) {
            ForgeChunkManager.unforceChunk(current.ticket, current.chunkPos);
        }
        if (!targetChunk.equals(current.chunkPos)) {
            ForgeChunkManager.forceChunk(current.ticket, targetChunk);
        }
        writeTicketMetadata(current.ticket.getModData(), portalId, targetChunk);
        ACTIVE_TICKETS.put(portalId, new TicketHandle(current.ticket, targetChunk));
    }

    public static void untrack(UUID portalId, BlockPos pos) {
        if (portalId == null || pos == null) {
            return;
        }
        Set<String> tracked = TRACKED_CHUNKS.get(portalId);
        if (tracked == null) {
            return;
        }
        if (tracked.contains(chunkKey(pos))) {
            clear(portalId);
        }
    }

    public static Set<String> getTrackedChunks(UUID portalId) {
        Set<String> tracked = TRACKED_CHUNKS.get(portalId);
        return tracked == null ? Collections.emptySet() : Collections.unmodifiableSet(tracked);
    }

    public static void clear(UUID portalId) {
        if (portalId != null) {
            TRACKED_CHUNKS.remove(portalId);
            TicketHandle removed = ACTIVE_TICKETS.remove(portalId);
            if (removed != null) {
                ForgeChunkManager.releaseTicket(removed.ticket);
            }
        }
    }

    private static void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        for (ForgeChunkManager.Ticket ticket : tickets) {
            NBTTagCompound modData = ticket.getModData();
            UUID portalId = readPortalId(modData);
            if (portalId == null) {
                ForgeChunkManager.releaseTicket(ticket);
                continue;
            }

            TicketHandle existing = ACTIVE_TICKETS.putIfAbsent(portalId, new TicketHandle(ticket, readChunkPos(modData)));
            if (existing != null) {
                ForgeChunkManager.releaseTicket(ticket);
                continue;
            }

            ChunkPos trackedChunk = readChunkPos(modData);
            if (trackedChunk != null) {
                TRACKED_CHUNKS.put(portalId, Collections.singleton(trackedChunk.x + "," + trackedChunk.z));
                if (!ticket.getChunkList().contains(trackedChunk)) {
                    ForgeChunkManager.forceChunk(ticket, trackedChunk);
                }
            }
        }
    }

    private static void writeTicketMetadata(NBTTagCompound tag, UUID portalId, ChunkPos chunkPos) {
        tag.setString("PortalId", portalId.toString());
        tag.setInteger("ChunkX", chunkPos.x);
        tag.setInteger("ChunkZ", chunkPos.z);
    }

    private static UUID readPortalId(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("PortalId")) {
            return null;
        }
        try {
            return UUID.fromString(tag.getString("PortalId"));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static ChunkPos readChunkPos(NBTTagCompound tag) {
        if (tag == null || !tag.hasKey("ChunkX") || !tag.hasKey("ChunkZ")) {
            return null;
        }
        return new ChunkPos(tag.getInteger("ChunkX"), tag.getInteger("ChunkZ"));
    }

    private static void releaseTicket(UUID portalId, TicketHandle handle) {
        ACTIVE_TICKETS.remove(portalId);
        ForgeChunkManager.releaseTicket(handle.ticket);
    }

    private static final class TicketHandle {
        private final ForgeChunkManager.Ticket ticket;
        private final ChunkPos chunkPos;

        private TicketHandle(ForgeChunkManager.Ticket ticket, ChunkPos chunkPos) {
            this.ticket = ticket;
            this.chunkPos = chunkPos;
        }
    }
}
