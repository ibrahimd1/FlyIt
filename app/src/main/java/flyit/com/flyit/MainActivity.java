package flyit.com.flyit;

import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.badlogic.gdx.math.Vector2;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.LoopEntityModifier;
import org.andengine.entity.modifier.MoveXModifier;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import java.io.IOException;

public class MainActivity extends SimpleBaseGameActivity {

    private static int CAMERA_WIDTH;
    private static int CAMERA_HEIGHT;
    public static int GAME_OVER = 0;
    public static int ADD_SCORE = 1;

    private DisplayMetrics metrics;
    private Camera mCamera;
    private boolean isBegin;
    private boolean isDead;

    private BitmapTextureAtlas mBirdBitmapTextureAtlas;
    private TiledTextureRegion mBirdTextureRegion;
    private BitmapTextureAtlas mNumberBitmapTextureAtlas;
    private TiledTextureRegion mNumberTextureRegion;

    private Texture mGroundTexture;
    private TextureRegion mGroundTextureRegion;
    private Texture mReadyTexture;
    private TextureRegion mReadyTextureRegion;
    private Texture mUpperBarTexture;
    private TextureRegion mUpperBarTextureRegion;
    private Texture mLowerBarTexture;
    private TextureRegion mLowerBarTextureRegion;
    private Texture mBackgroundTexture;
    private TextureRegion mBackgroundTextureRegion;
    private Bird bird;
    private Sprite floor;
    private Sprite ready;
    private PhysicsWorld mPhysicsWorld;
    private Scene mScene;
    private AdView mAdView;

    //yeni oluşturulanlar
    private Texture texMenuArka,texMenuOyna, texMenuOynaHover,texMenuCikis, texMenuCikisHover,texMenuEnYuksekPuan;
    private TextureRegion texRegMenuArka,texRegMenuOyna, texRegMenuOynaHover,texRegMenuCikis, texRegMenuCikisHover,texRegMenuEnYuksekPuan;
    private Sprite spriteMenuArka,spriteMenuOyna, spriteMenuOynaHover,spriteMenuCikis, spriteMenuCikisHover,spriteMenuEnYuksekPuan;
    private Scene sahneMenu;
    private Scene sahneEnYuksekSkor;
    private Score mScore,mEnYuksekPuan;
    private BarManager barManager;
    SharedPreferences yuksekPuan;

    private Handler mHandler;
    private IUpdateHandler updateHandler = new IUpdateHandler() {
        @Override
        public void reset() {
        }


        @Override
        public void onUpdate(float pSecondsElapsed) {
            barManager.update(bird);
            if (floor.collidesWith(bird)) {
                bird.dead(floor);

                int enYuksekPuan = yuksekPuan.getInt("EnYuksekPuan", 0);
                Log.i("EnYuksekSkor",Integer.toString(enYuksekPuan));
                if (mScore.getScore() > enYuksekPuan) {
                    SharedPreferences.Editor editor = yuksekPuan.edit();
                    editor.putInt("EnYuksekPuan", mScore.getScore());
                    editor.commit();
                    mEnYuksekPuan.setScore(mScore.getScore());

                    Thread timerThread = new Thread(){
                        public void run(){
                            try{
                                sleep(750);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                            }finally{
                                mEngine.setScene(sahneEnYuksekSkor);
                            }
                        }
                    };
                    timerThread.start();
                }
            }
        }
    };


    private Runnable addBar = new Runnable() {

        @Override
        public void run() {
            barManager.addBar();
            mHandler.postDelayed(addBar, 1250);
        }
    };


