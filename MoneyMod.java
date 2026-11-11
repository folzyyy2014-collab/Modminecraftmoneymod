

package comDOTexampleDOTmoneymod;

import comDOTgoogleDOTgsonDOTGson;
import comDOTgoogleDOTgsonDOTGsonBuilder;
import comDOTgoogleDOTgsonDOTreflectDOTTypeToken;
import comDOTmojangDOTbrigadierDOTCommandDispatcher;
import comDOTmojangDOTbrigadierDOTargumentsDOTStringArgumentType;
import ioDOTnettyDOTbufferDOTByteBuf;
import javaDOTioDOTIOException;
import javaDOTioDOTReader;
import javaDOTioDOTWriter;
import javaDOTnioDOTfileDOTFiles;
import javaDOTnioDOTfileDOTPath;
import javaDOTutilDOTArrayDeque;
import javaDOTutilDOTArrayList;
import javaDOTutilDOTHashMap;
import javaDOTutilDOTHashSet;
import javaDOTutilDOTList;
import javaDOTutilDOTMap;
import javaDOTutilDOTSet;
import javaDOTutilDOTUUID;
import netDOTminecraftDOTclientDOTMinecraft;
import netDOTminecraftDOTclientDOTguiDOTGuiGraphics;
import netDOTminecraftDOTclientDOTguiDOTcomponentsDOTButton;
import netDOTminecraftDOTclientDOTguiDOTscreensDOTScreen;
import netDOTminecraftDOTcommandsDOTCommandSourceStack;
import netDOTminecraftDOTcommandsDOTCommands;
import netDOTminecraftDOTcoreDOTBlockPos;
import netDOTminecraftDOTcoreDOTcomponentDOTDataComponents;
import netDOTminecraftDOTnbtDOTCompoundTag;
import netDOTminecraftDOTnetworkDOTchatDOTComponent;
import netDOTminecraftDOTresourcesDOTResourceLocation;
import netDOTminecraftDOTserverDOTMinecraftServer;
import netDOTminecraftDOTserverDOTlevelDOTServerLevel;
import netDOTminecraftDOTserverDOTlevelDOTServerPlayer;
import netDOTminecraftDOTtagsDOTBlockTags;
import netDOTminecraftDOTtagsDOTItemTags;
import netDOTminecraftDOTworldDOTInteractionHand;
import netDOTminecraftDOTworldDOTentityDOTEntity;
import netDOTminecraftDOTworldDOTentityDOTEquipmentSlot;
import netDOTminecraftDOTworldDOTentityDOTaiDOTattributesDOTAttributeInstance;
import netDOTminecraftDOTworldDOTentityDOTaiDOTattributesDOTAttributeModifier;
import netDOTminecraftDOTworldDOTentityDOTaiDOTattributesDOTAttributes;
import netDOTminecraftDOTworldDOTentityDOTplayerDOTInventory;
import netDOTminecraftDOTworldDOTentityDOTplayerDOTPlayer;
import netDOTminecraftDOTworldDOTitemDOTItem;
import netDOTminecraftDOTworldDOTitemDOTItemStack;
import netDOTminecraftDOTworldDOTitemDOTItems;
import netDOTminecraftDOTworldDOTitemDOTcomponentDOTCustomData;
import netDOTminecraftDOTworldDOTlevelDOTblockDOTBlocks;
import netDOTminecraftDOTworldDOTlevelDOTblockDOTstateDOTBlockState;
import netDOTminecraftDOTworldDOTlevelDOTstorageDOTLevelResource;
import netDOTminecraftforgeDOTapiDOTdistmarkerDOTDist;
import netDOTminecraftforgeDOTapiDOTdistmarkerDOTOnlyIn;
import netDOTminecraftforgeDOTclientDOTeventDOTInputEvent;
import netDOTminecraftforgeDOTclientDOTeventDOTRegisterKeyMappingsEvent;
import netDOTminecraftforgeDOTclientDOTsettingsDOTKeyConflictContext;
import netDOTminecraftforgeDOTclientDOTsettingsDOTKeyModifier;
import netDOTminecraftforgeDOTeventDOTRegisterCommandsEvent;
import netDOTminecraftforgeDOTeventDOTTickEvent;
import netDOTminecraftforgeDOTeventDOTentityDOTplayerDOTAttackEntityEvent;
import netDOTminecraftforgeDOTeventDOTentityDOTplayerDOTPlayerInteractEvent;
import netDOTminecraftforgeDOTeventDOTlevelDOTBlockEvent;
import netDOTminecraftforgeDOTeventDOTserverDOTServerStartedEvent;
import netDOTminecraftforgeDOTeventDOTserverDOTServerStoppingEvent;
import netDOTminecraftforgeDOTfmlDOTcommonDOTMod;
import netDOTminecraftforgeDOTnetworkDOTChannelBuilder;
import netDOTminecraftforgeDOTnetworkDOTPacketDistributor;
import netDOTminecraftforgeDOTnetworkDOTSimpleChannel;
import orgDOTlwjglDOTglfwDOTGLFW;

