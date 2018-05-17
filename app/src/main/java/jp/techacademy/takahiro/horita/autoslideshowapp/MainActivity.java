package jp.techacademy.takahiro.horita.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Timer mTimer;
    Handler mHandler = new Handler();
    int buttonCheck = 0; //ボタンの表示切り替え用

    ArrayList<Uri> arrayList = new ArrayList<>();
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        final Button forward_button = findViewById(R.id.forward_button);
        forward_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //パーミッションチェック
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    ImageView imageView = findViewById(R.id.imageView);
                    if(arrayList.size()-1 > i){
                        //進むボタンで1つ先の画像を表示
                        i++;
                        imageView.setImageURI(arrayList.get(i));
                    }else{
                        //最後の画像の表示時に進むボタンをタップすると最初の画像を表示
                        i = 0;
                        imageView.setImageURI(arrayList.get(i));
                    }
                }
            }
        });

        final Button buck_button = findViewById(R.id.buck_button);
        buck_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //パーミッションチェック
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    ImageView imageView = findViewById(R.id.imageView);
                    if (i > 0) {
                        //戻るボタンで1つ前の画像を表示
                        i--;
                        imageView.setImageURI(arrayList.get(i));
                    } else {
                        //最初の画像の表示時に戻るボタンをタップすると最後の画像を表示
                        i = arrayList.size() - 1;
                        imageView.setImageURI(arrayList.get(i));
                    }
                }
            }
        });

        final Button playstop_button = findViewById(R.id.playstop_button);
        playstop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //パーミッションチェック
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (buttonCheck == 0) {
                        //ボタンの表示を"再生"から"停止"に変更
                        playstop_button.setText("停止");
                        buttonCheck = 1;

                        //自動送りの間は戻るボタンはタップ不可
                        //buck_button.setClickable(false);
                        buck_button.setEnabled(false);
                        //自動送りの間は進むボタンはタップ不可
                        //forward_button.setClickable(false);
                        forward_button.setEnabled(false);

                        // タイマーの作成
                        mTimer = new Timer();
                        // タイマーの始動
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ImageView imageView = findViewById(R.id.imageView);
                                        if (arrayList.size() - 1 > i) {
                                            i++;
                                            imageView.setImageURI(arrayList.get(i));
                                        } else {
                                            i = 0;
                                            imageView.setImageURI(arrayList.get(i));
                                        }
                                    }
                                });
                            }
                        }, 2000, 2000); // 最初に始動させるまで2000ミリ秒、ループの間隔を2000ミリ秒に設定
                    } else {
                        //ボタンの表示を"停止"から"再生"に変更
                        playstop_button.setText("再生");
                        //ボタンの表示確認用フラグ
                        buttonCheck = 0;

                        //戻るボタンをタップ可に変更
                        //buck_button.setClickable(true);
                        buck_button.setEnabled(true);

                        //進むボタンをタップ可に変更
                        //forward_button.setClickable(true);
                        forward_button.setEnabled(true);

                        //自動送り停止
                        mTimer.cancel();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();

        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                arrayList.add(imageUri);

                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageURI(arrayList.get(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}