package de.highcraft.registry;

import de.highcraft.HighCraft;
import de.highcraft.drug.PlayerDrugData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, HighCraft.MODID);

    /** Addiction/tolerance/intoxication per player - kept on death (intoxication is reset on respawn). */
    public static final Supplier<AttachmentType<PlayerDrugData>> DRUG_DATA = ATTACHMENTS.register("drug_data",
            () -> AttachmentType.serializable(PlayerDrugData::new).copyOnDeath().build());

    private ModAttachments() {}
}