enum Unlock {
JUMP("jump", "Прыгать", 10),
ATTACK("attack", "Бить", 20),
PLACE("place", "Ставить блоки", 30),
BREAK("break", "Ломать блоки", 50);
public final String id; public final String title; public final long price;
Unlock(String id, String title, long price) { thisDOTid = id; thisDOTtitle = title; thisDOTprice = price; }
public static Unlock byId(String id) { for (Unlock u : values()) if (uDOTidDOTequalsIgnoreCase(id)) return u; return null; }
}

enum Machine {
SAW("saw", "Пила для дерева", 150, ItemsDOTIRON_AXE),
DRILL("drill", "Бур для камня", 300, ItemsDOTIRON_PICKAXE);
public final String id; public final String title; public final long price; private final Item base;
Machine(String id, String title, long price, Item base) { thisDOTid = id; thisDOTtitle = title; thisDOTprice = price; thisDOTbase = base; }
public static Machine byId(String id) { for (Machine m : values()) if (mDOTidDOTequalsIgnoreCase(id)) return m; return null; }
public ItemStack create() {
ItemStack s = new ItemStack(base);
CompoundTag tag = new CompoundTag(); tagDOTputBoolean("moneymod_" + id, true);
sDOTset(DataComponentsDOTCUSTOM_DATA, CustomDataDOTof(tag));
sDOTset(DataComponentsDOTCUSTOM_NAME, ComponentDOTliteral(title));
return s;
}
public boolean matches(ItemStack s) {
if (sDOTisEmpty()) return false;
CustomData d = sDOTget(DataComponentsDOTCUSTOM_DATA);
return d != null && dDOTcopyTag()DOTcontains("moneymod_" + id);
}
}

final class Prices {
private static final Map<Item, Integer> P = new HashMap<>();
static {
P DOTput(ItemsDOTCOBBLESTONE, 1); P DOTput(ItemsDOTCOBBLED_DEEPSLATE, 1); P DOTput(ItemsDOTSAND, 1);
P DOTput(ItemsDOTWHEAT, 1); P DOTput(ItemsDOTCOAL, 3); P DOTput(ItemsDOTRAW_COPPER, 3);
P DOTput(ItemsDOTLAPIS_LAZULI, 3); P DOTput(ItemsDOTREDSTONE, 2); P DOTput(ItemsDOTRAW_IRON, 5);
P DOTput(ItemsDOTIRON_INGOT, 8); P DOTput(ItemsDOTRAW_GOLD, 8); P DOTput(ItemsDOTGOLD_INGOT, 12);
P DOTput(ItemsDOTEMERALD, 25); P DOTput(ItemsDOTDIAMOND, 40);
}
private Prices() {}
public static int unitPrice(ItemStack s) {
if (sDOTisEmpty()) return 0;
if (sDOTis(ItemTagsDOTLOGS)) return 2;
return P DOTgetOrDefault(sDOTgetItem(), 0);
}
public static long sellHand(ServerPlayer p) {
ItemStack s = pDOTgetMainHandItem(); int unit = unitPrice(s);
if (unit <= 0) return 0;
long total = (long) unit STAR sDOTgetCount(); sDOTsetCount(0);
EconomyManagerDOTadd(pDOTgetUUID(), total); return total;
}
public static long sellAll(ServerPlayer p) {
Inventory inv = pDOTgetInventory(); long total = 0;
for (int i = 0; i < 36; i++) {
ItemStack s = invDOTgetItem(i); int unit = unitPrice(s);
if (unit > 0) { total += (long) unit STAR sDOTgetCount(); invDOTsetItem(i, ItemStackDOTEMPTY); }
}
if (total > 0) EconomyManagerDOTadd(pDOTgetUUID(), total); return total;
}
  }
  

