package com.example.somia.laserdetection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
//CvListener is used for implementing opencv with cell camera

    private static final String TAG = "Activity";
    //    private MyCameraView mOpenCvCameraView; //This is extended with JavaCameraView
    private JavaCameraView mOpenCvCameraView;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;



    //for HSV
    Mat frameH;
    Mat frameV;
    Mat frameS;

    //static int a=1;
    List<Mat> mChannels= new ArrayList<>();
    //bridge between camera and opencv
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//For FullScreen Cameta
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        //Button b = (Button) findViewById(R.id.button);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.CameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


//        //Button For picture capturing
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                a++;
//                String A=Integer.toString(a);
//                String B="Img"+A;
//                mOpenCvCameraView.takePicture(B);
//                NonfreeJNILib NonfreeJNILib=new NonfreeJNILib();
//
//            }
//        });

    }
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {

            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {

            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4); //8U 8bits C4 4 channels for 0-255 range value
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // Rotate mRgba 90 degrees
       // Core.transpose(mRgba, mRgbaT); //Take Transpose Source, Destination
        //Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size());//for resizing
        //Core.flip(mRgbaF, mRgba, 1); //Flip 2D Matrix

        Mat originalFrame=new Mat();
        Mat frame=new Mat();

        inputFrame.rgba().copyTo(originalFrame);
        inputFrame.rgba().copyTo(frame);

        mRgba = inputFrame.rgba();

        Imgproc.cvtColor(frame,frame, Imgproc.COLOR_RGB2HSV);

        mChannels.clear();
        Core.split(frame, mChannels); // Split channels: 0-H, 1-S, 2-V
        frameH = mChannels.get(0);
        frameS = mChannels.get(1);
        frameV = mChannels.get(2);

        // Apply a threshold to each component
        Imgproc.threshold(frameH, frameH, 155, 160, Imgproc.THRESH_BINARY);
        Imgproc.threshold(frameV, frameV, 250, 256, Imgproc.THRESH_BINARY);

        // Perform an AND operation
        Core.bitwise_and(frameH, frameV, frame);

        frameH.release();
        frameV.release();
        frameS.release();

        frame.release();

        return originalFrame; // This function must return
    }


}