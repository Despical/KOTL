/*
 * KOTL - Don't let others climb to top of the ladders!
 * Copyright (C) 2025  Berke Akçen
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

package me.despical.kotl.util;

import me.despical.commons.reflection.minecraft.MinecraftConnection;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static me.despical.commons.reflection.XReflection.*;

/**
 * @author Despical
 * <p>
 * Created at 18.04.2024
 */
public final class ActionBar {

    private static final boolean USE_SPIGOT_API = supports(12);
    private static final MethodHandle CHAT_COMPONENT_TEXT, PACKET_PLAY_OUT_CHAT;
    private static final Object CHAT_MESSAGE_TYPE;

    static {
        MethodHandle packet = null, chatComp = null;
        Object chatMsgType = null;

        if (!USE_SPIGOT_API) {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> packetPlayOutChatClass = getNMSClass("network.protocol.game", "PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = getNMSClass("network.chat", "IChatBaseComponent");
            Class<?> ChatSerializerClass = getNMSClass("network.chat", "IChatBaseComponent$ChatSerializer");

            try {
                chatComp = lookup.findStatic(ChatSerializerClass, "a", MethodType.methodType(iChatBaseComponentClass, String.class));

                Class<?> chatMessageTypeClass = Class.forName(
                    NMS_VERSION + v(17, "network.chat").orElse("") + "ChatMessageType"
                );

                MethodType type = MethodType.methodType(void.class, iChatBaseComponentClass, chatMessageTypeClass);

                for (Object obj : chatMessageTypeClass.getEnumConstants()) {
                    String name = obj.toString();
                    if (name.equals("GAME_INFO") || name.equalsIgnoreCase("ACTION_BAR")) {
                        chatMsgType = obj;
                        break;
                    }
                }

                packet = lookup.findConstructor(packetPlayOutChatClass, type);
            } catch (Exception ignored) {
                try {
                    chatMsgType = (byte) 2;

                    packet = lookup.findConstructor(packetPlayOutChatClass, MethodType.methodType(void.class, iChatBaseComponentClass, byte.class));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        CHAT_MESSAGE_TYPE = chatMsgType;
        CHAT_COMPONENT_TEXT = chatComp;
        PACKET_PLAY_OUT_CHAT = packet;
    }

    private ActionBar() {
    }

    public static void sendActionBar(@Nonnull Player player, @Nullable String message) {
        Objects.requireNonNull(player, "Cannot send action bar to null player");
        Objects.requireNonNull(message, "Cannot send null actionbar message");

        if (USE_SPIGOT_API) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            return;
        }

        try {
            Object component = CHAT_COMPONENT_TEXT.invoke("{\"text\":\"" + message.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}");
            Object packet = PACKET_PLAY_OUT_CHAT.invoke(component, CHAT_MESSAGE_TYPE);
            MinecraftConnection.sendPacket(player, packet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