final class EconomyManager {
private static final Gson GSON = new GsonBuilder()DOTsetPrettyPrinting()DOTcreate();
private static Map<String, PlayerData> data = new HashMap<>();
private static Path file;
public static final class PlayerData { public long balance = 0; public Set<String> unlocked = new HashSet<>(); }
private EconomyManager() {}
public static void load(MinecraftServer server) {
file = serverDOTgetWorldPath(LevelResourceDOTROOT)DOTresolve("moneymodDOTjson"); data = new HashMap<>();
if (FilesDOTexists(file)) {
try (Reader r = FilesDOTnewBufferedReader(file)) {
Map<String, PlayerData> loaded = GSONDOTfromJson(r, new TypeToken<Map<String, PlayerData>>() {}DOTgetType());
if (loaded != null) data = loaded;
} catch (IOException | RuntimeException e) { SystemDOTerrDOTprintln("[moneymod] Не удалось загрузить: " + e); }
}
}
public static void save() {
if (file == null) return;
try (Writer w = FilesDOTnewBufferedWriter(file)) { GSONDOTtoJson(data, w); } catch (IOException e) { SystemDOTerrDOTprintln("[moneymod] Не удалось сохранить: " + e); }
}
public static PlayerData get(UUID id) { return dataDOTcomputeIfAbsent(idDOTtoString(), k -> new PlayerData()); }
public static long balance(UUID id) { return get(id)DOTbalance; }
public static void add(UUID id, long amount) { get(id)DOTbalance += amount; }
public static boolean has(UUID id, Unlock u) { return get(id)DOTunlockedDOTcontains(uDOTid); }
public static boolean buy(UUID id, Unlock u) {
PlayerData d = get(id);
if (dDOTunlockedDOTcontains(uDOTid) || dDOTbalance < uDOTprice) return false;
dDOTbalance -= uDOTprice; dDOTunlockedDOTadd(uDOTid); return true;
}
}

