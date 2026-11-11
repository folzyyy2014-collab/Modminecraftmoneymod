package http://com.example.moneymod;

import http://com.google.gson.Gson;
import http://com.google.gson.GsonBuilder;
import http://com.google.gson.reflect.TypeToken;
import http://com.mojang.brigadier.CommandDispatcher;
import http://com.mojang.brigadier.arguments.StringArgumentType;
import http://io.netty.buffer.ByteBuf;
import http://java.io.IOException;
import http://java.io.Reader;
import http://java.io.Writer;
import http://java.nio.file.Files;
import http://java.nio.file.Path;
import http://java.util.ArrayDeque;
import http://java.util.ArrayList;
import http://java.util.HashMap;
import http://java.util.HashSet;
import http://java.util.List;
import http://java.util.Map;
import http://java.util.Set;
import http://java.util.UUID;
import http://net.minecraft.client.Minecraft;
import http://net.minecraft.client.gui.GuiGraphics;
import http://net.minecraft.client.gui.components.Button;
import http://net.minecraft.client.gui.screens.Screen;
import http://net.minecraft.commands.CommandSourceStack;
import http://net.minecraft.commands.Commands;
import http://net.minecraft.core.BlockPos;
import http://net.minecraft.core.component.DataComponents;
import http://net.minecraft.nbt.CompoundTag;
import http://net.minecraft.network.chat.Component;
import http://net.minecraft.resources.ResourceLocation;
import http://net.minecraft.server.MinecraftServer;
import http://net.minecraft.server.level.ServerLevel;
import http://net.minecraft.server.level.ServerPlayer;
import http://net.minecraft.tags.BlockTags;
import http://net.minecraft.tags.ItemTags;
import http://net.minecraft.world.InteractionHand;
import http://net.minecraft.world.entity.Entity;
import http://net.minecraft.world.entity.EquipmentSlot;
import http://net.minecraft.world.entity.ai.attributes.AttributeInstance;
import http://net.minecraft.world.entity.ai.attributes.AttributeModifier;
import http://net.minecraft.world.entity.ai.attributes.Attributes;
import http://net.minecraft.world.entity.player.Inventory;
import http://net.minecraft.world.entity.player.Player;
import http://net.minecraft.world.item.Item;
import http://net.minecraft.world.item.ItemStack;
import http://net.minecraft.world.item.Items;
import http://net.minecraft.world.item.component.CustomData;
import http://net.minecraft.world.level.block.Blocks;
import http://net.minecraft.world.level.block.state.BlockState;
import http://net.minecraft.world.level.storage.LevelResource;
import http://net.minecraftforge.api.distmarker.Dist;
import http://net.minecraftforge.api.distmarker.OnlyIn;
import http://net.minecraftforge.client.event.InputEvent;
import http://net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import http://net.minecraftforge.client.settings.KeyConflictContext;
import http://net.minecraftforge.client.settings.KeyModifier;
import http://net.minecraftforge.event.RegisterCommandsEvent;
import http://net.minecraftforge.event.TickEvent;
import http://net.minecraftforge.event.entity.player.AttackEntityEvent;
import http://net.minecraftforge.event.entity.player.PlayerInteractEvent;
import http://net.minecraftforge.event.level.BlockEvent;
import http://net.minecraftforge.event.server.ServerStartedEvent;
import http://net.minecraftforge.event.server.ServerStoppingEvent;
import http://net.minecraftforge.fml.common.Mod;
import http://net.minecraftforge.network.ChannelBuilder;
import http://net.minecraftforge.network.PacketDistributor;
import http://net.minecraftforge.network.SimpleChannel;
import http://org.lwjgl.glfw.GLFW;

