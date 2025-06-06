package projet.game.fight.bestiary;

import java.util.HashMap;

import projet.game.GameModel;
import projet.game.fight.Effects;
import projet.game.hero.Hero;

public interface Bestiary extends Effects {
	// public static final int xp;
	int[] getXY();

	void setXY(int X, int Y);

	/**
	 * 敵がどんな行動を取るか予測する
	 *
	 * @return
	 */
	public HashMap<String, Integer> prediction();

	/**
	 * 戦闘中の敵のターン
	 *
	 * @param gamedata
	 * @param hero
	 */
	public void turn(GameModel gamedata, Hero hero);

	String getImgPath();

	int getHealth();

	int getMaxHealth();

	/**
	 * 敵の次の行動
	 *
	 * @return
	 */
	String getNextAction();

	int getIntAction();

	Bestiary copie();

	void setHealth(int i);

	/**
	 * ヒーローからのダメージ
	 *
	 * @param dmg
	 */
	void damageTaken(int dmg);

	boolean getIsAlive();

	int getProtection();

	int getXp();
}