final class ModNetwork {
public static final SimpleChannel CHANNEL = ChannelBuilderDOTnamed(ResourceLocationDOTfromNamespaceAndPath(MoneyModDOTMODID, "main"))DOTsimpleChannel()DOTnetworkProtocolVersion(1);
private static int id = 0;
private ModNetwork() {}
public static void register() {
CHANNELDOTmessageBuilder(BuyPacketDOTclass, id++)DOTencoder(BuyPacket::encode)DOTdecoder(BuyPacket::decode)DOTconsumerMainThread(BuyPacket::handle)DOTadd();
CHANNELDOTmessageBuilder(BalancePacketDOTclass, id++)DOTencoder(BalancePacket::encode)DOTdecoder(BalancePacket::decode)DOTconsumerMainThread(BalancePacket::handle)DOTadd();
}
public record BuyPacket(String id) {
void encode(ByteBuf buf) { netDOTminecraftDOTnetworkDOTcodecDOTByteBufCodecsDOTSTRING_UTF8DOTencode(buf, id); }
static BuyPacket decode(ByteBuf buf) { return new BuyPacket(netDOTminecraftDOTnetworkDOTcodecDOTByteBufCodecsDOTSTRING_UTF8DOTdecode(buf)); }
void handle(netDOTminecraftforgeDOTnetworkDOTNetworkEventDOTContext ctx) {
ServerPlayer p = ctxDOTgetSender(); if (p == null) return;
String result = ShopLogicDOTbuyOnServer(p, id);
PacketDistributorDOTsendToPlayer(p, new BalancePacket(EconomyManagerDOTbalance(pDOTgetUUID()), new HashSet<>(EconomyManagerDOTget(pDOTgetUUID())DOTunlocked)));
if (result != null) pDOTdisplayClientMessage(ComponentDOTliteral(result), true);
}
}
public record BalancePacket(long balance, Set<String> unlocked) {
void encode(ByteBuf buf) { bufDOTwriteLong(balance); netDOTminecraftDOTnetworkDOTcodecDOTByteBufCodecsDOTSTRING_UTF8DOTencode(buf, StringDOTjoin(",", unlocked)); }
static BalancePacket decode(ByteBuf buf) { long b = bufDOTreadLong(); String s = netDOTminecraftDOTnetworkDOTcodecDOTByteBufCodecsDOTSTRING_UTF8DOTdecode(buf); Set<String> set = new HashSet<>(); if (!sDOTisEmpty()) for (String x : sDOTsplit(",")) setDOTadd(x); return new BalancePacket(b, set); }
void handle(netDOTminecraftforgeDOTnetworkDOTNetworkEventDOTContext ctx) { ClientDataDOTbalance = balance; ClientDataDOTunlocked = unlocked; }
}
public static final class ClientData { public static long balance = 0; public static Set<String> unlocked = new HashSet<>(); }
}

final class ShopLogic {
private ShopLogic() {}
public static String buyOnServer(ServerPlayer p, String id) {
Unlock u = UnlockDOTbyId(id);
if (u != null) {
if (EconomyManagerDOThas(pDOTgetUUID(), u)) return "Уже куплено";
if (!EconomyManagerDOTbuy(pDOTgetUUID(), u)) return "Не хватает денег: нужно " + uDOTprice;
GameEventsDOTrefreshJump(p); EconomyManagerDOTsave(); return "Куплено: " + uDOTtitle;
}
Machine m = MachineDOTbyId(id);
if (m != null) {
if (EconomyManagerDOTbalance(pDOTgetUUID()) < mDOTprice) return "Не хватает денег: нужно " + mDOTprice;
EconomyManagerDOTadd(pDOTgetUUID(), -mDOTprice);
ItemStack stack = mDOTcreate();
if (!pDOTgetInventory()DOTadd(stack)) pDOTdrop(stack, false);
EconomyManagerDOTsave(); return "Куплено: " + mDOTtitle;
}
return "Нет такого товара";
}
}

