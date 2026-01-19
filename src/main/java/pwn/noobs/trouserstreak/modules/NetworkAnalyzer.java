package pwn.noobs.trouserstreak.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.text.Text;
import pwn.noobs.trouserstreak.Trouser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkAnalyzer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> trackPlayers = sgGeneral.add(new BoolSetting.Builder()
            .name("Track Players")
            .description("Track all player positions and movements")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> logPackets = sgGeneral.add(new BoolSetting.Builder()
            .name("Log Packets")
            .description("Log all network packets")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> updateInterval = sgGeneral.add(new IntSetting.Builder()
            .name("Update Interval")
            .description("Ticks between network analysis updates")
            .defaultValue(20)
            .min(1)
            .sliderRange(1, 100)
            .build()
    );

    private final Map<String, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private int tickCounter = 0;

    public NetworkAnalyzer() {
        super(Trouser.Main, "Network Analyzer", "Analyzes network traffic and player movements");
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!trackPlayers.get()) return;

        tickCounter++;
        if (tickCounter >= updateInterval.get()) {
            tickCounter = 0;
            if (logPackets.get() && !playerDataMap.isEmpty()) {
                ChatUtils.sendMsg(Text.of("Players tracked: " + playerDataMap.size()));
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (!trackPlayers.get()) return;

        if (event.packet instanceof EntityPositionS2CPacket packet) {
            String playerId = packet.getId() + "";
            playerDataMap.computeIfAbsent(playerId, k -> new PlayerData())
                    .updatePosition(packet.getX(), packet.getY(), packet.getZ());
        }
    }

    private static class PlayerData {
        double x, y, z;
        long lastUpdate;

        void updatePosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.lastUpdate = System.currentTimeMillis();
        }
    }
}