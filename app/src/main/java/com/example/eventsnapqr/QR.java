package com.example.eventsnapqr;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

// import androidmads.library.qrgenearator.QRGContents;
// import androidmads.library.qrgenearator.QRGEncoder;

public class QR {

    private Bitmap bitmap;
    private String link;
    public QR(Bitmap bitmap, String link){
        this.link = link;
        this.bitmap = bitmap;
    }
    public QR(String link){
        this.link = link;
        QRGEncoder qrgEncoder = new QRGEncoder(link, null, QRGContents.Type.TEXT, 5);
        bitmap = qrgEncoder.getBitmap();
    }

    public String getLink() {
        return link;
    }
}