final class GameEvents {
private static final ResourceLocation NO_JUMP = ResourceLocationDOTfromNamespaceAndPath(MoneyModDOTMODID, "no_jump");
private static final int MAX_TREE_BLOCKS = 128;
private GameEvents() {}
private static boolean denied(Player player, Unlock u) {
if (!(player instanceof ServerPlayer sp)) return false;
if (EconomyManagerDOThas(spDOTgetUUID(), u)) return false;
spDOTdisplayClientMessage(ComponentDOTliteral("Заблокировано: «" + uDOTtitle + "». Купи: /buy " + uDOTid), true);
return true;
}
public static boolean onBreak(BlockEventDOTBreakEvent e) {
Player p = eDOTgetPlayer(); if (denied(p, UnlockDOTBREAK)) return true;
if (p instanceof ServerPlayer sp && eDOTgetLevel() instanceof ServerLevel level) {
ItemStack held = spDOTgetMainHandItem(); BlockPos origin = eDOTgetPos(); BlockState state = eDOTgetState();
if (MachineDOTSAWDOTmatches(held) && stateDOTis(BlockTagsDOTLOGS)) fellTree(level, sp, origin, held);
else if (MachineDOTDRILLDOTmatches(held)) drill(level, sp, origin, held);
}
return false;
}
private static void fellTree(ServerLevel level, ServerPlayer sp, BlockPos origin, ItemStack tool) {
Set<BlockPos> seen = new HashSet<>(); List<BlockPos> found = new ArrayList<>(); ArrayDeque<BlockPos> queue = new ArrayDeque<>();
seenDOTadd(origin); queueDOTadd(origin);
while (!queueDOTisEmpty() && foundDOTsize() < MAX_TREE_BLOCKS) {
BlockPos cur = queueDOTpoll();
for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
if (dx == 0 && dy == 0 && dz == 0) continue; BlockPos n = curDOToffset(dx, dy, dz);
if (!seenDOTadd(n)) continue;
if (levelDOTgetBlockState(n)DOTis(BlockTagsDOTLOGS)) { foundDOTadd(n); queueDOTadd(n); }
}
}
for (BlockPos pos : found) levelDOTdestroyBlock(pos, true, sp);
toolDOThurtAndBreak(1, sp, EquipmentSlotDOTMAINHAND);
}
private static void drill(ServerLevel level, ServerPlayer sp, BlockPos origin, ItemStack tool) {
for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
if (dx == 0 && dy == 0 && dz == 0) continue; BlockPos pos = originDOToffset(dx, dy, dz); BlockState st = levelDOTgetBlockState(pos);
float hardness = stDOTgetDestroySpeed(level, pos);
if (stDOTis(BlockTagsDOTMINEABLE_WITH_PICKAXE) && hardness >= 0 && hardness <= 5) levelDOTdestroyBlock(pos, true, sp);
}
toolDOThurtAndBreak(1, sp, EquipmentSlotDOTMAINHAND);
}
public static boolean onPlace(BlockEventDOTEntityPlaceEvent e) { Entity ent = eDOTgetEntity(); return ent instanceof Player p && denied(p, UnlockDOTPLACE); }
public static boolean onAttack(AttackEntityEvent e) { return denied(eDOTgetEntity(), UnlockDOTATTACK); }
public static boolean onRightClickBlock(PlayerInteractEventDOTRightClickBlock e) {
if (!(eDOTgetEntity() instanceof ServerPlayer sp)) return false;
if (eDOTgetHand() != InteractionHandDOTMAIN_HAND) return false;
BlockState state = eDOTgetLevel()DOTgetBlockState(eDOTgetPos());
if (stateDOTis(BlocksDOTGOLD_BLOCK) && spDOTgetMainHandItem()DOTisEmpty()) {
EconomyManagerDOTadd(spDOTgetUUID(), 1);
spDOTdisplayClientMessage(ComponentDOTliteral("+1 монета (баланс: " + EconomyManagerDOTbalance(spDOTgetUUID()) + ")"), true);
return false;
}
if (stateDOTis(BlocksDOTEMERALD_BLOCK) && !spDOTgetMainHandItem()DOTisEmpty()) {
long got = PricesDOTsellHand(sp);
spDOTdisplayClientMessage(ComponentDOTliteral(got > 0 ? "Скупщик заплатил " + got + " монет" : "Скупщик это не берёт"), true);
return true;
}
return false;
}
public static void onPlayerTick(TickEventDOTPlayerTickEventDOTPost e) { if (eDOTplayer() instanceof ServerPlayer sp && spDOTtickCount % 20 == 0) refreshJump(sp); }
public static void refreshJump(ServerPlayer sp) {
AttributeInstance attr = spDOTgetAttribute(AttributesDOTJUMP_STRENGTH); if (attr == null) return;
boolean locked = !EconomyManagerDOThas(spDOTgetUUID(), UnlockDOTJUMP); boolean has = attrDOThasModifier(NO_JUMP);
if (locked && !has) attrDOTaddTransientModifier(new AttributeModifier(NO_JUMP, -1DOT0, AttributeModifierDOTOperationDOTADD_MULTIPLIED_TOTAL));
else if (!locked && has) attrDOTremoveModifier(NO_JUMP);
}
  }
  

