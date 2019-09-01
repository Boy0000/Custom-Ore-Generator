package de.derfrzocker.custom.ore.generator.impl.v1_13_R1;

import de.derfrzocker.custom.ore.generator.api.CustomOreGeneratorService;
import de.derfrzocker.custom.ore.generator.api.OreConfig;
import de.derfrzocker.custom.ore.generator.api.OreGenerator;
import de.derfrzocker.custom.ore.generator.api.WorldConfig;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.v1_13_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nullable;
import java.util.*;

@RequiredArgsConstructor
public class ChunkOverrieder<C extends GeneratorSettings> implements ChunkGenerator<C> {

    final ChunkGenerator<C> parent;


    @Override
    public void createChunk(IChunkAccess iChunkAccess) {
        parent.createChunk(iChunkAccess);
    }

    @Override
    public void addFeatures(RegionLimitedWorldAccess regionLimitedWorldAccess, WorldGenStage.Features features) {
        parent.addFeatures(regionLimitedWorldAccess, features);
    }

    @Override
    public void addDecorations(RegionLimitedWorldAccess regionLimitedWorldAccess) {
        parent.addDecorations(regionLimitedWorldAccess);

        Set<Biome> biomes = getBiomes(regionLimitedWorldAccess);

        CustomOreGeneratorService service = Bukkit.getServicesManager().load(CustomOreGeneratorService.class);

        WorldConfig worldConfig;

        {
            Optional<WorldConfig> optional = service.getWorldConfig(parent.getWorld().getWorld().getName());

            if (!optional.isPresent())
                return;

            worldConfig = optional.get();
        }

        biomes.forEach(biome -> {
            List<OreConfig> oreConfigs = Arrays.asList(worldConfig.getOreConfigs().stream().filter(oreConfig -> oreConfig.getBiomes().contains(biome)).filter(OreConfig::isActivated).toArray(OreConfig[]::new));

            oreConfigs.forEach(oreConfig -> generate(oreConfig, regionLimitedWorldAccess, biome));
        });
    }

    @Override
    public void addMobs(RegionLimitedWorldAccess regionLimitedWorldAccess) {
        parent.addMobs(regionLimitedWorldAccess);
    }

    @Override
    public List<BiomeBase.BiomeMeta> getMobsFor(EnumCreatureType enumCreatureType, BlockPosition blockPosition) {
        return parent.getMobsFor(enumCreatureType, blockPosition);
    }

    @Nullable
    @Override
    public BlockPosition findNearestMapFeature(World world, String s, BlockPosition blockPosition, int i) {
        return parent.findNearestMapFeature(world, s, blockPosition, i);
    }

    @Override
    public C getSettings() {
        return parent.getSettings();
    }

    @Override
    public int a(World world, boolean b, boolean b1) {
        return parent.a(world, b, b1);
    }

    @Override
    public boolean canSpawnStructure(BiomeBase biomeBase, StructureGenerator<? extends WorldGenFeatureConfiguration> structureGenerator) {
        return parent.canSpawnStructure(biomeBase, structureGenerator);
    }

    @Nullable
    @Override
    public WorldGenFeatureConfiguration getFeatureConfiguration(BiomeBase biomeBase, StructureGenerator<? extends WorldGenFeatureConfiguration> structureGenerator) {
        return parent.getFeatureConfiguration(biomeBase, structureGenerator);
    }

    @Override
    public Long2ObjectMap<StructureStart> getStructureStartCache(StructureGenerator<? extends WorldGenFeatureConfiguration> structureGenerator) {
        return parent.getStructureStartCache(structureGenerator);
    }

    @Override
    public Long2ObjectMap<LongSet> getStructureCache(StructureGenerator<? extends WorldGenFeatureConfiguration> structureGenerator) {
        return parent.getStructureCache(structureGenerator);
    }

    @Override
    public WorldChunkManager getWorldChunkManager() {
        return parent.getWorldChunkManager();
    }

    @Override
    public long getSeed() {
        return parent.getSeed();
    }

    @Override
    public int getSpawnHeight() {
        return parent.getSpawnHeight();
    }

    @Override
    public int e() {
        return parent.e();
    }

    @Override
    public World getWorld() {
        return parent.getWorld();
    }

    private Set<Biome> getBiomes(RegionLimitedWorldAccess access) {
        Set<Biome> set = new HashSet<>();

        int x = access.a() << 4;
        int z = access.b() << 4;

        for (int x2 = x; x2 < x + 16; x2++)
            for (int z2 = z; z2 < z + 16; z2++) {
                BiomeBase base = access.getBiome(new BlockPosition(x2, 60, z2));
                try {
                    set.add(Biome.valueOf(BiomeBase.REGISTRY_ID.b(base).getKey().toUpperCase()));
                } catch (Exception ignored) {
                }
            }

        return set;
    }

    private void generate(OreConfig oreConfig, RegionLimitedWorldAccess access, Biome biome) {
        CustomOreGeneratorService service = Bukkit.getServicesManager().load(CustomOreGeneratorService.class);

        Optional<OreGenerator> optional = service.getOreGenerator(oreConfig.getOreGenerator());

        if (!optional.isPresent())
            return;

        OreGenerator oreGenerator = optional.get();

        if (oreGenerator instanceof MinableGenerator_v1_13_R1) {
            ((MinableGenerator_v1_13_R1) oreGenerator).generate(oreConfig, parent.getWorld().getWorld(), access, service.createRandom(parent.getSeed() + oreConfig.getMaterial().toString().hashCode(), access.a(), access.b()), biome);
            return;
        }

        oreGenerator.generate(oreConfig, parent.getWorld().getWorld(), access.a(), access.b(), service.createRandom(parent.getSeed() + oreConfig.getMaterial().toString().hashCode(), access.a(), access.b()), biome);
    }
}
