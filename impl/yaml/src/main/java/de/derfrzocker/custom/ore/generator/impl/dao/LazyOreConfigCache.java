/*
 * MIT License
 *
 * Copyright (c) 2019 Marvin (DerFrZocker)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package de.derfrzocker.custom.ore.generator.impl.dao;

import de.derfrzocker.custom.ore.generator.api.OreConfig;
import de.derfrzocker.custom.ore.generator.impl.OreConfigYamlImpl;
import de.derfrzocker.spigot.utils.Config;
import de.derfrzocker.spigot.utils.ReloadAble;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class LazyOreConfigCache implements ReloadAble {

    private final Object look = new Object();
    @NotNull
    private final File file;

    @Nullable
    private OreConfig oreConfig;

    public LazyOreConfigCache(@NotNull final File file) {
        Validate.notNull(file, "File can not be null");
        Validate.isTrue(file.getName().endsWith(".yml"), "File " + file + " has not valid extension, must be '.yml'");

        if (file.exists())
            Validate.isTrue(file.isFile(), "File " + file + " is not a File?");

        this.file = file;
    }

    /**
     * When this LazyOreConfigCache has a OreConfig loaded, it will save it to disk.
     * Nothing will happen, if this LazyOreConfigCache does not holds a OreConfig
     */
    public void save() {
        if (this.oreConfig == null)
            return;

        final OreConfig oreConfig;

        if (!(this.oreConfig instanceof ConfigurationSerializable)) {
            oreConfig = new OreConfigYamlImpl(this.oreConfig.getName(), this.oreConfig.getMaterial(), this.oreConfig.getOreGenerator(), this.oreConfig.getBlockSelector());
            OreConfigYamlImpl.copyData(this.oreConfig, (OreConfigYamlImpl) oreConfig);
        } else
            oreConfig = this.oreConfig;


        final Config config = new Config(file);

        config.set("value", oreConfig);

        try {
            config.options().header("Only edit this file if you 10000% sure you know what you are doing. \nYou can set everything via the plugin commands, no need to edit this file manully.").copyHeader(true);
            config.save(file);
        } catch (final IOException e) {
            throw new RuntimeException("Unexpected error while saving OreConfig " + oreConfig.getName() + " to disk!", e);
        }
    }

    /**
     * When this LazyOreConfigCache has a OreConfig loaded, it will return it.
     * If not, then it will try to load if from a file and set it to the cache.
     *
     * @return the cached or fresh loaded OreConfig
     * @throws RuntimeException if no OreConfig is Cached and the file does not exists
     * @throws RuntimeException if no OreConfig is Cached and the file does not contains a OreConfig under the key "value"
     * @throws RuntimeException if no OreConfig is Cached and the file name and OreConfig name does not match
     */
    @NotNull
    public OreConfig getOreConfig() {
        if (oreConfig != null)
            return oreConfig;

        synchronized (look) {
            if (oreConfig != null)
                return oreConfig;

            if (!file.exists())
                throw new RuntimeException("File " + file + " does not exists, can not load OreConfig from none existing file");

            final Config config = new Config(file);

            final Object object = config.get("value");

            if (!(object instanceof OreConfig))
                throw new RuntimeException("File " + file + " does not have a OreConfig under the key 'value'");

            final OreConfig oreConfig = (OreConfig) object;

            if (!oreConfig.getName().equals(file.getName().substring(0, file.getName().length() - 4)))
                throw new RuntimeException("File name " + file.getName() + " and OreConfig name " + oreConfig.getName() + " does not match");

            this.oreConfig = oreConfig;
        }

        return oreConfig;
    }

    /**
     * @param oreConfig to set
     * @throws IllegalArgumentException if oreConfig is null
     * @throws RuntimeException         if the file name and the oreConfig name does't match
     */
    public void setOreConfig(@NotNull final OreConfig oreConfig) {
        Validate.notNull(oreConfig, "OreConfig can not be null");

        if (!oreConfig.getName().equals(file.getName().substring(0, file.getName().length() - 4)))
            throw new RuntimeException("File name " + file.getName() + " and OreConfig name " + oreConfig.getName() + " does not match");

        this.oreConfig = oreConfig;
    }

    @Override
    public void reload() {
        oreConfig = null;
    }

}
