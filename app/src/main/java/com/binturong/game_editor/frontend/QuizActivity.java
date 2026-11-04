package com.binturong.game_editor.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
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

import com.binturong.game_editor.R;
import com.binturong.game_editor.backend.data.SnakePoints;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.binturong.game_editor.backend.entity.Level;
import com.binturong.game_editor.backend.service.DatabaseClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class QuizActivity extends HeaderActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private List<SnakePoints> snakePointsList = new ArrayList<>();
    private final List<SnakePoints> blockPointsList = new ArrayList<>();
    private SurfaceView surfaceView;
    private TextView scoreTv, levelView;

    private ImageView liveView1, liveView2, liveView3, liveView4;
    private SurfaceHolder surfaceHolder;

    // Доступные значения: right, left, top, bottom
    private String movingPosition = "right";

    private int score = 0, targetScore = 12;
    private int maxScore;

    private static int pointsSize;

    private static final int defaultTailPints = 3;

    private static final String snakeColor = "#406835";
    //private static final int blockColor = Color.parseColor("#5BF244");
    //private static final int blockColor = Color.parseColor("#3DBFF2");
    private static final String blockColor = "#43AEB5";
    //private static final int meatColor = Color.parseColor("#F241C3");
    private static final String meatColor ="#DA342E";

    //Допустимые значение 1 - 1000
    private int snakeMovingSpeed = 805;

    //todo: убрать дублирование
    private static final int surfaceSizeInPoints = 43;
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

    int currentLevel = 1;

    private MediaPlayer mediaPlayer;

    private int customLevelId = -1;
    private JSONArray customBlocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = sharedPreferences.edit();

        scoreTv = findViewById(R.id.scoreTv);
        maxScore = sharedPreferences.getInt("maxScore", 0);

        levelView = findViewById(R.id.level_view);

        mediaPlayer = MediaPlayer.create(this, R.raw.crunch);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mode = extras.getString("mode");
            customLevelId = extras.getInt("levelId", -1);
        }

        if (Objects.equals(mode, "level")) {
            if (setLevelData()) {
                scoreTv.setText("0/" + targetScore);
                levelView.setVisibility(View.VISIBLE);
                levelView.setText("ур " + currentLevel);
            }
        } else if (Objects.equals(mode, "custom")) {
            if (setCustomLevelData()) {
                scoreTv.setText("0/" + targetScore);
                levelView.setVisibility(View.VISIBLE);
                levelView.setText("ур #" + customLevelId);
            }
        } else {
            levelView.setVisibility(View.GONE);
        }

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        isVibrateOn = sharedPreferences.getBoolean("vibration", true);

        setKeyboardButtons();
        setHeaderButtons();
        setLiveView();

        changeGiftVisibility();
        changeGiftReadyVisibility();

        setAddBanner();

        setSurfaceView();
    }

    private boolean isLevelMode() {
        return Objects.equals(mode, "level") || Objects.equals(mode, "custom");
    }

    /**
     * Загружает уровень из локальной БД. Запрос маленький (по первичному ключу),
     * поэтому ждём результат, чтобы targetScore и скорость были готовы до старта игры.
     */
    private boolean setCustomLevelData() {
        Log.d("idLevelCustom", String.valueOf(customLevelId));
        if (customLevelId < 0) return false;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Level level = executor.submit(new java.util.concurrent.Callable<Level>() {
                @Override
                public Level call() {
                    return DatabaseClient.getInstance(QuizActivity.this)
                            .levelDao().getById(customLevelId);
                }
            }).get();

            if (level == null || level.blocks == null || level.blocks.isEmpty()) {
                return false;
            }

            targetScore = level.targetScore > 0 ? level.targetScore : 12;
            // допустимые значения скорости 1 - 1000, при 1000 задержка таймера будет 0
            snakeMovingSpeed = (level.startSpeed >= 1 && level.startSpeed < 1000)
                    ? level.startSpeed : 805;

            customBlocks = new JSONArray(level.blocks);
            Log.d("customBloack", String.valueOf(customBlocks));
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            executor.shutdown();
        }
    }

    /** Вызывать только после того, как посчитан pointsSize (то есть из init()) */
    private void addCustomBlocks() {
        try {
            for (int i = 0; i < customBlocks.length(); i++) {
                JSONObject c = customBlocks.getJSONObject(i);
                blockPointsList.add(new SnakePoints(
                        pointsSize * c.getInt("x"),
                        pointsSize * c.getInt("y")
                ));
            }
        } catch (JSONException ignored) {
        }
    }


    private void setSurfaceView() {
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        surfaceView.getHolder().addCallback(this);
    }

    private void setHeaderButtons() {
        AppCompatImageButton homeHead = findViewById(R.id.homeHead);
        AppCompatImageButton settingsHead = findViewById(R.id.settingsHead);
        AppCompatImageButton gift = findViewById(R.id.gift);
        AppCompatImageButton giftReady = findViewById(R.id.giftReady);

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

        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.purge();
                timer.cancel();

                Intent mainIntent = new Intent(QuizActivity.this, MainActivity.class);
                startActivity(mainIntent);
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
    }

    private void setLiveView() {
        liveView1 = (ImageView)findViewById(R.id.live1);
        liveView2 = (ImageView)findViewById(R.id.live2);
        liveView3 = (ImageView)findViewById(R.id.live3);
        liveView4 = (ImageView)findViewById(R.id.live4);
    }

    private void setKeyboardButtons() {
        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);
        AppCompatImageButton pause = findViewById(R.id.pause);

        topBtn.setOnClickListener(this);
        bottomBtn.setOnClickListener(this);
        leftBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);

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
    }

    @Override
    public void onBackPressed() {
        timer.purge();
        timer.cancel();
        startActivity(new Intent(QuizActivity.this, MainActivity.class));
        super.onBackPressed();
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
        if (Objects.equals(mode, "custom")) {
            blockPointsList.clear();
            addCustomBlocks();
        }
        setBlockPointsList();
        if (isLevelMode()) {                       // было: Objects.equals(mode, "level")
            scoreTv.setText("0/" + targetScore);
            levelView.setVisibility(View.VISIBLE);
            levelView.setText(Objects.equals(mode, "custom") ? "ур #" + customLevelId : "ур " + currentLevel);
        } else {
            scoreTv.setText("0");
            levelView.setVisibility(View.GONE);
        }

        score = 0;

        makeNewSnake();
        addPoint();
        moveSnake();
    }

    /**
     * Устанавливает данные блоков уровня. И если что-то пошло не так, возвращает false
     * //todo: тоже рефакторить)
     * @return
     */
    private boolean setLevelData() {
        currentLevel = sharedPreferences.getInt("currentLevel", 1);
        //currentLevel = 2;
        try {
            String blocksString = sharedPreferences.getString("level_" + currentLevel, "");

            if (blocksString.isEmpty()) {
                //если уровни кончились
                Intent resultIntent = new Intent(QuizActivity.this, ResultActivity.class);
                resultIntent.putExtra("mode", "level");
                resultIntent.putExtra("done", true);
                startActivity(resultIntent);
                return false;
            }

            JSONArray blocks = new JSONArray(blocksString);
            for (int i = 0; i < blocks.length(); i++) {
                JSONObject coordinates = blocks.getJSONObject(i);
                blockPointsList.add(new SnakePoints(
                        pointsSize * coordinates.getInt("x"),
                        pointsSize * coordinates.getInt("y")
                ));
            }
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private void setBlockPointsList() {
        //отрисовка рамок
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
                    mediaPlayer.start();
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

                //todo: дааа, рефакторинг реально нужен %)
                //Если режим уровней и набрано нужное количество очков
                if (Objects.equals(mode, "level") && score == targetScore) {
                    timer.purge();
                    timer.cancel();

                    updateRecord();
                    currentLevel++;
                    editor.putInt("currentLevel", currentLevel);
                    editor.commit();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent resultIntent = new Intent(QuizActivity.this, ResultActivity.class);
                            resultIntent.putExtra("mode", "level");
                            resultIntent.putExtra("success", true);
                            startActivity(resultIntent);
                        }
                    });
                    return;
                }

                //Если змейка врезалась
                if (checkGameOver(headPositionX, headPositionY)) {
                    liveCount--;
                    restart = true;
                    if (isVibrateOn) {
                        vibrator.vibrate(100);
                    }
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

                            if (Objects.equals(mode, "level")) {
                                resultIntent.putExtra("mode", "level");
                                resultIntent.putExtra("success", false);
                            } else {
                                resultIntent.putExtra("score", score);
                            }
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
                    drawCircleWithVolume(positionX, positionY, pointsSize, meatColor);

                    //Меняет положение головы. Остальные звенья змеи будут идти следом + рисует голову чуть больше остального))
                    drawCircleWithVolume(
                            snakePointsList.get(0).getPositionX(),
                            snakePointsList.get(0).getPositionY(),
                            (float) (pointsSize * 1.3),
                            snakeColor
                    );

                    //Движение звеньев за головой, позиция 1 - это глова
                    for (int i = 1; i < snakePointsList.size(); i++) {
                        int getTempPositionX = snakePointsList.get(i).getPositionX();
                        int getTempPositionY = snakePointsList.get(i).getPositionY();

                        snakePointsList.get(i).setPositionX(headPositionX);
                        snakePointsList.get(i).setPositionY(headPositionY);
                        canvas.drawCircle(snakePointsList.get(i).getPositionX(), snakePointsList.get(i).getPositionY(), pointsSize, createPointColor(snakeColor));

                        headPositionX = getTempPositionX;
                        headPositionY = getTempPositionY;
                    }

                    //отрисовка блоков
                    for (int i = 0; i < blockPointsList.size(); i++) {
                        canvas.drawCircle(blockPointsList.get(i).getPositionX(), blockPointsList.get(i).getPositionY(), pointsSize, createPointColor(blockColor));
                    }

                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }, 1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void drawCircleWithVolume (float positionX, float positionY, float pointsSize, String color) {
        canvas.drawCircle(positionX - 3, positionY + 3, pointsSize, createPointColor(calculateShadowColor(color, 50)));
        canvas.drawCircle(positionX - 2, positionY + 2, pointsSize, createPointColor(calculateShadowColor(color, 30)));
        canvas.drawCircle(positionX + 1, positionY - 1, pointsSize, createPointColor(calculateLightColor(color)));
        canvas.drawCircle(positionX, positionY, pointsSize, createPointColor(color));
    }

    private String calculateShadowColor(String color, int coeff) {
        int[]rgb = hex2rgb(color);
        rgb[0] = calculateShadow(rgb[0], coeff);
        rgb[1] = calculateShadow(rgb[1], coeff);
        rgb[2] = calculateShadow(rgb[2], coeff);

        return rgb2hex(rgb);
    }

    private String calculateLightColor(String color) {
        int[]rgb = hex2rgb(color);
        int coeff = 70;
        rgb[0] = calculateLight(rgb[0], coeff);
        rgb[1] = calculateLight(rgb[1], coeff);
        rgb[2] = calculateLight(rgb[2], coeff);

        return rgb2hex(rgb);
    }

    private int calculateShadow(int val, int coeff) {
        return (int)(val - (int)(val * coeff / 100));
    }

    private int calculateLight(int val, int coeff) {
        return (int)(val + (int)((255 - val) * coeff / 100));
    }

    private String rgb2hex(int[] rgb) {
        return String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
    }

    private int[] hex2rgb(String colorStr) {
        int r = Integer.valueOf(colorStr.substring(1, 3), 16);
        int g = Integer.valueOf(colorStr.substring(3, 5), 16);
        int b = Integer.valueOf(colorStr.substring(5, 7), 16);
        int[]rgb = new int[3];
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;

        return rgb;
    }

    private void growShake() {
        SnakePoints snakePoints = new SnakePoints(0, 0);

        //Добавляем звено в хвост)
        snakePointsList.add(snakePoints);

        score++;
        updateRecord();

        snakeMovingSpeed = snakeMovingSpeed + 8;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isLevelMode()) {
                    scoreTv.setText(score + "/" + targetScore);
                } else {
                    scoreTv.setText(String.valueOf(score));
                }
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

        return false;
    }

    private Paint createPointColor(String color) {
        if (pointColor == null) {
            pointColor = new Paint();
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true); //гладкое
        }
        pointColor.setColor(Color.parseColor(color));

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