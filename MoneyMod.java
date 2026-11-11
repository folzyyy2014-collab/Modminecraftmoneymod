package com.example.moneymod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;

enum Unlock {
    JUMP("jump", "Прыгать", 10),
    ATTACK("attack", "Бить", 20),
    PLACE("place", "Ставить блоки", 30),
    BREAK("break", "Ломать блоки", 50);

    public final String id;
    public final String title;
    public final long price;

    Unlock(String id, String title, long price) {
        this.id = id;
        this.title = title;
        this.price = price;
    }

    public static Unlock byId(String id) {
        for (Unlock u : values()) {
            if (u.id.equalsIgnoreCase(id)) return u;
        }
        return null;
    }
}

/** Техника: обычный инструмент с меткой. Способность включается в GameEvents. */
enum Machine {
    SAW("saw", "Пила для дерева", 150, Items.IRON_AXE),
    DRILL("drill", "Бур для камня", 300, Items.IRON_PICKAXE);

    public final String id;
    public final String title;
    public final long price;
    private final Item base;

    Machine(String id, String title, long price, Item base) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.base = base;
    }

    public static Machine byId(String id) {
        for (Machine m : values()) {
            if (m.id.equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    public ItemStack create() {
        ItemStack s = new ItemStack(base);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("moneymod_" + id, true);
        s.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        s.set(DataComponents.CUSTOM_NAME, Component.literal(title));
        return s;
    }

    public boolean matches(ItemStack s) {
        if (s.isEmpty()) return false;
        CustomData d = s.get(DataComponents.CUSTOM_DATA);
        return d != null && d.copyTag().contains("moneymod_" + id);
    }
}

/** Таблица цен скупщика. С каждой продажи берётся налог в казну. */
final class Prices {
    private static final Map<Item, Integer> P = new HashMap<>();

    static {
        P.put(Items.COBBLESTONE, 1);
        P.put(Items.COBBLED_DEEPSLATE, 1);
        P.put(Items.SAND, 1);
        P.put(Items.WHEAT, 1);
        P.put(Items.COAL, 3);
        P.put(Items.RAW_COPPER, 3);
        P.put(Items.LAPIS_LAZULI, 3);
        P.put(Items.REDSTONE, 2);
        P.put(Items.RAW_IRON, 5);
        P.put(Items.IRON_INGOT, 8);
        P.put(Items.RAW_GOLD, 8);
        P.put(Items.GOLD_INGOT, 12);
        P.put(Items.EMERALD, 25);
        P.put(Items.DIAMOND, 40);
    }

    private Prices() {}

    public static int unitPrice(ItemStack s) {
        if (s.isEmpty()) return 0;
        if (s.is(ItemTags.LOGS)) return 2;
        return P.getOrDefault(s.getItem(), 0);
    }

    /** Выплата игроку после налога. Налог уходит в казну. */
    private static long payout(ServerPlayer p, long gross) {
        long tax = gross * EconomyManager.taxPercent / 100;
        long net = gross - tax;
        EconomyManager.add(p.getUUID(), net);
        EconomyManager.addTreasury(tax);
        return net;
    }

    /** Продаёт предмет в основной руке. Возвращает полученную сумму (0 = не покупают). */
    public static long sellHand(ServerPlayer p) {
        ItemStack s = p.getMainHandItem();
        int unit = unitPrice(s);
        if (unit <= 0) return 0;
        long gross = (long) unit * s.getCount();
        s.setCount(0);
        return payout(p, gross);
    }

    /** Продаёт всё продаваемое из инвентаря. */
    public static long sellAll(ServerPlayer p) {
        Inventory inv = p.getInventory();
        long gross = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            int unit = unitPrice(s);
            if (unit > 0) {
                gross += (long) unit * s.getCount();
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (gross <= 0) return 0;
        return payout(p, gross);
    }
}

/** Серверное хранилище денег, банка, казны и разблокировок. Всё считается только на сервере. */
final class EconomyManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, PlayerData> data = new HashMap<>();
    private static Path file;

    /** Казна государства хранится как "игрок" с нулевым UUID. */
    public static final UUID TREASURY = new UUID(0L, 0L);
    /** Налог с продаж ресурсов, в процентах (потом его будет менять президент). */
    public static int taxPercent = 10;
    /** Процент банка раз в 10 минут. */
    public static final int INTEREST_PERCENT = 2;

    public static final class PlayerData {
        public long balance = 0;
        public long bank = 0;
        public Set<String> unlocked = new HashSet<>();
    }

    private EconomyManager() {}

    public static void load(MinecraftServer server) {
        file = server.getWorldPath(LevelResource.ROOT).resolve("moneymod.json");
        data = new HashMap<>();
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Map<String, PlayerData> loaded = GSON.fromJson(r, new TypeToken<Map<String, PlayerData>>() {}.getType());
                if (loaded != null) data = loaded;
            } catch (IOException | RuntimeException e) {
                System.err.println("[moneymod] Не удалось загрузить данные: " + e);
            }
        }
    }

    public static void save() {
        if (file == null) return;
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(data, w);
        } catch (IOException e) {
            System.err.println("[moneymod] Не удалось сохранить данные: " + e);
        }
    }

    public static PlayerData get(UUID id) {
        return data.computeIfAbsent(id.toString(), k -> new PlayerData());
    }

    public static long balance(UUID id) {
        return get(id).balance;
    }

    public static long bank(UUID id) {
        return get(id).bank;
    }

    public static void add(UUID id, long amount) {
        get(id).balance += amount;
    }

    public static boolean has(UUID id, Unlock u) {
        return get(id).unlocked.contains(u.id);
    }

    /** Покупка разблокировки. Возвращает true, если куплено. */
    public static boolean buy(UUID id, Unlock u) {
        PlayerData d = get(id);
        if (d.unlocked.contains(u.id) || d.balance < u.price) return false;
        d.balance -= u.price;
        d.unlocked.add(u.id);
        return true;
    }

    /** Наличные -> банк. Возвращает, сколько реально положено. */
    public static long deposit(UUID id, long amount) {
        PlayerData d = get(id);
        long moved = Math.min(amount, d.balance);
        if (moved <= 0) return 0;
        d.balance -= moved;
        d.bank += moved;
        return moved;
    }

    /** Банк -> наличные. Возвращает, сколько реально снято. */
    public static long withdraw(UUID id, long amount) {
        PlayerData d = get(id);
        long moved = Math.min(amount, d.bank);
        if (moved <= 0) return 0;
        d.bank -= moved;
        d.balance += moved;
        return moved;
    }

    /** Начисляет проценты на счета в банке (раз в 10 минут). */
    public static void applyInterest() {
        String treasuryKey = TREASURY.toString();
        for (Map.Entry<String, PlayerData> e : data.entrySet()) {
            if (e.getKey().equals(treasuryKey)) continue;
            PlayerData d = e.getValue();
            if (d.bank > 0) d.bank += Math.max(1, d.bank * INTEREST_PERCENT / 100);
        }
    }

    public static long treasury() {
        return get(TREASURY).balance;
    }

    public static void addTreasury(long amount) {
        get(TREASURY).balance += amount;
    }
    }
    
