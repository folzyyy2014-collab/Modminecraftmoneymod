package com.example.moneymod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.CommandDispatcher;
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
        return d != null && d.contains("moneymod_" + id);
    }
}

/** Таблица цен скупщика. Меняй цифры как хочешь. */
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

    /** Продаёт предмет в основной руке. Возвращает полученную сумму (0 = не покупают). */
    public static long sellHand(ServerPlayer p) {
        ItemStack s = p.getMainHandItem();
        int unit = unitPrice(s);
        if (unit <= 0) return 0;
        long total = (long) unit * s.getCount();
        s.setCount(0);
        EconomyManager.add(p.getUUID(), total);
        return total;
    }

    /** Продаёт всё продаваемое из инвентаря. */
    public static long sellAll(ServerPlayer p) {
        Inventory inv = p.getInventory();
        long total = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            int unit = unitPrice(s);
            if (unit > 0) {
                total += (long) unit * s.getCount();
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
        if (total > 0) EconomyManager.add(p.getUUID(), total);
        return total;
    }
}

/** Серверное хранилище денег и разблокировок. Всё считается только на сервере. */
final class EconomyManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, PlayerData> data = new HashMap<>();
    private static Path file;

    public static final class PlayerData {
        public long balance = 0;
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
}

final class GameEvents {
    private static final ResourceLocation NO_JUMP =
        ResourceLocation.fromNamespaceAndPath(MoneyMod.MODID, "no_jump");
    private static final int MAX_TREE_BLOCKS = 128;

    private GameEvents() {}

    /** true = запретить действие (нет разблокировки). */
    private static boolean denied(Player player, Unlock u) {
        if (!(player instanceof ServerPlayer sp)) return false;
        if (EconomyManager.has(sp.getUUID(), u)) return false;
        sp.displayClientMessage(
            Component.literal("Заблокировано: «" + u.title + "». Купи: /buy " + u.id), true);
        return true;
    }

    public static boolean onBreak(BlockEvent.BreakEvent e) {
        Player p = e.getPlayer();
        if (denied(p, Unlock.BREAK)) return true;

        if (p instanceof ServerPlayer sp && e.getLevel() instanceof ServerLevel level) {
            ItemStack held = sp.getMainHandItem();
            BlockPos origin = e.getPos();
            BlockState state = e.getState();
            if (Machine.SAW.matches(held) && state.is(BlockTags.LOGS)) {
                fellTree(level, sp, origin, held);
            } else if (Machine.DRILL.matches(held)) {
                drill(level, sp, origin, held);
            }
        }
        return false;
    }

