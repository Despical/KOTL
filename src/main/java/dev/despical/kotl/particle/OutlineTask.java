/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2026  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.despical.kotl.particle;

import dev.despical.kotl.KOTL;
import dev.despical.kotl.arena.Arena;
import dev.despical.kotl.arena.options.ArenaKeys;
import dev.despical.particle.ParticleBuilder;
import dev.despical.particle.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Despical
 * <p>
 * Created at 06.06.2026
 */
public class OutlineTask extends BukkitRunnable {

    private final Arena arena;
    private final Location location;
    private final double step;
    private final ParticleBuilder particleBuilder;

    public OutlineTask(KOTL plugin, Arena arena) {
        Location min = arena.getOption(ArenaKeys.MIN_CORNER);

        this.arena = arena;
        this.location = new Location(min.getWorld(), 0, 0, 0);

        ParticleEffect particle = ParticleEffect.valueOf(plugin.getConfig().getString("arena-outlines.particle", "flame").toUpperCase());

        this.step = plugin.getConfig().getDouble("arena-outlines.step", .4);
        this.particleBuilder = new ParticleBuilder(particle);
    }

    private Location setLocation(Location location, double x, double y, double z) {
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        return location;
    }

    @Override
    public void run() {
        Location min = arena.getOption(ArenaKeys.MIN_CORNER);
        Location max = arena.getOption(ArenaKeys.MAX_CORNER);

        if (min == null || max == null || min.getWorld() == null || !min.getWorld().equals(max.getWorld())) {
            return;
        }

        location.setWorld(min.getWorld());

        double[] xArr = {Math.min(min.getX(), max.getX()), Math.max(min.getX(), max.getX())};
        double[] yArr = {Math.min(min.getY(), max.getY()), Math.max(min.getY(), max.getY())};
        double[] zArr = {Math.min(min.getZ(), max.getZ()), Math.max(min.getZ(), max.getZ())};

        for (double x = xArr[0]; x < xArr[1]; x += step) {
            for (double y : yArr) {
                for (double z : zArr) {
                    particleBuilder.setLocation(setLocation(location, x, y, z)).display();
                }
            }
        }

        for (double y = yArr[0]; y < yArr[1]; y += step) {
            for (double x : xArr) {
                for (double z : zArr) {
                    particleBuilder.setLocation(setLocation(location, x, y, z)).display();
                }
            }
        }

        for (double z = zArr[0]; z < zArr[1]; z += step) {
            for (double y : yArr) {
                for (double x : xArr) {
                    particleBuilder.setLocation(setLocation(location, x, y, z)).display();
                }
            }
        }
    }
}
