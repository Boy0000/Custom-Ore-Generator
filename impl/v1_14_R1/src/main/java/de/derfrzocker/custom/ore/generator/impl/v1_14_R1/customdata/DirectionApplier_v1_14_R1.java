package de.derfrzocker.custom.ore.generator.impl.v1_14_R1.customdata;

import de.derfrzocker.custom.ore.generator.api.CustomData;
import de.derfrzocker.custom.ore.generator.api.CustomDataApplier;
import de.derfrzocker.custom.ore.generator.api.OreConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_14_R1.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DirectionApplier_v1_14_R1 implements CustomDataApplier {

    private final static Map<String, EnumDirection> DIRECTION_MAP = new HashMap<>(6);

    static {
        DIRECTION_MAP.put("UP", EnumDirection.UP);
        DIRECTION_MAP.put("DOWN", EnumDirection.DOWN);
        DIRECTION_MAP.put("WEST", EnumDirection.WEST);
        DIRECTION_MAP.put("SOUTH", EnumDirection.SOUTH);
        DIRECTION_MAP.put("EAST", EnumDirection.EAST);
        DIRECTION_MAP.put("NORTH", EnumDirection.NORTH);
    }

    @NonNull
    private final CustomData customData;

    @Override
    public void apply(OreConfig oreConfig, Object location, Object blockAccess) {
        final BlockPosition blockPosition = (BlockPosition) location;
        final GeneratorAccess generatorAccess = (GeneratorAccess) blockAccess;
        final IBlockData iBlockData = generatorAccess.getType(blockPosition);

        final BlockStateDirection blockStateDirection = (BlockStateDirection) iBlockData.getBlock().getStates().a("facing");

        if (blockStateDirection == null)
            return; //TODO maybe throw exception?

        final Optional<Object> objectOptional = oreConfig.getCustomData(customData);

        if (!objectOptional.isPresent())
            return; //TODO maybe throw exception?

        final String facing = (String) objectOptional.get();

        iBlockData.set(blockStateDirection, DIRECTION_MAP.get(facing.toUpperCase()));

        generatorAccess.setTypeAndData(blockPosition, iBlockData, 2);
    }

}
