package projet.game;

import java.awt.Color;
import java.io.File;

import fr.umlv.zen5.Application;
import fr.umlv.zen5.ApplicationContext;
import fr.umlv.zen5.Event;
import fr.umlv.zen5.Event.Action;

public class GameController {

	/**
	 * クリックまたはキー入力があったか検出
	 *
	 * @param data
	 * @param event
	 * @param action
	 * @return
	 */
	public static int[] DetectionAction(GameModel data, Event event, Action action) {
		if (event != null) {
			// On clique avec la souris
			if (action == Action.POINTER_DOWN) {
				return new int[] { (int) event.getLocation().getX(),
						(int) event.getLocation().getY() };
			} else if (action == Action.KEY_PRESSED) {
				return new int[] { -1, -1 };
			}
		}
		return null;
	}

	/**
	 * ゲームのメインループ - MVCのコントローラ部分
	 * アクションを検出し、それに応じてGameModelを変更し、レビューを変更する
	 *
	 * @param context
	 * @param data
	 * @param view
	 * @return true
	 */
	private static boolean gameLoop(ApplicationContext context, GameModel data, SimpleGameView view) {
		var event = context.pollOrWaitEvent(999999999);
		if (event == null) {
			return true;
		}
		var action = event.getAction();

		int[] xy = DetectionAction(data, event, action);

		if (xy != null) {
			if (xy[0] == -1 && xy[1] == -1) {
				data.KeyAction(event);
			} else {
				data.PointerAction(xy[0], xy[1]);
			}
			data.Loop(xy[0], xy[1]);
		}

		if ((data.getHero().getHp() <= 0 || data.isWin()) && !data.isScoreAdded()) {
			data.addScore(data.isWin());
			data.setScoreAdded(true);
		}
		if (action != Action.POINTER_UP) {
			SimpleGameView.draw(context, view, data);
		}

		return true;
	}

	private static String[] getAllImage(String folderPath, String excludeFile) {
		File folder = new File(folderPath);
		String[] files = folder
				.list((dir, name) -> (name.endsWith(".png") || name.endsWith(".gif")) && !name.equals(excludeFile));
		if (files == null)
			return new String[0];
		return files;
	}

	private static void backPackHero(ApplicationContext context) {
		var screenInfo = context.getScreenInfo();
		var width = screenInfo.getWidth();
		var height = screenInfo.getHeight();
		System.out.println("size of the screen (" + width + " x " + height + ")");

		GameModel data = new GameModel();

		String dir = "data";
		String blank = "Rot_Wolf.png";
		String[] otherImage = getAllImage(dir, blank);

		var images = new ImageLoader(dir, blank, otherImage);

		var view = SimpleGameView.initGameGraphics(5, 5, (int) Math.min(width, height) - 2 * 5, images);

		SimpleGameView.draw(context, view, data);

		while (true) {
			gameLoop(context, data, view);
			if (!gameLoop(context, data, view)) {
				System.out.println("Thank you for quitting!");
				context.exit(0);
				return;
			}
		}
	}

	public static void main(String[] args) {
		Application.run(Color.DARK_GRAY, GameController::backPackHero);
	}
}
