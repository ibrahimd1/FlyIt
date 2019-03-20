package flyit.com.flyit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import flyit.com.flyit.MainActivity.Constant;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.os.Handler;
import android.os.Message;

/*Sahnede yer alacak s�tunlar�n y�netildi�i s�n�ft�r.
 * Sahne yerle�im konumu,s�tun aralar�ndaki bo�luklar ve alt �st s�tun aral���
 * de�erleri burada hesaplan�r ve uygulan�r.
 * */
public class BarManager {
	private boolean flag;
	private ArrayList<Bar> freeBars;
	private ArrayList<Bar> busyBars;
	private Scene mScene;
	private int CAMERA_WIDTH;
	private int CAMERA_HEIGHT;
	private TextureRegion mUpperBarTextureRegion;
	private TextureRegion mLowerBarTextureRegion;
	private Sprite floor;
	private Handler mHandler;
	private VertexBufferObjectManager mVertexBufferObjectManager;

	public BarManager(Scene pScene, int width, int height, TextureRegion pUpperBarTextureRegion,
			TextureRegion pLowerBarTextureRegion, Sprite floor, VertexBufferObjectManager pVertexBufferObjectManager,
			Handler pHandler) {
		freeBars = new ArrayList<Bar>();
		busyBars = new ArrayList<Bar>();

		this.mScene = pScene;
		this.CAMERA_WIDTH = width;
		this.CAMERA_HEIGHT = height;
		this.mUpperBarTextureRegion = pUpperBarTextureRegion;
		this.mLowerBarTextureRegion = pLowerBarTextureRegion;
		this.floor = floor;
		this.flag = false;
		this.mHandler = pHandler;

		for (int i = 0; i < 4; i++) {
			Random random = new Random();
			float maxRand = CAMERA_HEIGHT - 2 * Constant.PADDING - Constant.INTERVAL - floor.getHeight();
			float randPos = random.nextFloat() * maxRand + Constant.PADDING;
			Bar upperBar = new Bar(CAMERA_WIDTH + mUpperBarTextureRegion.getWidth(), CAMERA_HEIGHT - randPos
					+ mUpperBarTextureRegion.getHeight() / 2, mUpperBarTextureRegion, mVertexBufferObjectManager,
					Bar.UPPER_BAR);
			Bar lowerBar = new Bar(CAMERA_WIDTH + mLowerBarTextureRegion.getWidth(), CAMERA_HEIGHT - randPos
					- Constant.INTERVAL - mLowerBarTextureRegion.getHeight() / 2, mLowerBarTextureRegion,
					mVertexBufferObjectManager, Bar.LOWER_BAR);
			upperBar.setZIndex(0);
			lowerBar.setZIndex(0);
			mScene.attachChild(upperBar);
			mScene.attachChild(lowerBar);
			mScene.sortChildren();
			/*Alt ve �st s�tunlar olu�turulduktan sonra listeye ekleniyor.*/
			freeBars.add(upperBar);
			freeBars.add(lowerBar);
		}
	}

	public void update(Bird bird) {
		float barRandTemp = -1;
		while (flag) {
		}
		flag = true;
		Iterator<Bar> iterator = busyBars.iterator();
		while (iterator.hasNext()) {
			Bar bar = iterator.next();

			if (bar.collidesWith(bird)) {
				bird.dead(floor);
			} else if (bar.getType() == Bar.UPPER_BAR && !bar.isGoal && (bar.getX() - bird.getX() < 3f)
					&& bar.getX() > bird.getX()) {

				Message msg = new Message();
				msg.what = MainActivity.ADD_SCORE;
				mHandler.sendMessage(msg);
				bar.isGoal = true;
				System.out.println("isGoal");
			}

			if (bar.getX() <= -bar.getWidth()) {
				if (barRandTemp == -1) {
					Random random = new Random();
					float maxRand = CAMERA_HEIGHT - 2 * Constant.PADDING - Constant.INTERVAL - floor.getHeight();
					float randPos = random.nextFloat() * maxRand + Constant.PADDING;
					bar.setPosition(CAMERA_WIDTH + mUpperBarTextureRegion.getWidth(), CAMERA_HEIGHT - randPos
							+ mUpperBarTextureRegion.getHeight() / 2);
					barRandTemp = randPos;
				} else {
					bar.setPosition(CAMERA_WIDTH + mLowerBarTextureRegion.getWidth(), CAMERA_HEIGHT - barRandTemp
							- Constant.INTERVAL - mLowerBarTextureRegion.getHeight() / 2);
					barRandTemp = -1;
				}
				bar.isGoal = false;
				freeBars.add(bar);
				iterator.remove();
			}
		}
		flag = false;
	}