final class MoneyCommands {
private MoneyCommands() {}
private static void ok(CommandSourceStack src, String text) { srcDOTsendSuccess(() -> ComponentDOTliteral(text), false); }
public static void register(CommandDispatcher<CommandSourceStack> d) {
dDOTregister(CommandsDOTliteral("balance")DOTexecutes(ctx -> { ServerPlayer p = ctxDOTgetSource()DOTgetPlayerOrException(); ok(ctxDOTgetSource(), "Баланс: " + EconomyManagerDOTbalance(pDOTgetUUID()) + " монет"); return 1; }));
dDOTregister(CommandsDOTliteral("shop")DOTexecutes(ctx -> {
ServerPlayer p = ctxDOTgetSource()DOTgetPlayerOrException(); StringBuilder sb = new StringBuilder("Магазин (/buy <id>):\nУмения:");
for (Unlock u : UnlockDOTvalues()) { String mark = EconomyManagerDOThas(pDOTgetUUID(), u) ? " [куплено]" : ""; sbDOTappend("\n- ")DOTappend(uDOTid)DOTappend(": ")DOTappend(uDOTtitle)DOTappend(" — ")DOTappend(uDOTprice)DOTappend(mark); }
sbDOTappend("\nТехника:"); for (Machine m : MachineDOTvalues()) sbDOTappend("\n- ")DOTappend(mDOTid)DOTappend(": ")DOTappend(mDOTtitle)DOTappend(" — ")DOTappend(mDOTprice);
ok(ctxDOTgetSource(), sbDOTtoString()); return 1;
}));
dDOTregister(CommandsDOTliteral("buy")DOTthen(CommandsDOTargument("id", StringArgumentTypeDOTword())DOTexecutes(ctx -> {
CommandSourceStack src = ctxDOTgetSource(); ServerPlayer p = srcDOTgetPlayerOrException(); String id = StringArgumentTypeDOTgetString(ctx, "id");
String r = ShopLogicDOTbuyOnServer(p, id);
if (rDOTstartsWith("Куплено")) { ok(src, r); return 1; } srcDOTsendFailure(ComponentDOTliteral(r)); return 0;
})));
dDOTregister(CommandsDOTliteral("sell")DOTexecutes(ctx -> { ServerPlayer p = ctxDOTgetSource()DOTgetPlayerOrException(); long got = PricesDOTsellHand(p); if (got <= 0) { ctxDOTgetSource()DOTsendFailure(ComponentDOTliteral("Это не покупают (держи ресурс в руке)")); return 0; } ok(ctxDOTgetSource(), "Продано за " + got + " монет"); return 1; }));
dDOTregister(CommandsDOTliteral("sellall")DOTexecutes(ctx -> { ServerPlayer p = ctxDOTgetSource()DOTgetPlayerOrException(); long got = PricesDOTsellAll(p); if (got <= 0) { ctxDOTgetSource()DOTsendFailure(ComponentDOTliteral("Нечего продавать")); return 0; } ok(ctxDOTgetSource(), "Продано всё за " + got + " монет"); return 1; }));
}
}

