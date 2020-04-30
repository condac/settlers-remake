package jsettlers.common.movable;

import jsettlers.common.images.ImageLink;
import jsettlers.common.player.ECivilisation;

public enum ESpellType {
	ROMAN_EYE(10, false, ECivilisation.ROMAN, "original_14_GUI_246"),
	IRRIGATE(10, true, ECivilisation.ROMAN, "original_14_GUI_249"),
	GREEN_THUMB(15, true, ECivilisation.ROMAN, "original_14_GUI_252"),
	DEFEATISM(15, true, ECivilisation.ROMAN, "original_14_GUI_255"),

	DESERTIFICATION(10, true, ECivilisation.EGYPTIAN, "original_24_GUI_240"),
	DRAIN_MOOR(10, true, ECivilisation.EGYPTIAN, "original_24_GUI_243"),
	CONVERT_FOOD(15,true, ECivilisation.EGYPTIAN, "original_24_GUI_246"),
	BURN_FOREST(15,true, ECivilisation.EGYPTIAN, "original_24_GUI_249"),

	MELT_SNOW(10, true, ECivilisation.ASIAN, "original_34_GUI_255"),
	SUMMON_STONE(10, true, ECivilisation.ASIAN, "original_34_GUI_258"),
	SUMMON_FISH(15, true, ECivilisation.ASIAN, "original_34_GUI_261"),

	AMAZON_EYE(10, true, ECivilisation.AMAZON, "original_44_GUI_249"),
	SUMMON_FOREST(10, true, ECivilisation.AMAZON, "original_44_GUI_252"),
	FREEZE_FOES(15, true, ECivilisation.AMAZON, "original_44_GUI_255"),
	SEND_GOODS(15, false, ECivilisation.AMAZON, "original_44_GUI_258"),

	// common spell
	GIFTS(20, true, null, "original_14_GUI_258"),

	GILDING(20, true, ECivilisation.ROMAN, "original_14_GUI_264"),
	CURSE_MOUNTAIN(25, true, ECivilisation.ROMAN, "original_14_GUI_267"),
	DEFECT(40, true, ECivilisation.ROMAN, "original_14_GUI_270"),

	INCREASE_MORAL(20, true, ECivilisation.EGYPTIAN, "original_24_GUI_255"),
	SEND_FOES(25, true, ECivilisation.EGYPTIAN, "original_24_GUI_258"),
	CURSE_BOWMAN(40, true, ECivilisation.EGYPTIAN, "original_24_GUI_261"),

	MELT_STONE(15, true, ECivilisation.ASIAN, "original_34_GUI_267"),
	GODLESS_HIT(20, true, ECivilisation.ASIAN, "original_34_GUI_270"),
	MOTIVATE_SWORDSMAN(25, true, ECivilisation.ASIAN, "original_34_GUI_273"),
	CALL_DEFENDERS(40, true, ECivilisation.ASIAN, "original_34_GUI_276"),

	REMOVE_GOLD(20, true, ECivilisation.AMAZON, "original_44_GUI_264"),
	CALL_GOODS(25, true, ECivilisation.AMAZON, "original_44_GUI_267"),
	DESTROY_ARROWS(40, true, ECivilisation.AMAZON, "original_44_GUI_270");

	private short manna;
	private ImageLink imageLink;
	private boolean forcePresence;

	private ECivilisation civ;

	ESpellType(int manna, boolean forcePresence, ECivilisation civ, String imageLink) {
		this.civ = civ;
		this.manna = (short) manna;
		this.forcePresence = forcePresence;
		this.imageLink = ImageLink.fromName(imageLink, 0);
	}

	public boolean availableForCiv(ECivilisation civilisation) {
		return civ == null || civ == civilisation;
	}

	public ImageLink getImageLink() {
		return imageLink;
	}

	public short getBaseCost() {
		return manna;
	}

	public short getMannaCost(int count) {
		return (short)(manna*(count/10f+1));
	}

	public boolean forcePresence() {
		return forcePresence;
	}

	public static final int GILDING_MAX_IRON = 40;

	public static final int DEFEATISM_MAX_SOLDIERS = 20;

	public static final int GIFTS_MAX_STACKS = 5;
	public static final int GIFTS_RADIUS = 3;

	public static final int CURSE_MOUNTAIN_RADIUS = 5;
	public static final float CURSE_MOUNTAIN_RESOURCE_MOD = 0.5f;

	public static final long DEFECT_MAX_ENEMIES = 10;

	public static final int IRRIGATE_RADIUS = 7;

	public static final short ROMAN_EYE_RADIUS = 10;
	public static final float ROMAN_EYE_TIME = 6;
}
