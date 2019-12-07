package com.example.spektrumv3;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class VisionAPIFragment extends Fragment {
    List<ImageLabel> labelList;
    static final String TAG = "APIFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.vision_api, container, false);
    }



    public void updateText(String text){
        TextView textView = (TextView) getView().findViewById(R.id.infoTextView);
        textView.setText("hello again");
    }

    public void analyzeImageForLabels(Bitmap bitmap){
        labelList = new ArrayList<>();

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

        labeler.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                for(FirebaseVisionImageLabel label : labels){
                    String text = label.getText();
                    String entityId = label.getEntityId();
                    float confidence = label.getConfidence();
                    ImageLabel newLabel = new ImageLabel(confidence, entityId, text);
                    labelList.add(newLabel);
                }
                String topLabels = "";

                if(labelList.size() >= 5) {
                    for(int i = 0; i < 5; i++){
                        topLabels += labelList.get(i) + "\n";
                    }
                }else{
                    //if there are fewer than 5, we want to fetch all of them
                    for(int i = 0; i < labelList.size(); i++){
                        topLabels += labelList.get(i) + "\n";
                    }
                }
                Log.d(TAG, "onSuccess: top labels" + topLabels);
                updateText(topLabels);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed to analyze image for labels.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void analyzeImageForFaces(Bitmap bitmap){
//        FaceDetector faceDetector = new FaceDetector.Builder(getContext())
//                .setTrackingEnabled(false)
//                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                .build();
//
//        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
//
//        SparseArray<Face> faces = faceDetector.detect(frame);
//        for(int i = 0; i < faces.size(); i++) {
//            Face face = faces.valueAt(i);
//        }
//
//        faceDetector.release();

        //TRIAL OF SOMETHING NEW
        FirebaseVisionFaceDetectorOptions highAccuracy = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector();

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        Log.d(TAG, "onSuccess: # of faces: " + firebaseVisionFaces.size());
                        updateText("There are: " + firebaseVisionFaces.size() + " faces.");
                        //image was scanned successfully
                        for(FirebaseVisionFace face : firebaseVisionFaces){
                            Rect bounds = face.getBoundingBox();
                            float rotY = face.getHeadEulerAngleY();
                            float rotZ = face.getHeadEulerAngleZ();

                            FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                            if(leftEye != null){
                                FirebaseVisionPoint leftEyePosition = leftEye.getPosition();
                            }

                            FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                            if(rightEye != null){
                                FirebaseVisionPoint rightEyePosition = rightEye.getPosition();
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void analyzeImageForLandmark(Bitmap bitmap){
        final List<LandmarkLabel> landmarkList = new ArrayList<>();
        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15) //we only want one landmark??
                        .build();

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector(options);

        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        //task completed successfully
                        for(FirebaseVisionCloudLandmark landmark : firebaseVisionCloudLandmarks){
                            String name = landmark.getLandmark();
                            String id = landmark.getEntityId();
                            float confidence = landmark.getConfidence();
                            LandmarkLabel label = new LandmarkLabel(confidence, name, id);
                            landmarkList.add(label);
                        }
                        Log.d(TAG, "onSuccess: landmarks: " + landmarkList);
                        //System.out.println(landmarkList);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //task failed to complete
                        Log.d(TAG, "onFailure: " + "failed to find a landmark");
                    }
                });

    }



}