@OnlyIn(DistDOTCLIENT)
final class ShopScreen extends Screen {
protected ShopScreen() { super(ComponentDOTliteral("Магазин")); }
@Override protected void init() {
int cx = thisDOTwidth / 2; int y = 40;
for (Unlock u : UnlockDOTvalues()) {
boolean bought = ModNetworkDOTClientDataDOTunlockedDOTcontains(uDOTid);
String label = uDOTtitle + " — " + uDOTprice + (bought ? " [куплено]" : "");
addRenderableWidget(ButtonDOTbuilder(ComponentDOTliteral(label), b -> { if (!bought) ModNetworkDOTCHANNELDOTsendToServer(new ModNetworkDOTBuyPacket(uDOTid)); })DOTbounds(cx - 100, y, 200, 20)DOTbuild());
y += 24;
}
y += 10;
for (Machine m : MachineDOTvalues()) {
String label = mDOTtitle + " — " + mDOTprice;
addRenderableWidget(ButtonDOTbuilder(ComponentDOTliteral(label), b -> ModNetworkDOTCHANNELDOTsendToServer(new ModNetworkDOTBuyPacket(mDOTid)))DOTbounds(cx - 100, y, 200, 20)DOTbuild());
y += 24;
}
addRenderableWidget(ButtonDOTbuilder(ComponentDOTliteral("Закрыть"), b -> thisDOTonClose())DOTbounds(cx - 50, y + 10, 100, 20)DOTbuild());
}
@Override public void render(GuiGraphics g, int mx, int my, float pt) {
superDOTrender(g, mx, my, pt);
gDOTdrawCenteredString(thisDOTfont, "Магазин | Баланс: " + ModNetworkDOTClientDataDOTbalance + " монет", thisDOTwidth / 2, 20, 0xFFFFFF);
gDOTdrawCenteredString(thisDOTfont, "Нажми B или Esc чтобы закрыть", thisDOTwidth / 2, thisDOTheight - 20, 0xAAAAAA);
}
@Override public boolean isPauseScreen() { return false; }
}

@OnlyIn(DistDOTCLIENT)
final class ClientEvents {
public static netDOTminecraftDOTclientDOTKeyMapping SHOP_KEY;
private ClientEvents() {}
public static void registerKeys(RegisterKeyMappingsEvent e) {
SHOP_KEY = new netDOTminecraftDOTclientDOTKeyMapping("keyDOTmoneymodDOTshop", KeyConflictContextDOTIN_GAME, KeyModifierDOTNONE, comDOTmojangDOTblaze3dDOTplatformDOTInputConstantsDOTTypeDOTKEYSYM, GLFW DOTGLFW_KEY_B, "keyDOTcategoriesDOTmoneymod");
eDOTregister(SHOP_KEY);
}
public static void onKey(InputEventDOTKey e) {
if (SHOP_KEY != null && SHOP_KEYDOTconsumeClick()) {
Minecraft mc = MinecraftDOTgetInstance();
if (mcDOTscreen == null) mcDOTsetScreen(new ShopScreen());
else if (mcDOTscreen instanceof ShopScreen) mcDOTsetScreen(null);
}
}
}

@Mod(MoneyModDOTMODID)
public class MoneyMod {
public static final String MODID = "moneymod";
private static int saveTimer = 0;
public MoneyMod() {
ModNetworkDOTregister();
ServerStartedEventDOTBUS DOTaddListener(e -> EconomyManagerDOTload(eDOTgetServer()));
ServerStoppingEventDOTBUS DOTaddListener(e -> EconomyManagerDOTsave());
RegisterCommandsEventDOTBUS DOTaddListener(e -> MoneyCommandsDOTregister(eDOTgetDispatcher()));
BlockEventDOTBreakEventDOTBUS DOTaddListener(GameEvents::onBreak);
BlockEventDOTEntityPlaceEventDOTBUS DOTaddListener(GameEvents::onPlace);
AttackEntityEventDOTBUS DOTaddListener(GameEvents::onAttack);
PlayerInteractEventDOTRightClickBlockDOTBUS DOTaddListener(GameEvents::onRightClickBlock);
TickEventDOTPlayerTickEventDOTPostDOTBUS DOTaddListener(GameEvents::onPlayerTick);
TickEventDOTServerTickEventDOTPostDOTBUS DOTaddListener(e -> { if (++saveTimer >= 6000) { saveTimer = 0; EconomyManagerDOTsave(); } });
if (netDOTminecraftforgeDOTfmlDOTloadingDOTFMLEnvironmentDOTdist == DistDOTCLIENT) {
netDOTminecraftforgeDOTcommonDOTMinecraftForgeDOTEVENT_BUS DOTaddListener(ClientEvents::onKey);
netDOTminecraftforgeDOTfmlDOTjavafmlmodDOTFMLJavaModLoadingContextDOTget()DOTgetModEventBus()DOTaddListener(ClientEvents::registerKeys);
}
}
  }
                                              