	// Sahneye sutun eklemek icin kullanılır.

	public void addBar() {
		while (flag) {
			//..
		}
		flag = true;
		if (freeBars.isEmpty()) {
			Random random = new Random();
			float maxRand = CAMERA_HEIGHT - 2 * Constant.PADDING - Constant.INTERVAL - floor.getHeight();
			float randPos = random.nextFloat() * maxRand + Constant.PADDING;
			Bar upperBar = new Bar(CAMERA_WIDTH + mUpperBarTextureRegion.getWidth(), CAMERA_HEIGHT - randPos
					+ mUpperBarTextureRegion.getHeight() / 2, mUpperBarTextureRegion, mVertexBufferObjectManager,
					Bar.UPPER_BAR);
			Bar lowerBar = new Bar(CAMERA_WIDTH + mLowerBarTextureRegion.getWidth(), CAMERA_HEIGHT - randPos
					- Constant.INTERVAL - mLowerBarTextureRegion.getHeight() / 2, mLowerBarTextureRegion,
					mVertexBufferObjectManager, Bar.LOWER_BAR);
			upperBar.setZIndex(0);
			lowerBar.setZIndex(0);
			mScene.attachChild(upperBar);
			mScene.attachChild(lowerBar);
			mScene.sortChildren();
			freeBars.add(upperBar);
			freeBars.add(lowerBar);
		}
		Bar upperBar = freeBars.get(0);
		Bar lowerBar = freeBars.get(1);
		MoveXModifier moveXModifier1 = new MoveXModifier(3.0f, upperBar.getX(), -mUpperBarTextureRegion.getWidth());
		MoveXModifier moveXModifier2 = new MoveXModifier(3.0f, upperBar.getX(), -mLowerBarTextureRegion.getWidth());
		upperBar.registerEntityModifier(moveXModifier1);
		lowerBar.registerEntityModifier(moveXModifier2);
		freeBars.remove(0);
		freeBars.remove(0);
		busyBars.add(upperBar);
		busyBars.add(lowerBar);
		flag = false;
	}

	public void gameOver() {
		flag = true;
		Iterator<Bar> iterator = busyBars.iterator();
		while (iterator.hasNext()) {
			Bar bar = iterator.next();
			bar.clearEntityModifiers();
		}
		flag = false;
	}

	//Yeniden ba�lat�ld�ktan sonra Sutun konumlar�n�n belirlendi�i method a�a��dad�r.
	 // Sutun konumlar� Random olarak belirlenmektedir.Ayr�ca alt s�tun ve �st suton olarak
	 // i�lendi�ine dikkat edin.!*/
	public void restart() {
		flag = true;
		float barRandTemp = -1;
		Iterator<Bar> iterator = busyBars.iterator();
		while (iterator.hasNext()) {
			Bar bar = iterator.next();
			if (barRandTemp == -1) {
				Random random = new Random();
				float maxRand = CAMERA_HEIGHT - 2 * Constant.PADDING - Constant.INTERVAL - floor.getHeight();
				float randPos = random.nextFloat() * maxRand + Constant.PADDING;
				bar.setPosition(CAMERA_WIDTH + mUpperBarTextureRegion.getWidth(), CAMERA_HEIGHT - randPos
						+ mUpperBarTextureRegion.getHeight() / 2);
				barRandTemp = randPos;
			} else {
				bar.setPosition(CAMERA_WIDTH + mLowerBarTextureRegion.getWidth(), CAMERA_HEIGHT - barRandTemp
						- Constant.INTERVAL - mLowerBarTextureRegion.getHeight() / 2);
				barRandTemp = -1;
			}
			bar.isGoal = false;
			freeBars.add(bar);
			iterator.remove();
		}
		flag = false;
	}
}
