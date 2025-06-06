package projet.game.fight.bestiary;

import java.util.HashMap;
import java.util.Random;

import projet.game.GameModel;
import projet.game.fight.Effects;
import projet.game.hero.Hero;
import projet.game.items.Curse;
import projet.game.items.Items;

public class LilBee implements Bestiary, Effects {
	private final String imgPath;
	private int[] coordonates = { 0, 0, 0, 0 };
	private int health;
	private int protection;
	private final int maxHealth;
	private boolean isAlive;
	private final int xp;
	private final int[] dmg;
	private int[] shield;
	private HashMap<String, Integer> nextAction;
	private final String description;

	// アクションに対する効果
	int haste = 0; // + シールド
	int rage = 0; // + 攻撃力
	int slow = 0; // - シールド
	int weak = 0; // - 攻撃力

	// ダメージ効果
	int poison = 0;
	int burn = 0;
	int freeze = 0;
	int regen = 0;

	// その他効果
	boolean zombie = false;
	int charm = 0;
	int dodge = 0;
	int rough_side = 0;
	int sleep = 0;
	int spike = 0;

	public LilBee(int health) {
		this.imgPath = "Lil_Bee.gif";
		this.health = health;
		this.protection = 0;
		this.maxHealth = 16;
		this.isAlive = true;
		this.xp = 4;
		this.dmg = new int[] { 7, 13 };
		this.shield = new int[] { 14 };
		this.nextAction = prediction();
		this.description = "These are small bees are all drones of the queen";
	}

	@Override
	public String getImgPath() {
		return this.imgPath;
	}

	@Override
	public boolean getIsAlive() {
		return isAlive;
	}

	@Override
	public int getProtection() {
		return protection;
	}

	public int attack(int damage) {
		return (damage + rage - weak);
	}

	public void protect(int pp) {
		protection = pp + haste - slow;
	}

	public void cursed(GameModel gamedata) {
		Items curse = gamedata.getItems().randomItemByType(Curse.class);
		curse.setXY(400, 250);
		gamedata.getBackpack().addItem(curse);
		gamedata.setIsOrganize(true);
		gamedata.setCursed(true, curse);
	}

	public void resetProtection() {
		protection = 0;
	}

	@Override
	public HashMap<String, Integer> prediction() {
		Random random = new Random();
		int action = random.nextInt(3);
		HashMap<String, Integer> result = new HashMap<>();
		if (action == 0) {
			int mindmg = this.dmg[0];
			int maxdmg = this.dmg[1];
			int damage = (int) (Math.random() * (maxdmg - mindmg + 1)) + mindmg;
			result.put("Attack", damage);
		} else {
			if (this.shield.length == 1) {
				int protection = this.shield[0];
				result.put("Block", protection);
			} else if (this.shield.length == 2) {
				int minShield = this.shield[0];
				int maxShield = this.shield[1];
				int protection = (int) (Math.random() * (maxShield - minShield + 1)) + minShield;
				result.put("Block", protection);
			}
		}
		return result;
	}

	@Override
	public void turn(GameModel gameData, Hero hero) {
		resetProtection();
		// 火傷ダメージ
		this.health -= this.burn;
		// 体力が0以下なら敵は死亡 → 報酬ドロップ
		if (health <= 0) {
			this.isAlive = false;
			return;
		}
		// 敵が「睡眠（sleep）」状態でない場合、行動する
		if (sleep == 0) {
			// 敵は「Attack（攻撃）」または「Protect（防御）」などの行動を行う
			String action = nextAction.keySet().iterator().next();
			if (action.equals("Attack")) {
				hero.damageTaken(attack(nextAction.get(action)));
			} else if (action.equals("Cursed")) {
				cursed(gameData);
			} else {
				protect(nextAction.get(action));
			}
		}
		// ターン終了時の処理
		// 「毒（poison）」状態、かつ「ゾンビ（zombie）」なら回復、それ以外はダメージ
		if (poison != 0) {
			if (zombie) {
				this.health += poison;
			} else {
				this.health -= poison;
			}
		}
		// 「再生（regen）」状態、かつ「ゾンビ」ならダメージ、それ以外は回復
		if (regen != 0) {
			if (zombie) {
				this.health -= regen;
			} else {
				this.health += regen;
				if (this.health > maxHealth) {
					this.health = maxHealth;
				}
			}
		}
		// 体力が0以下であれば、敵は死亡する → isAlive を false に設定 → 資源をドロップ
		// 状態異常の効果ターンを1減らす
		decreaseEffects();
		if (health <= 0) {
			this.isAlive = false;
		} else {
			this.nextAction = prediction();
		}
	}

	@Override
	public void damageTaken(int dmg) {
		if (dodge > 0) {
			dodge--;
		} else if (rough_side > 0) {
			int remainingDamage = (dmg / 2) + freeze - protection;
			if (remainingDamage > 0) {
				protection = 0;
				health -= remainingDamage;
				rage += 1;
			} else {
				protection -= (dmg / 2) + freeze;
			}
		} else {
			int remainingDamage = dmg + freeze - protection;
			if (remainingDamage > 0) {
				protection = 0;
				health -= remainingDamage;
				rage += 1;
			} else {
				protection -= dmg + freeze;
			}
		}
		if (health <= 0) {
			isAlive = false;
		}
	}