enum Unlock {
JUMP("jump", "Прыгать", 10),
ATTACK("attack", "Бить", 20),
PLACE("place", "Ставить блоки", 30),
BREAK("break", "Ломать блоки", 50);
public final String id; public final String title; public final long price;
Unlock(String id, String title, long price) { http://this.id = id; http://this.title = title; http://this.price = price; }
public static Unlock byId(String id) { for (Unlock u : values()) if (u.id.equalsIgnoreCase(id)) return u; return null; }
}

enum Machine {
SAW("saw", "Пила для дерева", 150, http://Items.IRON_AXE),
DRILL("drill", "Бур для камня", 300, http://Items.IRON_PICKAXE);
public final String id; public final String title; public final long price; private final Item base;
Machine(String id, String title, long price, Item base) { http://this.id = id; http://this.title = title; http://this.price = price; http://this.base = base; }
public static Machine byId(String id) { for (Machine m : values()) if (m.id.equalsIgnoreCase(id)) return m; return null; }
public ItemStack create() {
ItemStack s = new ItemStack(base);
CompoundTag tag = new CompoundTag(); http://tag.putBoolean("moneymod_" + id, true);
http://s.set(DataComponents.CUSTOM_DATA, http://CustomData.of(tag));
http://s.set(DataComponents.CUSTOM_NAME, http://Component.literal(title));
return s;
}
public boolean matches(ItemStack s) {
if (s.isEmpty()) return false;
CustomData d = http://s.get(DataComponents.CUSTOM_DATA);
return d != null && http://d.copyTag().contains("moneymod_" + id);
}
}

final class Prices {
private static final Map<Item, Integer> P = new HashMap<>();
static {
http://P.put(Items.COBBLESTONE, 1); http://P.put(Items.COBBLED_DEEPSLATE, 1); http://P.put(Items.SAND, 1);
http://P.put(Items.WHEAT, 1); http://P.put(Items.COAL, 3); http://P.put(Items.RAW_COPPER, 3);
http://P.put(Items.LAPIS_LAZULI, 3); http://P.put(Items.REDSTONE, 2); http://P.put(Items.RAW_IRON, 5);
http://P.put(Items.IRON_INGOT, 8); http://P.put(Items.RAW_GOLD, 8); http://P.put(Items.GOLD_INGOT, 12);
http://P.put(Items.EMERALD, 25); http://P.put(Items.DIAMOND, 40);
}
private Prices() {}
public static int unitPrice(ItemStack s) {
if (s.isEmpty()) return 0;
if (s.is(ItemTags.LOGS)) return 2;
return http://P.getOrDefault(s.getItem(), 0);
}
public static long sellHand(ServerPlayer p) {
ItemStack s = http://p.getMainHandItem(); int unit = unitPrice(s);
if (unit <= 0) return 0;
long total = (long) unit _ http://s.getCount(); http://s.setCount(0);
http://EconomyManager.add(p.getUUID(), total); return total;
}
public static long sellAll(ServerPlayer p) {
Inventory inv = http://p.getInventory(); long total = 0;
for (int i = 0; i < 36; i++) {
ItemStack s = http://inv.getItem(i); int unit = unitPrice(s);
if (unit > 0) { total += (long) unit _ http://s.getCount(); http://inv.setItem(i, http://ItemStack.EMPTY); }
}
if (total > 0) http://EconomyManager.add(p.getUUID(), total); return total;
}
}

final class EconomyManager {
private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
private static Map<String, PlayerData> data = new HashMap<>();
private static Path file;
public static final class PlayerData { public long balance = 0; public Set<String> unlocked = new HashSet<>(); }
private EconomyManager() {}
public static void load(MinecraftServer server) {
file = http://server.getWorldPath(LevelResource.ROOT).resolve("moneymod.json"); data = new HashMap<>();
if (Files.exists(file)) {
try (Reader r = http://Files.newBufferedReader(file)) {
Map<String, PlayerData> loaded = http://GSON.fromJson(r, new TypeToken<Map<String, PlayerData>>() {}.getType());
if (loaded != null) data = loaded;
} catch (IOException | RuntimeException e) { http://System.err.println("[moneymod] Не удалось загрузить: " + e); }
}
}
public static void save() {
if (file == null) return;
try (Writer w = http://Files.newBufferedWriter(file)) { http://GSON.toJson(data, w); } catch (IOException e) { http://System.err.println("[moneymod] Не удалось сохранить: " + e); }
}
public static PlayerData get(UUID id) { return http://data.computeIfAbsent(id.toString(), k -> new PlayerData()); }
public static long balance(UUID id) { return get(id).balance; }
public static void add(UUID id, long amount) { get(id).balance += amount; }
public static boolean has(UUID id, Unlock u) { return get(id).unlocked.contains(u.id); }
public static boolean buy(UUID id, Unlock u) {
PlayerData d = get(id);
if (d.unlocked.contains(u.id) || http://d.balance < http://u.price) return false;
http://d.balance -= http://u.price; http://d.unlocked.add(u.id); return true;
}
}

final class ModNetwork {
public static final SimpleChannel CHANNEL = http://ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(MoneyMod.MODID, "main")).simpleChannel().networkProtocolVersion(1);
private static int id = 0;
private ModNetwork() {}
public static void register() {
http://CHANNEL.messageBuilder(BuyPacket.class, id++).encoder(BuyPacket::encode).decoder(BuyPacket::decode).consumerMainThread(BuyPacket::handle).add();
http://CHANNEL.messageBuilder(BalancePacket.class, id++).encoder(BalancePacket::encode).decoder(BalancePacket::decode).consumerMainThread(BalancePacket::handle).add();
}
public record BuyPacket(String id) {
void encode(ByteBuf buf) { http://net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.encode(buf, id); }
static BuyPacket decode(ByteBuf buf) { return new BuyPacket(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.decode(buf)); }
void handle(net.minecraftforge.network.NetworkEvent.Context ctx) {
ServerPlayer p = http://ctx.getSender(); if (p == null) return;
String result = http://ShopLogic.buyOnServer(p, id);
http://PacketDistributor.sendToPlayer(p, new BalancePacket(EconomyManager.balance(p.getUUID()), new HashSet<>(EconomyManager.get(p.getUUID()).unlocked)));
if (result != null) http://p.displayClientMessage(Component.literal(result), true);
}
}
public record BalancePacket(long balance, Set<String> unlocked) {
void encode(ByteBuf buf) { http://buf.writeLong(balance); http://net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.encode(buf, http://String.join(",", unlocked)); }
static BalancePacket decode(ByteBuf buf) { long b = http://buf.readLong(); String s = http://net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8.decode(buf); Set<String> set = new HashSet<>(); if (!s.isEmpty()) for (String x : http://s.split(",")) http://set.add(x); return new BalancePacket(b, set); }
void handle(net.minecraftforge.network.NetworkEvent.Context ctx) { http://ClientData.balance = balance; http://ClientData.unlocked = unlocked; }
}
public static final class ClientData { public static long balance = 0; public static Set<String> unlocked = new HashSet<>(); }
}

final class ShopLogic {
private ShopLogic() {}
public static String buyOnServer(ServerPlayer p, String id) {
Unlock u = http://Unlock.byId(id);
if (u != null) {
if (EconomyManager.has(p.getUUID(), u)) return "Уже куплено";
if (!EconomyManager.buy(p.getUUID(), u)) return "Не хватает денег: нужно " + http://u.price;
http://GameEvents.refreshJump(p); http://EconomyManager.save(); return "Куплено: " + http://u.title;
}
Machine m = http://Machine.byId(id);
if (m != null) {
if (EconomyManager.balance(p.getUUID()) < http://m.price) return "Не хватает денег: нужно " + http://m.price;
http://EconomyManager.add(p.getUUID(), -m.price);
ItemStack stack = http://m.create();
if (!p.getInventory().add(stack)) http://p.drop(stack, false);
http://EconomyManager.save(); return "Куплено: " + http://m.title;
}
return "Нет такого товара";
}
}

final class GameEvents {
private static final ResourceLocation NO_JUMP = http://ResourceLocation.fromNamespaceAndPath(MoneyMod.MODID, "no_jump");
private static final int MAX_TREE_BLOCKS = 128;
private GameEvents() {}
private static boolean denied(Player player, Unlock u) {
if (!(player instanceof ServerPlayer sp)) return false;
if (EconomyManager.has(sp.getUUID(), u)) return false;
http://sp.displayClientMessage(Component.literal("Заблокировано: «" + http://u.title + "». Купи: /buy " + http://u.id), true);
return true;
}
public static boolean onBreak(BlockEvent.BreakEvent e) {
Player p = http://e.getPlayer(); if (denied(p, http://Unlock.BREAK)) return true;
if (p instanceof ServerPlayer sp && http://e.getLevel() instanceof ServerLevel level) {
ItemStack held = http://sp.getMainHandItem(); BlockPos origin = http://e.getPos(); BlockState state = http://e.getState();
if (Machine.SAW.matches(held) && http://state.is(BlockTags.LOGS)) fellTree(level, sp, origin, held);
else if (Machine.DRILL.matches(held)) drill(level, sp, origin, held);
}
return false;
}
private static void fellTree(ServerLevel level, ServerPlayer sp, BlockPos origin, ItemStack tool) {
Set<BlockPos> seen = new HashSet<>(); List<BlockPos> found = new ArrayList<>(); ArrayDeque<BlockPos> queue = new ArrayDeque<>();
http://seen.add(origin); http://queue.add(origin);
while (!queue.isEmpty() && http://found.size() < MAX_TREE_BLOCKS) {
BlockPos cur = http://queue.poll();
for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
if (dx == 0 && dy == 0 && dz == 0) continue; BlockPos n = http://cur.offset(dx, dy, dz);
if (!seen.add(n)) continue;
if (level.getBlockState(n).is(BlockTags.LOGS)) { http://found.add(n); http://queue.add(n); }
}
}
for (BlockPos pos : found) http://level.destroyBlock(pos, true, sp);
http://tool.hurtAndBreak(1, sp, http://EquipmentSlot.MAINHAND);
}
private static void drill(ServerLevel level, ServerPlayer sp, BlockPos origin, ItemStack tool) {
for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
if (dx == 0 && dy == 0 && dz == 0) continue; BlockPos pos = http://origin.offset(dx, dy, dz); BlockState st = http://level.getBlockState(pos);
float hardness = http://st.getDestroySpeed(level, pos);
if (st.is(BlockTags.MINEABLE_WITH_PICKAXE) && hardness >= 0 && hardness <= 5) http://level.destroyBlock(pos, true, sp);
}
http://tool.hurtAndBreak(1, sp, http://EquipmentSlot.MAINHAND);
}
public static boolean onPlace(BlockEvent.EntityPlaceEvent e) { Entity ent = http://e.getEntity(); return ent instanceof Player p && denied(p, http://Unlock.PLACE); }
public static boolean onAttack(AttackEntityEvent e) { return denied(e.getEntity(), http://Unlock.ATTACK); }
public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
if (!(e.getEntity() instanceof ServerPlayer sp)) return false;
if (e.getHand() != http://InteractionHand.MAIN_HAND) return false;
BlockState state = http://e.getLevel().getBlockState(e.getPos());
if (state.is(Blocks.GOLD_BLOCK) && http://sp.getMainHandItem().isEmpty()) {
http://EconomyManager.add(sp.getUUID(), 1);
http://sp.displayClientMessage(Component.literal("+1 монета (баланс: " + http://EconomyManager.balance(sp.getUUID()) + ")"), true);
return false;
}
if (state.is(Blocks.EMERALD_BLOCK) && !sp.getMainHandItem().isEmpty()) {
long got = http://Prices.sellHand(sp);
http://sp.displayClientMessage(Component.literal(got > 0 ? "Скупщик заплатил " + got + " монет" : "Скупщик это не берёт"), true);
return true;
}
return false;
}
public static void onPlayerTick(TickEvent.PlayerTickEvent.Post e) { if (e.player() instanceof ServerPlayer sp && http://sp.tickCount % 20 == 0) refreshJump(sp); }
public static void refreshJump(ServerPlayer sp) {
AttributeInstance attr = http://sp.getAttribute(Attributes.JUMP_STRENGTH); if (attr == null) return;
boolean locked = !EconomyManager.has(sp.getUUID(), http://Unlock.JUMP); boolean has = http://attr.hasModifier(NO_JUMP);
if (locked && !has) http://attr.addTransientModifier(new AttributeModifier(NO_JUMP, -1.0, http://AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
else if (!locked && has) http://attr.removeModifier(NO_JUMP);
}
}

final class MoneyCommands {
private MoneyCommands() {}
private static void ok(CommandSourceStack src, String text) { http://src.sendSuccess(() -> http://Component.literal(text), false); }
public static void register(CommandDispatcher<CommandSourceStack> d) {
http://d.register(Commands.literal("balance").executes(ctx -> { ServerPlayer p = http://ctx.getSource().getPlayerOrException(); ok(ctx.getSource(), "Баланс: " + http://EconomyManager.balance(p.getUUID()) + " монет"); return 1; }));
http://d.register(Commands.literal("shop").executes(ctx -> {
ServerPlayer p = http://ctx.getSource().getPlayerOrException(); StringBuilder sb = new StringBuilder("Магазин (/buy <id>):\nУмения:");
for (Unlock u : http://Unlock.values()) { String mark = http://EconomyManager.has(p.getUUID(), u) ? " [куплено]" : ""; http://sb.append("\n- ").append(u.id).append(": ").append(u.title).append(" — ").append(u.price).append(mark); }
http://sb.append("\nТехника:"); for (Machine m : http://Machine.values()) http://sb.append("\n- ").append(m.id).append(": ").append(m.title).append(" — ").append(m.price);
ok(ctx.getSource(), http://sb.toString()); return 1;
}));
http://d.register(Commands.literal("buy").then(Commands.argument("id", http://StringArgumentType.word()).executes(ctx -> {
CommandSourceStack src = http://ctx.getSource(); ServerPlayer p = http://src.getPlayerOrException(); String id = http://StringArgumentType.getString(ctx, "id");
String r = http://ShopLogic.buyOnServer(p, id);
if (r.startsWith("Куплено")) { ok(src, r); return 1; } http://src.sendFailure(Component.literal(r)); return 0;
})));
http://d.register(Commands.literal("sell").executes(ctx -> { ServerPlayer p = http://ctx.getSource().getPlayerOrException(); long got = http://Prices.sellHand(p); if (got <= 0) { http://ctx.getSource().sendFailure(Component.literal("Это не покупают (держи ресурс в руке)")); return 0; } ok(ctx.getSource(), "Продано за " + got + " монет"); return 1; }));
http://d.register(Commands.literal("sellall").executes(ctx -> { ServerPlayer p = http://ctx.getSource().getPlayerOrException(); long got = http://Prices.sellAll(p); if (got <= 0) { http://ctx.getSource().sendFailure(Component.literal("Нечего продавать")); return 0; } ok(ctx.getSource(), "Продано всё за " + got + " монет"); return 1; }));
}
}

@OnlyIn(Dist.CLIENT)
final class ShopScreen extends Screen {
protected ShopScreen() { super(Component.literal("Магазин")); }
@Override protected void init() {
int cx = http://this.width / 2; int y = 40;
for (Unlock u : http://Unlock.values()) {
boolean bought = http://ModNetwork.ClientData.unlocked.contains(u.id);
String label = http://u.title + " — " + http://u.price + (bought ? " [куплено]" : "");
addRenderableWidget(Button.builder(Component.literal(label), b -> { if (!bought) http://ModNetwork.CHANNEL.sendToServer(new http://ModNetwork.BuyPacket(u.id)); }).bounds(cx - 100, y, 200, 20).build());
y += 24;
}
y += 10;
for (Machine m : http://Machine.values()) {
String label = http://m.title + " — " + http://m.price;
addRenderableWidget(Button.builder(Component.literal(label), b -> http://ModNetwork.CHANNEL.sendToServer(new http://ModNetwork.BuyPacket(m.id))).bounds(cx - 100, y, 200, 20).build());
y += 24;
}
addRenderableWidget(Button.builder(Component.literal("Закрыть"), b -> http://this.onClose()).bounds(cx - 50, y + 10, 100, 20).build());
}
@Override public void render(GuiGraphics g, int mx, int my, float pt) {
http://super.render(g, mx, my, pt);
http://g.drawCenteredString(this.font, "Магазин | Баланс: " + http://ModNetwork.ClientData.balance + " монет", http://this.width / 2, 20, 0xFFFFFF);
http://g.drawCenteredString(this.font, "Нажми B или Esc чтобы закрыть", http://this.width / 2, http://this.height - 20, 0xAAAAAA);
}
@Override public boolean isPauseScreen() { return false; }
}

@OnlyIn(Dist.CLIENT)
final class ClientEvents {
public static http://net.minecraft.client.KeyMapping SHOP_KEY;
private ClientEvents() {}
public static void registerKeys(RegisterKeyMappingsEvent e) {
SHOP_KEY = new http://net.minecraft.client.KeyMapping("key.moneymod.shop", http://KeyConflictContext.IN_GAME, http://KeyModifier.NONE, http://com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM, http://GLFW.GLFW_KEY_B, "key.categories.moneymod");
http://e.register(SHOP_KEY);
}
public static void onKey(InputEvent.Key e) {
if (SHOP_KEY != null && SHOP_KEY.consumeClick()) {
Minecraft mc = http://Minecraft.getInstance();
if (mc.screen == null) http://mc.setScreen(new ShopScreen());
else if (mc.screen instanceof ShopScreen) http://mc.setScreen(null);
}
}
}

@Mod(MoneyMod.MODID)
public class MoneyMod {
public static final String MODID = "moneymod";
private static int saveTimer
