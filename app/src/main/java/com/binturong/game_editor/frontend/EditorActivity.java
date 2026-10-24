package com.binturong.game_editor.frontend;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;

import com.binturong.game_editor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends HeaderActivity implements SurfaceHolder.Callback {

    private int levelId = -1;
    private final List<SnakePoints> blockPointsList = new ArrayList<>();
    //Вспомогательный массив с блоками 2 * 2, чисто для редактора
    private List<SnakePoints> bigBlockPointsList = new ArrayList<>();

    Button saveLevel, publishLevel, clearLevel;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private static final int surfaceSizeInPoints = 43;
    private int surfaceSize;
    private static int pointsSize;
    private static final int defaultTailPints = 3;

    private static final int snakeColor = Color.GREEN;
    private static final int blockColor = Color.parseColor("#3DBFF2");
    private static final int meatColor = Color.YELLOW;
    private Canvas canvas = null;

    //Цвет еды/одного звена змеи
    private Paint pointColor = null;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        saveLevel = findViewById(R.id.save_level);
        publishLevel = findViewById(R.id.publish_level);
        clearLevel = findViewById(R.id.clear_level);

        setHeaderButtons();

        clearLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //очищаем список больших блоков
                bigBlockPointsList = new ArrayList<>();

                //очищаем все и заново рисуем только рамку
                canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

                //отрисовка рамки
                for (int i = 0; i < blockPointsList.size(); i++) {
                    canvas.drawCircle(blockPointsList.get(i).getPositionX(), blockPointsList.get(i).getPositionY(), pointsSize, createPointColor("block"));
                }

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        });
        saveLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            String levelData = prepareLevelData("draft");
                            String response;
                            if (levelId == -1) {
                                //если уровень новый, создаем
                                response = sendPOST("level/create", levelData);
                            } else {
                                //иначе редактируем старый
                                response = sendPOST("level/edit", levelData);
                            }
                            Log.d("result", response);

                            //todo: тоже рефакторинг уже просится)
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                            if (isResponseOk(response)) {
                                builder.setTitle("Уровень сохранен в черновики");
                                builder.setMessage("Посмотреть свои уровни вы можете в профиле");
                                builder.setCancelable(true);
                            } else {
                                builder.setTitle("Что-то пошло не так");
                                builder.setMessage("Пожалуйста проверьте подключение к интернету и попробуйте позже");
                                builder.setCancelable(true);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    builder.show();
                                }
                            });
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
            }
        });

        publishLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            String levelData = prepareLevelData("publish");
                            String response;
                            if (levelId == -1) {
                                //если уровень новый, создаем
                                response = sendPOST("level/create", levelData);
                            } else {
                                //иначе редактируем старый
                                response = sendPOST("level/edit", levelData);
                            }
                            Log.d("result", response);

                            //todo: тоже рефакторинг уже просится)
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
                            if (isResponseOk(response)) {
                                builder.setTitle("Уровень отправлен на модерацию");
                                builder.setMessage("Модерация занимает до 2ух суток. Посмотреть свои уровни и их статус вы можете в профиле");
                                builder.setCancelable(true);
                            } else {
                                builder.setTitle("Что-то пошло не так");
                                builder.setMessage("Пожалуйста проверьте подключение к интернету и попробуйте позже");
                                builder.setCancelable(true);
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    builder.show();
                                }
                            });
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                thread.start();
            }
        });

        setAddBanner();
        setSurfaceView();

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    //чтобы все блоки рисовались ровно по сетке, а не точно в рандомной точке нажатия
                    int blockSize = 4 * pointsSize;
                    int xPosition = x / blockSize;
                    int yPosition = y / blockSize;
                    xPosition = xPosition * blockSize + pointsSize;
                    yPosition = yPosition * blockSize + pointsSize;

                    //если блок с такими координатами уже существует, убираем его
                    boolean isBlockExist = false;
                    for (int i = 0; i < bigBlockPointsList.size(); i++) {
                        if (bigBlockPointsList.get(i).getPositionX() == xPosition &&
                                bigBlockPointsList.get(i).getPositionY() == yPosition) {
                            isBlockExist = true;
                            bigBlockPointsList.remove(i);
                            break;
                        }
                    }
                    //если блока нет, добавили
                    if (!isBlockExist) {
                        bigBlockPointsList.add(new SnakePoints(xPosition, yPosition));
                    }

                    drawBlocks();
                }

                return onTouchEvent(event);
            }
        });
    }

    private void setSurfaceView() {
        surfaceView = findViewById(R.id.surfaceViewEditor);
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
                showUpdateDialog(EditorActivity.this);
            }
        });

        giftReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExtraLiveDialog(EditorActivity.this);
            }
        });

        homeHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(EditorActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        settingsHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(EditorActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(EditorActivity.this, MainActivity.class));
        super.onBackPressed();
    }

    /**
     * Подготавливает данные уровня для сохранения
     * todo: рефакторинг конечно нужен)
     * @return
     * @throws JSONException
     */
    public String prepareLevelData(String type) throws JSONException {
        JSONArray blocks = new JSONArray();
        for (SnakePoints block : bigBlockPointsList) {
            JSONObject jsonBlock = new JSONObject();
            jsonBlock.put("x", block.getPositionX() / pointsSize);
            jsonBlock.put("y", block.getPositionY() / pointsSize);
            blocks.put(jsonBlock);

            jsonBlock = new JSONObject();
            jsonBlock.put("x", block.getPositionX() / pointsSize + 2);
            jsonBlock.put("y", block.getPositionY() / pointsSize);
            blocks.put(jsonBlock);

            jsonBlock = new JSONObject();
            jsonBlock.put("x", block.getPositionX() / pointsSize);
            jsonBlock.put("y", block.getPositionY() / pointsSize + 2);
            blocks.put(jsonBlock);

            jsonBlock = new JSONObject();
            jsonBlock.put("x", block.getPositionX() / pointsSize + 2);
            jsonBlock.put("y", block.getPositionY() / pointsSize + 2);
            blocks.put(jsonBlock);
        }

        JSONObject params = new JSONObject();
        if (levelId != -1) {
            params.put("level_id", levelId);
        }
        params.put("blocks", blocks);
        params.put("user_token", sharedPreferences.getString("userToken", "0"));
        params.put("type", type);

        return params.toString();
    }

    private void drawBlocks() {
        //отрисовка блоков
        canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

        //отрисовка рамки
        for (int i = 0; i < blockPointsList.size(); i++) {
            canvas.drawCircle(blockPointsList.get(i).getPositionX(), blockPointsList.get(i).getPositionY(), pointsSize, createPointColor("block"));
        }

        //отрисовка больших блоков
        for (int i = 0; i < bigBlockPointsList.size(); i++) {
            //большой блок - квадрат из 4 мелких блоков
            canvas.drawCircle(
                    bigBlockPointsList.get(i).getPositionX(),
                    bigBlockPointsList.get(i).getPositionY(),
                    pointsSize,
                    createPointColor("block")
            );
            canvas.drawCircle(
                    bigBlockPointsList.get(i).getPositionX() + pointsSize * 2,
                    bigBlockPointsList.get(i).getPositionY(),
                    pointsSize,
                    createPointColor("block")
            );
            canvas.drawCircle(
                    bigBlockPointsList.get(i).getPositionX(),
                    bigBlockPointsList.get(i).getPositionY() + pointsSize * 2,
                    pointsSize,
                    createPointColor("block")
            );
            canvas.drawCircle(
                    bigBlockPointsList.get(i).getPositionX() + pointsSize * 2,
                    bigBlockPointsList.get(i).getPositionY() + pointsSize * 2,
                    pointsSize,
                    createPointColor("block")
            );
        }
        surfaceHolder.unlockCanvasAndPost(canvas);
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
        surfaceSize = surfaceView.getWidth(); //квадрат по ширине экрана
        pointsSize = surfaceSize / surfaceSizeInPoints;
        surfaceView.getHolder().setFixedSize(surfaceSize, surfaceSize);

        setBlockPointsList();

        canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR);

        //отрисовка блоков
        for (int i = 0; i < blockPointsList.size(); i++) {
            canvas.drawCircle(
                    blockPointsList.get(i).getPositionX(),
                    blockPointsList.get(i).getPositionY(),
                    pointsSize,
                    createPointColor("block")
            );
        }

        surfaceHolder.unlockCanvasAndPost(canvas);

//        snakePointsList.clear();
//        setBlockPointsList();
//
//        int startPositionX = pointsSize * defaultTailPints;
//
//        for (int i = 0; i < defaultTailPints; i++) {
//            SnakePoints snakePoints = new SnakePoints(startPositionX, pointsSize);
//            snakePointsList.add(snakePoints);
//
//            startPositionX = startPositionX - (pointsSize * 2);
//        }
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

    private void setBigBlockPointsList() {
        blockPointsList.add(new SnakePoints(pointsSize * 3, pointsSize * 5));
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
}