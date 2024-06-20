package com.binturong.game_editor;

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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EditorActivity extends HeaderActivity implements SurfaceHolder.Callback {

    private final List<SnakePoints> blockPointsList = new ArrayList<>();
    //Вспомогательный массив с блоками 2 * 2, чисто для редактора
    private final List<SnakePoints> bigBlockPointsList = new ArrayList<>();

    Button saveLevel, publicLevel, clearLevel;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private static final int surfaceSizeInPoints = 45;
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
        publicLevel = findViewById(R.id.public_level);
        clearLevel = findViewById(R.id.clear_level);

        setHeaderButtons();
        saveLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            URL url = new URL ("https://test-constructor.ru/api/v.0/level/create");
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-Type", "application/json");
                            con.setRequestProperty("Accept", "application/json");
                            con.setDoOutput(true);


                            JSONObject block = new JSONObject();
                            block.put("x", 5);
                            block.put("y", 6);
                            JSONArray blocks = new JSONArray();
                            blocks.put(block);

                            JSONObject params = new JSONObject();
                            params.put("blocks", blocks);
                            params.put("user_token", 5);

                            String jsonInputString = params.toString();

                            Log.d("log", "log1");

                            try(OutputStream os = con.getOutputStream()) {
                                byte[] input = jsonInputString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }
                            Log.d("log", "log2");

                            try(BufferedReader br = new BufferedReader(
                                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                                StringBuilder response = new StringBuilder();
                                String responseLine = null;
                                while ((responseLine = br.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                                Log.d("resp", response.toString());
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            Log.d("log", "log3");

                            con.disconnect();

                        } catch (Exception e) {
                            e.printStackTrace();
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
                finish();
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