	@Override
	public int getHaste() {
		return haste;
	}

	@Override
	public int getRage() {
		return rage;
	}

	@Override
	public int getSlow() {
		return slow;
	}

	@Override
	public int getWeak() {
		return weak;
	}

	@Override
	public int getPoison() {
		return poison;
	}

	@Override
	public int getBurn() {
		return burn;
	}

	@Override
	public int getFreeze() {
		return freeze;
	}

	@Override
	public int getRegen() {
		return regen;
	}

	@Override
	public boolean isZombie() {
		return zombie;
	}

	@Override
	public int getCharm() {
		return charm;
	}

	@Override
	public int getDodge() {
		return dodge;
	}

	@Override
	public int getRoughSide() {
		return rough_side;
	}

	@Override
	public int getSleep() {
		return sleep;
	}

	@Override
	public void setHaste(int haste) {
		this.haste += haste;
	}

	@Override
	public void setRage(int rage) {
		this.rage += rage;
	}

	@Override
	public void setSlow(int slow) {
		this.slow += slow;
	}

	@Override
	public void setWeak(int weak) {
		this.weak += weak;
	}

	@Override
	public void setPoison(int poison) {
		this.poison += poison;
	}

	@Override
	public void setBurn(int burn) {
		this.burn += burn;
	}

	@Override
	public void setFreeze(int freeze) {
		this.freeze += freeze;
	}

	@Override
	public void setRegen(int regen) {
		this.regen += regen;
	}

	@Override
	public void setZombie(boolean zombie) {
		this.zombie = zombie;
	}

	@Override
	public void setCharm(int charm) {
		this.charm += charm;
	}

	@Override
	public void setDodge(int dodge) {
		this.dodge += dodge;
	}

	@Override
	public void setRoughSide(int rough_side) {
		this.rough_side += rough_side;
	}

	@Override
	public void setSleep(int sleep) {
		this.sleep += sleep;
	}

	@Override
	public HashMap<String, Integer> getEffects() {
		HashMap<String, Integer> effetsActifs = new HashMap<>();
		if (haste > 0)
			effetsActifs.put("Haste", haste);
		if (rage > 0)
			effetsActifs.put("Rage", rage);
		if (slow > 0)
			effetsActifs.put("Slow", slow);
		if (weak > 0)
			effetsActifs.put("Weak", weak);
		if (poison > 0)
			effetsActifs.put("Poison", poison);
		if (burn > 0)
			effetsActifs.put("Burn", burn);
		if (freeze > 0)
			effetsActifs.put("Freeze", freeze);
		if (regen > 0)
			effetsActifs.put("Regen", regen);
		if (zombie)
			effetsActifs.put("Zombie", 1);
		if (charm > 0)
			effetsActifs.put("Charm", charm);
		if (dodge > 0)
			effetsActifs.put("Dodge", dodge);
		if (rough_side > 0)
			effetsActifs.put("Rough_Hide", rough_side);
		if (sleep > 0)
			effetsActifs.put("Sleep", sleep);
		if (spike > 0)
			effetsActifs.put("Spikes", spike);
		return effetsActifs;
	}

	public void decreaseEffects() {
		HashMap<String, Integer> effetsActifs = getEffects();
		for (String effet : effetsActifs.keySet()) {
			int nbEffets = 1;
			switch (effet) {
				case "Haste":
					haste = Math.max(0, haste - nbEffets);
					break;
				case "Rage":
					rage = Math.max(0, rage - nbEffets);
					break;
				case "Slow":
					slow = Math.max(0, slow - nbEffets);
					break;
				case "Weak":
					weak = Math.max(0, weak - nbEffets);
					break;
				case "Poison":
					poison = Math.max(0, poison - nbEffets);
					break;
				case "Burn":
					burn = Math.max(0, burn - nbEffets);
					break;
				case "Freeze":
					freeze = Math.max(0, freeze - nbEffets);
					break;
				case "Regen":
					regen = Math.max(0, regen - nbEffets);
					break;
				case "Zombie":
					zombie = false;
					break;
				case "Charm":
					charm = Math.max(0, charm - nbEffets);
					break;
				case "Dodge":
					dodge = Math.max(0, dodge - nbEffets);
					break;
				case "Rough_Hide":
					rough_side = Math.max(0, rough_side - nbEffets);
					break;
				case "Sleep":
					sleep = Math.max(0, sleep - nbEffets);
					break;
				case "Spikes":
					spike = Math.max(0, spike - nbEffets);
					break;
			}
		}
	}

	@Override
	public int[] getXY() {
		return coordonates;
	}

	@Override
	public void setXY(int X, int Y) {
		this.coordonates[0] = X;
		this.coordonates[1] = Y;
		this.coordonates[2] = 90 * 50;
		this.coordonates[3] = 90 * 50;
	}

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public int getMaxHealth() {
		return maxHealth;
	}

	@Override
	public Bestiary copie() {
		return new LilBee(health);
	}

	@Override
	public void setHealth(int i) {
		this.health = i;
	}

	@Override
	public String getNextAction() {
		return nextAction.keySet().iterator().next();
	}

	@Override
	public int getIntAction() {
		return nextAction.entrySet().iterator().next().getValue();
	}

	@Override
	public int getXp() {
		return xp;
	}

	public String getDescription() {
		return description;
	}
}
