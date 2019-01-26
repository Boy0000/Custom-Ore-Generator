package de.derfrzocker.custom.generator.ore;

import de.derfrzocker.custom.generator.ore.api.CustomOreGeneratorService;
import de.derfrzocker.custom.generator.ore.api.Version;
import de.derfrzocker.custom.generator.ore.command.*;
import de.derfrzocker.custom.generator.ore.impl.BiomeConfigYamlImpl;
import de.derfrzocker.custom.generator.ore.impl.CustomOreGeneratorServiceImpl;
import de.derfrzocker.custom.generator.ore.impl.OreConfigYamlImpl;
import de.derfrzocker.custom.generator.ore.impl.WorldConfigYamlImpl;
import de.derfrzocker.custom.generator.ore.impl.dao.WorldConfigYamlDao;
import de.derfrzocker.custom.generator.ore.util.CommandSeparator;
import de.derfrzocker.custom.generator.ore.util.Config;
import de.derfrzocker.custom.generator.ore.util.ReloadAble;
import de.derfrzocker.custom.generator.ore.v1_13_R1.MinableGenerator_v1_13_R1;
import de.derfrzocker.custom.generator.ore.v1_13_R1.WorldHandler_v1_13_R1;
import de.derfrzocker.custom.generator.ore.v1_13_R2.MinableGenerator_v1_13_R2;
import de.derfrzocker.custom.generator.ore.v1_13_R2.WorldHandler_v1_13_R2;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomOreGenerator extends JavaPlugin implements Listener {

    @Getter
    private static CustomOreGenerator instance;

    private final CommandSeparator commandSeparator = new OreGenCommand();

    @Getter
    private List<ReloadAble> reloadAbles = new ArrayList<>();

    static {
        Version.v1_13_R2.add(() -> new MinableGenerator_v1_13_R2());
        Version.v1_13_R2.add(WorldHandler_v1_13_R2::new);

        Version.v1_13_R1.add(() -> new MinableGenerator_v1_13_R1());
        Version.v1_13_R1.add(WorldHandler_v1_13_R1::new);

        Version.v1_12_R1.add(() -> new MinableGenerator_v1_12_R1());
        Version.v1_12_R1.add(CustomOreBlockPopulator::new);

       // Version.v1_11_R1.add(() -> new MinableGenerator_v1_11_R1()); TODO Test Minecraft 1.11
        //Version.v1_11_R1.add(CustomOreBlockPopulator::new);
    }

    @Override
    public void onLoad() {
        instance = this;

        ConfigurationSerialization.registerClass(BiomeConfigYamlImpl.class);
        ConfigurationSerialization.registerClass(OreConfigYamlImpl.class);
        ConfigurationSerialization.registerClass(WorldConfigYamlImpl.class);

        Bukkit.getServicesManager().register(CustomOreGeneratorService.class, new CustomOreGeneratorServiceImpl(new WorldConfigYamlDao(new File(getDataFolder(), "data/world_configs.yml"))), this, ServicePriority.Normal);
    }

    @Override
    public void onEnable() {
        CustomOreGeneratorMessages.getInstance().setFile(Config.getConfig(this, "messages.yml"));

        Version.getCurrent().run();
        Version.clear();

        getCommand("oregen").setExecutor(commandSeparator);
        commandSeparator.registerExecuter(new SetCommand(), "set");
        commandSeparator.registerExecuter(new SetBiomeCommand(), "setbiome");
        commandSeparator.registerExecuter(new ReloadCommand(), "reload");
        HelpCommand helpCommand = new HelpCommand();
        commandSeparator.registerExecuter(helpCommand, "");
        commandSeparator.registerExecuter(helpCommand, null);
        commandSeparator.registerExecuter(helpCommand, "help");
    }


    public static CustomOreGeneratorService getService() {
        CustomOreGeneratorService service = Bukkit.getServicesManager().load(CustomOreGeneratorService.class);

        if (service == null)
            throw new IllegalStateException("The Bukkit Service have no " + CustomOreGeneratorService.class.getName() + " registered", new NullPointerException("service can't be null"));

        return service;
    }

}
