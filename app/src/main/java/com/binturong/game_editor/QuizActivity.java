package com.binturong.game_editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

import com.yandex.mobile.ads.banner.BannerAdSize;
import com.yandex.mobile.ads.banner.BannerAdView;
import com.yandex.mobile.ads.common.AdRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class QuizActivity extends HeaderActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private List<SnakePoints> snakePointsList = new ArrayList<>();
    private final List<SnakePoints> blockPointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTv;

    private ImageView liveView1, liveView2, liveView3, liveView4;

    private AppCompatImageButton pause, homeHead, settingsHead;

    private SurfaceHolder surfaceHolder;

    // Доступные значения: right, left, top, bottom
    private String movingPosition = "right";

    private int score = 0;
    private int maxScore;

    private static int pointsSize;

    private static final int defaultTailPints = 3;

    private static final int snakeColor = Color.parseColor("#24A413");
    //private static final int blockColor = Color.parseColor("#5BF244");
    private static final int blockColor = Color.parseColor("#3DBFF2");
    private static final int meatColor = Color.parseColor("#F241C3");

    //Допустимые значение 1 - 1000
    private int snakeMovingSpeed = 700;

    //todo: убрать дублирование
    private static final int surfaceSizeInPoints = 45;
    private int surfaceSize;

    //координаты рандомной точки еды
    private int positionX, positionY;

    private Timer timer;

    private Canvas canvas = null;

    //Цвет еды/одного звена змеи
    private Paint pointColor = null;

    private int liveCount = 3;
    boolean restart = false;
    boolean paused = false;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Vibrator vibrator;

    boolean isVibrateOn = true;

    String mode = "level";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getString("mode");
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        isVibrateOn = sharedPreferences.getBoolean("vibration", true);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        scoreTv = findViewById(R.id.scoreTv);
        maxScore = sharedPreferences.getInt("maxScore", 0);

        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);

        AppCompatImageButton gift = findViewById(R.id.gift);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);

        liveView1 = (ImageView)findViewById(R.id.live1);
        liveView2 = (ImageView)findViewById(R.id.live2);
        liveView3 = (ImageView)findViewById(R.id.live3);
        liveView4 = (ImageView)findViewById(R.id.live4);

        pause = findViewById(R.id.pause);
        homeHead = findViewById(R.id.homeHead);
        settingsHead = findViewById(R.id.settingsHead);

        surfaceView.getHolder().addCallback(this);


        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               showUpdateDialog(QuizActivity.this);
            }
        });

        giftReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraLiveDialog(QuizActivity.this);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (paused) {
                    pause.setImageResource(R.drawable.pause);
                    moveSnake();
                } else {
                    pause.setImageResource(R.drawable.play);
                    timer.purge();
                    timer.cancel();
                }

                paused = !paused;
            }
        });

        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.purge();
                timer.cancel();

                finish();
            }
        });

        settingsHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.purge();
                timer.cancel();

                Intent settingsIntent = new Intent(QuizActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        topBtn.setOnClickListener(this);
        bottomBtn.setOnClickListener(this);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);

        changeGiftVisibility();
        changeGiftReadyVisibility();

        BannerAdView banner =  findViewById(R.id.banner);
        banner.setAdUnitId("R-M-9403186-1");
        banner.setAdSize(BannerAdSize.fixedSize(this, 320, 50));
        AdRequest adRequest = new AdRequest.Builder().build();
        banner.loadAd(adRequest);
    }

    @Override
    public void onClick(View view) {
        if (paused) return;

        if (isVibrateOn) {
            vibrator.vibrate(100);
        }

        AppCompatImageButton clickedButton = (AppCompatImageButton) view;
        int clickedButtonId = clickedButton.getId();

        if (clickedButtonId == R.id.topBtn) {
            if (!movingPosition.equals("bottom")) {
                movingPosition = "top";
            }
        } else if (clickedButtonId == R.id.bottomBtn) {
            if (!movingPosition.equals("top")) {
                movingPosition = "bottom";
            }
        } else if (clickedButtonId == R.id.leftBtn) {
            if (!movingPosition.equals("right")) {
                movingPosition = "left";
            }
        } else if (clickedButtonId == R.id.rightBtn) {
            if (!movingPosition.equals("left")) {
                movingPosition = "right";
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void init() {
        restart = false;

        liveCount = 3;
        if (isExtraLiveAvailable()) {
            liveCount = 4;
            Log.d("live", Integer.toString(liveCount));
            liveView4.setVisibility(View.VISIBLE);
        } else {
            liveView4.setVisibility(View.INVISIBLE);
        }

        liveView1.setVisibility(View.VISIBLE);
        liveView2.setVisibility(View.VISIBLE);
        liveView3.setVisibility(View.VISIBLE);

        surfaceSize = surfaceView.getWidth(); //квадрат по ширине экрана
        pointsSize = surfaceSize / surfaceSizeInPoints;
        surfaceView.getHolder().setFixedSize(surfaceSize, surfaceSize);

        snakePointsList.clear();
        setBlockPointsList();
        scoreTv.setText("0");
        score = 0;

        makeNewSnake();
        addPoint();
        moveSnake();
    }

    private void setBlockPointsList() {
        for (int i = 1; i < surfaceSize / pointsSize + 2; i = i + 2) {
            //отрисовка горизонтальных рамок
            blockPointsList.add(new SnakePoints(pointsSize * i, pointsSize));
            blockPointsList.add(new SnakePoints(pointsSize * i, pointsSize * surfaceSizeInPoints));
            //отрисовка вертикальных рамок
            blockPointsList.add(new SnakePoints(pointsSize, pointsSize * i));
            blockPointsList.add(new SnakePoints(pointsSize * surfaceSizeInPoints, pointsSize * i));
        }
    }

    /**
     * Генерит рандомную позицию(нужно для еды)
     * @return
     */
    private int generateRandomPosition() {
        int max = surfaceSize / pointsSize - 2; // -2 -чтобы не рисовал за пределами поля?
        int randomPosition = new Random().nextInt(max);

        if ((randomPosition % 2) != 0) {
            randomPosition = randomPosition + 1;
        }

        return randomPosition;
    }

    /**
     * Добавление рандомной точки - еды для змеи
     */
    private void addPoint() {
        boolean isMeatPointOnBlock = true;

        while (isMeatPointOnBlock) {
            isMeatPointOnBlock = false;

            positionX = (pointsSize * generateRandomPosition()) + pointsSize;
            positionY = (pointsSize * generateRandomPosition()) + pointsSize;

            //проверка, чтобы еда не отрисовывалась на блоке
            //todo: Сейчас он рамку так же проверяет, можно это упростить.
            for (int i = 0; i < blockPointsList.size(); i++) {
                if (positionX == blockPointsList.get(i).getPositionX() &&
                        positionY == blockPointsList.get(i).getPositionY())
                {
                    isMeatPointOnBlock = true;
                    break;
                }
            }
        }
    }

    private void moveSnake() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();

                //Если змея съела еду)
                if (headPositionX == positionX && headPositionY == positionY) {
                    growShake();
                    addPoint();
                    //ну такое, рекурсия, но посмотрим, может норм
                    timer.purge();
                    timer.cancel();
                    moveSnake();
                }

                switch (movingPosition) {
                    case "right":
                        snakePointsList.get(0).setPositionX(headPositionX + pointsSize * 2);
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "left":
                        snakePointsList.get(0).setPositionX(headPositionX - pointsSize * 2);
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "top":
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY - pointsSize * 2);
                        break;
                    case "bottom":
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY + pointsSize * 2);
                        break;
                }

                //Если змейка врезалась
                if (checkGameOver(headPositionX, headPositionY)) {
                    liveCount--;
                    restart = true;
                }

                //Если игра закончена
                if (liveCount <= 0) {
                    //останавливаем таймер
                    timer.purge();
                    timer.cancel();

                    updateRecord();

                    //чтобы отрисовалось
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent resultIntent = new Intent(QuizActivity.this, ResultActivity.class);
                            resultIntent.putExtra("score", score);
                            startActivity(resultIntent);
                        }
                    });
                } else {
                    if (restart) {
                        restart = false;

                        snakePointsList.clear();
                        setBlockPointsList();
                        makeNewSnake();

                        if (liveCount == 2) {
                            liveView4.setVisibility(View.INVISIBLE);
                            liveView3.setVisibility(View.INVISIBLE); //это по факту лишнее, но пусть будет)
                        } else if (liveCount == 1) {
                            liveView4.setVisibility(View.INVISIBLE);
                            liveView3.setVisibility(View.INVISIBLE);
                            liveView2.setVisibility(View.INVISIBLE);
                        } else if (liveCount == 3) {
                            liveView4.setVisibility(View.INVISIBLE);
                        }
                    }

                    canvas = surfaceHolder.lockCanvas();

                    //Очищаем холст, бгг
                    canvas.drawColor(Color.YELLOW, PorterDuff.Mode.CLEAR);

                    //Отрисовка рандомной точки - еды
                    canvas.drawCircle(positionX, positionY, pointsSize, createPointColor("meat"));

                    //Меняет положение головы. Остальные звенья змеи будут идти следом
                    canvas.drawCircle(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY(), pointsSize, createPointColor("snake"));

                    //Движение звеньев за головой, позиция 1 - это глова
                    for (int i = 1; i < snakePointsList.size(); i++) {
                        int getTempPositionX = snakePointsList.get(i).getPositionX();
                        int getTempPositionY = snakePointsList.get(i).getPositionY();

                        snakePointsList.get(i).setPositionX(headPositionX);
                        snakePointsList.get(i).setPositionY(headPositionY);
                        canvas.drawCircle(snakePointsList.get(i).getPositionX(), snakePointsList.get(i).getPositionY(), pointsSize, createPointColor("snake"));

                        headPositionX = getTempPositionX;
                        headPositionY = getTempPositionY;
                    }

                    //отрисовка блоков
                    for (int i = 0; i < blockPointsList.size(); i++) {
                        canvas.drawCircle(blockPointsList.get(i).getPositionX(), blockPointsList.get(i).getPositionY(), pointsSize, createPointColor("block"));
                    }

                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void growShake() {
        SnakePoints snakePoints = new SnakePoints(0, 0);

        //Добавляем звено в хвост)
        snakePointsList.add(snakePoints);

        score++;
        updateRecord();

        snakeMovingSpeed = snakeMovingSpeed + 15;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTv.setText(String.valueOf(score));
            }
        });
    }

    private boolean checkGameOver(int headPositionX, int headPositionY) {

        //Если змейка врезалась в края поля
        if (snakePointsList.get(0).getPositionX() < 0 ||
                snakePointsList.get(0).getPositionY() < 0 ||
                snakePointsList.get(0).getPositionX() >= surfaceSize ||
                snakePointsList.get(0).getPositionY() >= surfaceSize)
        {
            return true;
        }

        //Если змейка врезалась в себя)
        for (int i = 1; i < snakePointsList.size(); i++) {
            if (headPositionX == snakePointsList.get(i).getPositionX() &&
                    headPositionY == snakePointsList.get(i).getPositionY())
            {
                return true;
            }
        }

        //Если змейка врезалась в блоки
        //todo: Сейчас рамку проверяет здесь, можно сделать проверять ее выше, по границам. Нужно рамку в отдельный список блоков, но потом)
        for (int i = 0; i < blockPointsList.size(); i++) {
            if (headPositionX == blockPointsList.get(i).getPositionX() &&
                    headPositionY == blockPointsList.get(i).getPositionY())
            {
                return true;
            }
        }

        if (liveCount <= 0) {
            return true;
        }

//        //Если набрано нужное количество очков
//        if (score == 1) {
//            return true;
//        }

        return false;
    }

    private Paint createPointColor(String type) {
        if (pointColor == null) {
            pointColor = new Paint();
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true); //гладкое
        }

        switch (type) {
            case "snake":
                pointColor.setColor(snakeColor);
                break;
            case "block":
                pointColor.setColor(blockColor);
                break;
            case "meat":
                pointColor.setColor(meatColor);
                break;
            default:
                pointColor.setColor(snakeColor);
                break;
        }

        return pointColor;
    }

    /**
     * Заполняет snakePointsList новой змейкой (начало игры или после того, как врезалась)
     * todo: возможно, отрефакторить нафиг. Возможно, в жопу глобальные переменные
     */
    public void makeNewSnake() {
        movingPosition = "right";
        int startPositionX = pointsSize * defaultTailPints;

        for (int i = 0; i < defaultTailPints; i++) {
            SnakePoints snakePoints = new SnakePoints(
                    startPositionX,
                    pointsSize * 3 //сносим вниз, потому что по краю рамка из блоков
            );
            snakePointsList.add(snakePoints);

            startPositionX = startPositionX - (pointsSize * 2);
        }
    }

    public void updateRecord() {
        if (score > maxScore) {
            editor.putInt("maxScore", score);
            editor.commit();
        }
    }
}