    @Override
    protected void onSetContentView() {
        this.mRenderSurfaceView = new RenderSurfaceView(this);
        this.mRenderSurfaceView.setRenderer(this.mEngine, this);
        final android.widget.FrameLayout.LayoutParams surfaceViewLayoutParams = new FrameLayout.LayoutParams(super.createSurfaceViewLayoutParams());

        //Creating the banner view.
        mAdView=new AdView(this);
        mAdView.setAdUnitId("ca-app-pub-4739427604654377/9744808219");
        mAdView.refreshDrawableState();
        mAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        AdRequest adRequest=new AdRequest.Builder().addTestDevice("730450094504347588472C05EE0B7134").build();
        mAdView.loadAd(adRequest);
        final FrameLayout.LayoutParams adViewLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);


        final FrameLayout frameLayout = new FrameLayout(this);
        final FrameLayout.LayoutParams frameLayoutLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);

        yuksekPuan = getSharedPreferences("EnYuksekPuan", 0);

        frameLayout.addView(this.mRenderSurfaceView,surfaceViewLayoutParams);
        frameLayout.addView(mAdView,adViewLayoutParams);
        this.setContentView(frameLayout, frameLayoutLayoutParams);
    }

    private void setVisibility() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdView.setVisibility(AdView.INVISIBLE);
            }
        });
    }

    //bu metod reklamı tekrar gösterir menu sahnesinde diger sahnelerde çalıştırabilirsizin

    private void setVisibilityGoster() {
        Log.i("rek","goster");
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdView.setVisibility(AdView.VISIBLE);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mEngine.getScene().equals(sahneEnYuksekSkor)) {
                mEngine.setScene(mScene);
                restart();
            }
            else if (mEngine.getScene().equals(mScene)){
                finish();
                System.exit(0);
            }
        }
        return  true;
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        Constant.init();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == GAME_OVER) {
                    barManager.gameOver();
                    mHandler.removeCallbacks(addBar);
                    floor.clearEntityModifiers();
                    isDead = true;
                    isBegin = false;
                }
                if (msg.what == ADD_SCORE) {
                    mScore.addScore();
                }
            }
        };
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CAMERA_WIDTH = 320;
        CAMERA_HEIGHT = 480;
        mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true,
                ScreenOrientation.PORTRAIT_FIXED, new FillResolutionPolicy(),
                mCamera);
        return engineOptions;
    }


    @Override
    protected void onCreateResources() throws IOException {

        this.mBirdBitmapTextureAtlas = new BitmapTextureAtlas(
                getTextureManager(), 1024, 1024, TextureOptions.DEFAULT);
        mBirdTextureRegion = BitmapTextureAtlasTextureRegionFactory
                .createTiledFromAsset(mBirdBitmapTextureAtlas, this,
                        "gfx/bird.png", 0, 0, 3, 1);
        this.mEngine.getTextureManager().loadTexture(mBirdBitmapTextureAtlas);

        this.mNumberBitmapTextureAtlas = new BitmapTextureAtlas(
                getTextureManager(), 1024, 1024, TextureOptions.DEFAULT);
        mNumberTextureRegion = BitmapTextureAtlasTextureRegionFactory
                .createTiledFromAsset(mNumberBitmapTextureAtlas, this,
                        "gfx/score_num.png", 0, 0, 11, 1);
        this.mEngine.getTextureManager().loadTexture(mNumberBitmapTextureAtlas);

        mGroundTexture = new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/ground.png");
        mGroundTextureRegion = TextureRegionFactory
                .extractFromTexture(mGroundTexture);
        mGroundTexture.load();

        mReadyTexture = new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/ready.png");
        mReadyTextureRegion = TextureRegionFactory
                .extractFromTexture(mReadyTexture);
        mReadyTexture.load();

        mBackgroundTexture = new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/background.png");
        mBackgroundTextureRegion = TextureRegionFactory
                .extractFromTexture(mBackgroundTexture);
        mBackgroundTexture.load();

        mUpperBarTexture = new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/up_bar.png");
        mUpperBarTextureRegion = TextureRegionFactory
                .extractFromTexture(mUpperBarTexture);
        mUpperBarTexture.load();

        mLowerBarTexture = new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/low_bar.png");
        mLowerBarTextureRegion = TextureRegionFactory
                .extractFromTexture(mLowerBarTexture);
        mLowerBarTexture.load();

        isBegin = false;
        isDead = false;

        //yeni
        texMenuArka =new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/baslangic.png");
        texRegMenuArka = TextureRegionFactory
                .extractFromTexture(texMenuArka);
        texMenuArka.load();

        texMenuOyna =new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/play_button.png");
        texRegMenuOyna = TextureRegionFactory
                .extractFromTexture(texMenuOyna);
        texMenuOyna.load();

        texMenuCikis =new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/quit_button.png");
        texRegMenuCikis = TextureRegionFactory
                .extractFromTexture(texMenuCikis);
        texMenuCikis.load();

        texMenuOynaHover =new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/play_button.png");
        texRegMenuOynaHover = TextureRegionFactory
                .extractFromTexture(texMenuOynaHover);
        texMenuOynaHover.load();

        texMenuCikisHover =new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/quit_button.png");
        texRegMenuCikisHover = TextureRegionFactory
                .extractFromTexture(texMenuCikisHover);
        texMenuCikisHover.load();
        texMenuEnYuksekPuan =new AssetBitmapTexture(getTextureManager(),
                getAssets(), "gfx/best_score_screen.png");
        texRegMenuEnYuksekPuan = TextureRegionFactory
                .extractFromTexture(texMenuEnYuksekPuan);
        texMenuEnYuksekPuan.load();

    }

    @Override
    protected Scene onCreateScene() {
        this.mEngine.registerUpdateHandler(new FPSLogger());
        mScene = new Scene();
        sahneMenu=new Scene();
        sahneEnYuksekSkor=new Scene();
        AutoParallaxBackground background = new AutoParallaxBackground(0, 0, 0,
                5);
        Sprite spriteBackground = new Sprite(CAMERA_WIDTH / 2,
                CAMERA_HEIGHT / 2, mBackgroundTextureRegion,
                getVertexBufferObjectManager());
        background.attachParallaxEntity(new ParallaxEntity(-5.0f,
                spriteBackground));
        mScene.setBackground(background);

        floor = new Sprite(CAMERA_WIDTH / 2f,
                mGroundTextureRegion.getHeight() / 2f, mGroundTextureRegion,
                getVertexBufferObjectManager());
        floor.setZIndex(10);

        MoveXModifier floorModifier = new MoveXModifier(3.0f, CAMERA_WIDTH, 6f);
        floor.registerEntityModifier(new LoopEntityModifier(floorModifier));

        mScene.attachChild(floor);
        Vector2 vector2 = new Vector2(0, Constant.GRAVITY);
        mPhysicsWorld = new PhysicsWorld(vector2, false);

        bird = new Bird(CAMERA_WIDTH / 4, CAMERA_HEIGHT / 2,
                mBirdTextureRegion, getVertexBufferObjectManager(), this,
                mPhysicsWorld, mScene, mHandler);
        mScene.attachChild(bird);

        ready = new Sprite(CAMERA_WIDTH / 4 + mReadyTextureRegion.getWidth()
                / 2, CAMERA_HEIGHT / 2, mReadyTextureRegion,
                getVertexBufferObjectManager());
        mScene.attachChild(ready);


        mScene.setOnSceneTouchListener(new IOnSceneTouchListener() {
            @Override
            public boolean onSceneTouchEvent(Scene arg0, TouchEvent arg1) {
                System.out.println("isBegin: " + isBegin);
                System.out.println("isDead: " + isDead);
                if (arg1.getAction() == TouchEvent.ACTION_DOWN) {
                    if (isDead) {
                        restart();
                    } else if (!isBegin) {
                        begin();
                    }
                    bird.flyUp();
                }
                return true;
            }
        });
        barManager = new BarManager(mScene, CAMERA_WIDTH, CAMERA_HEIGHT,
                mUpperBarTextureRegion, mLowerBarTextureRegion, floor,
                getVertexBufferObjectManager(), mHandler);
        mScore = new Score(CAMERA_WIDTH / 2, CAMERA_HEIGHT - Constant.PADDING,
                mNumberTextureRegion, getVertexBufferObjectManager(), mScene);

        mEnYuksekPuan = new Score(CAMERA_WIDTH / 2, CAMERA_HEIGHT - Constant.BEST_SCORE_LOCATION,
                mNumberTextureRegion, getVertexBufferObjectManager(), sahneEnYuksekSkor);

        mScene.registerUpdateHandler(mPhysicsWorld);
        mScene.registerUpdateHandler(updateHandler);

        //yeni

        AutoParallaxBackground background2 = new AutoParallaxBackground(0, 0, 0,
                5);
        Sprite spriteBackground2 = new Sprite(CAMERA_WIDTH / 2,
                CAMERA_HEIGHT / 2, texRegMenuArka,
                getVertexBufferObjectManager());
        background2.attachParallaxEntity(new ParallaxEntity(-5.0f,
                spriteBackground2));
        sahneMenu.setBackground(background2);


        AutoParallaxBackground background3 = new AutoParallaxBackground(0, 0, 0,
                0);
        spriteMenuEnYuksekPuan = new Sprite(CAMERA_WIDTH / 2,
                CAMERA_HEIGHT / 2, texRegMenuEnYuksekPuan,
                getVertexBufferObjectManager());
        background3.attachParallaxEntity(new ParallaxEntity(0.0f,
                spriteMenuEnYuksekPuan));

        sahneEnYuksekSkor.setBackground(background3);

        spriteMenuOyna = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, texRegMenuOyna,
                getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp()) {
                    mEngine.setScene(mScene);
                }
                return true;
            }
        };

        spriteMenuOynaHover = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT - Constant.PLAY_BUTTON, texRegMenuOyna,
                getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp()) {
                    mEngine.setScene(mScene);
                    restart();
                }
                return true;
            }
        };

        spriteMenuCikis=new Sprite(CAMERA_WIDTH / 2 , CAMERA_HEIGHT/ 3, texRegMenuCikis,
                getVertexBufferObjectManager()) {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp()) {
                    finish();
                    System.exit(0);
                }
                return true;
            }
        };

        sahneMenu.registerTouchArea(spriteMenuOyna);
        sahneMenu.registerTouchArea(spriteMenuCikis);
        sahneMenu.attachChild(spriteMenuOyna);
        sahneMenu.attachChild(spriteMenuCikis);
        sahneEnYuksekSkor.registerTouchArea(spriteMenuOynaHover);
        sahneEnYuksekSkor.attachChild(spriteMenuOynaHover);

        return sahneMenu;
        //return mScene;
    }

    void restart() {
        isDead = false;
        isBegin = false;
        bird.restart();
        barManager.restart();
        ready.setAlpha(1.0f);
        mScore.resetScore();
        Log.i("SKOR", Integer.toString(mScore.getScore()));
    }

    void begin() {
        isBegin = true;
        AlphaModifier alphaModifier = new AlphaModifier(1.0f, 1.0f, 0.0f);
        ready.registerEntityModifier(alphaModifier);
        bird.begin();
        mHandler.postDelayed(addBar, 2000);
    }

    public static class Constant {
        public static float SPEED;
        public static float PADDING;
        public static float INTERVAL;
        public static float GRAVITY;
        public static float BEST_SCORE_LOCATION;
        public static float PLAY_BUTTON;

        public static void init() {

            SPEED = 8f;
            PADDING = 60f;
            INTERVAL = 100f;
            GRAVITY = -SensorManager.GRAVITY_EARTH * 2f;
            BEST_SCORE_LOCATION = 300f;
            PLAY_BUTTON = 350f;
        }
    }

}