    /** Пила: валит всё дерево целиком (соседние брёвна). */
    private static void fellTree(ServerLevel level, ServerPlayer sp, BlockPos origin, ItemStack tool) {
        Set<BlockPos> seen = new HashSet<>();
        List<BlockPos> found = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        seen.add(origin);
        queue.add(origin);
        while (!queue.isEmpty() && found.size() < MAX_TREE_BLOCKS) {
            BlockPos cur = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = cur.offset(dx, dy, dz);
                        if (!seen.add(n)) continue;
                        if (level.getBlockState(n).is(BlockTags.LOGS)) {
                            found.add(n);
                            queue.add(n);
                        }
                    }
                }
            }
        }
        for (BlockPos pos : found) level.destroyBlock(pos, true, sp);
        tool.hurtAndBreak(1, sp, EquipmentSlot.MAINHAND);
    }

    /** Бур: ломает блоки 3x3x3 вокруг (только то, что берёт кирка, без бедрока). */
    private static void drill(ServerLevel level, ServerPlayer sp, BlockPos origin, ItemStack tool) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockState st = level.getBlockState(pos);
                    float hardness = st.getDestroySpeed(level, pos);
                    if (st.is(BlockTags.MINEABLE_WITH_PICKAXE) && hardness >= 0 && hardness <= 5) {
                        level.destroyBlock(pos, true, sp);
                    }
                }
            }
        }
        tool.hurtAndBreak(1, sp, EquipmentSlot.MAINHAND);
    }

    public static boolean onPlace(BlockEvent.EntityPlaceEvent e) {
        Entity ent = e.getEntity();
        return ent instanceof Player p && denied(p, Unlock.PLACE);
    }

    public static boolean onAttack(AttackEntityEvent e) {
        return denied(e.getEntity(), Unlock.ATTACK);
    }

    /**
     * ПКМ по золотому блоку пустой рукой = кликер (+1 монета).
     * ПКМ по изумрудному блоку с ресурсом в руке = скупщик продаёт то, что в руке.
     */
    public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return false;
        if (e.getHand() != InteractionHand.MAIN_HAND) return false;
        BlockState state = e.getLevel().getBlockState(e.getPos());

        if (state.is(Blocks.GOLD_BLOCK) && sp.getMainHandItem().isEmpty()) {
            EconomyManager.add(sp.getUUID(), 1);
            sp.displayClientMessage(
                Component.literal("+1 монета (баланс: " + EconomyManager.balance(sp.getUUID()) + ")"), true);
            return false;
        }

        if (state.is(Blocks.EMERALD_BLOCK) && !sp.getMainHandItem().isEmpty()) {
            long got = Prices.sellHand(sp);
            sp.displayClientMessage(Component.literal(
                got > 0 ? "Скупщик заплатил " + got + " монет" : "Скупщик это не берёт"), true);
            return true; // не ставить блок рядом
        }
        return false;
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post e) {
        if (e.player() instanceof ServerPlayer sp && sp.tickCount % 20 == 0) {
            refreshJump(sp);
        }
    }

    /** Прыжок блокируем обнулением силы прыжка, пока не куплена разблокировка. */
    public static void refreshJump(ServerPlayer sp) {
        AttributeInstance attr = sp.getAttribute(Attributes.JUMP_STRENGTH);
        if (attr == null) return;
        boolean locked = !EconomyManager.has(sp.getUUID(), Unlock.JUMP);
        boolean has = attr.hasModifier(NO_JUMP);
        if (locked && !has) {
            attr.addTransientModifier(new AttributeModifier(
                NO_JUMP, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (!locked && has) {
            attr.removeModifier(NO_JUMP);
        }
    }
}

final class MoneyCommands {
    private MoneyCommands() {}

    private static void ok(CommandSourceStack src, String text) {
        src.sendSuccess(() -> Component.literal(text), false);
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("balance").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            ok(ctx.getSource(), "Баланс: " + EconomyManager.balance(p.getUUID()) + " монет");
            return 1;
        }));

        d.register(Commands.literal("shop").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            StringBuilder sb = new StringBuilder("Магазин (/buy <id>):\nУмения:");
            for (Unlock u : Unlock.values()) {
                String mark = EconomyManager.has(p.getUUID(), u) ? " [куплено]" : "";
                sb.append("\n- ").append(u.id).append(": ").append(u.title)
                  .append(" — ").append(u.price).append(mark);
            }
            sb.append("\nТехника:");
            for (Machine m : Machine.values()) {
                sb.append("\n- ").append(m.id).append(": ").append(m.title)
                  .append(" — ").append(m.price);
            }
            ok(ctx.getSource(), sb.toString());
            return 1;
        }));

        d.register(Commands.literal("buy")
            .then(Commands.argument("id", StringArgumentType.word()).executes(ctx -> {
                CommandSourceStack src = ctx.getSource();
                ServerPlayer p = src.getPlayerOrException();
                String id = StringArgumentType.getString(ctx, "id");

                Unlock u = Unlock.byId(id);
                if (u != null) {
                    if (EconomyManager.has(p.getUUID(), u)) {
                        src.sendFailure(Component.literal("Уже куплено"));
                        return 0;
                    }
                    if (!EconomyManager.buy(p.getUUID(), u)) {
                        src.sendFailure(Component.literal("Не хватает денег: нужно " + u.price));
                        return 0;
                    }
                    GameEvents.refreshJump(p);
                    ok(src, "Куплено: " + u.title);
                    return 1;
                }

                Machine m = Machine.byId(id);
                if (m != null) {
                    if (EconomyManager.balance(p.getUUID()) < m.price) {
                        src.sendFailure(Component.literal("Не хватает денег: нужно " + m.price));
                        return 0;
                    }
                    EconomyManager.add(p.getUUID(), -m.price);
                    ItemStack stack = m.create();
                    if (!p.getInventory().add(stack)) p.drop(stack, false);
                    ok(src, "Куплено: " + m.title);
                    return 1;
                }

                src.sendFailure(Component.literal("Нет такого товара. Смотри /shop"));
                return 0;
            })));

        d.register(Commands.literal("sell").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            long got = Prices.sellHand(p);
            if (got <= 0) {
                ctx.getSource().sendFailure(Component.literal("Это не покупают (держи ресурс в руке)"));
                return 0;
            }
            ok(ctx.getSource(), "Продано за " + got + " монет");
            return 1;
        }));

        d.register(Commands.literal("sellall").executes(ctx -> {
            ServerPlayer p = ctx.getSource().getPlayerOrException();
            long got = Prices.sellAll(p);
            if (got <= 0) {
                ctx.getSource().sendFailure(Component.literal("Нечего продавать"));
                return 0;
            }
            ok(ctx.getSource(), "Продано всё за " + got + " монет");
            return 1;
        }));
    }
}

@Mod(MoneyMod.MODID)
public class MoneyMod {
    public static final String MODID = "moneymod";
    private static int saveTimer = 0;

    public MoneyMod() {
        ServerStartedEvent.BUS.addListener(e -> EconomyManager.load(e.getServer()));
        ServerStoppingEvent.BUS.addListener(e -> EconomyManager.save());
        RegisterCommandsEvent.BUS.addListener(e -> MoneyCommands.register(e.getDispatcher()));

        BlockEvent.BreakEvent.BUS.addListener(GameEvents::onBreak);
        BlockEvent.EntityPlaceEvent.BUS.addListener(GameEvents::onPlace);
        AttackEntityEvent.BUS.addListener(GameEvents::onAttack);
        PlayerInteractEvent.RightClickBlock.BUS.addListener(GameEvents::onRightClickBlock);
        TickEvent.PlayerTickEvent.Post.BUS.addListener(GameEvents::onPlayerTick);

        // автосохранение раз в 5 минут
        TickEvent.ServerTickEvent.Post.BUS.addListener(e -> {
            if (++saveTimer >= 6000) {
                saveTimer = 0;
                EconomyManager.save();
            }
        });
    }
  }
          
