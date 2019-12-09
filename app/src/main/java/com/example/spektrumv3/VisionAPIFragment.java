package com.example.spektrumv3;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class VisionAPIFragment extends Fragment {
    private List<ImageLabel> labelList;
    private ProgressBar progressBar;
    private TextView textView;
    static final String TAG = "APIFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vision_api, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        textView = view.findViewById(R.id.infoTextView);

        return view;
    }

    public void updateText(String text){
        textView.setText(text);
        toggleProgressBar(false);
    }

    public void toggleProgressBar(Boolean showProgress) {
        if (showProgress) {
            progressBar.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }

    }

    public void analyzeImageForLabels(Bitmap bitmap){
        labelList = new ArrayList<>();
        labelList.clear();
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
                        //Log.d(TAG, "onSuccess: # of faces: " + firebaseVisionFaces.size());
                        if(firebaseVisionFaces.size() == 1){
                            updateText("There is: " + firebaseVisionFaces.size() + " face.");
                        }else if(firebaseVisionFaces.size() == 0)
                            updateText("There does not appear to be any faces.");
                        else
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
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector();

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
                        String landmarkString = "";
                        if(!landmarkList.isEmpty()) {
                            landmarkString = "I am " + new DecimalFormat("#0.00%").format(landmarkList.get(0).confidence)
                                    + " confident this is the  \n"
                                    + landmarkList.get(0).label;
                        }else{
                            landmarkString = "I do not recognize this landmark.";
                        }

                        updateText(landmarkString);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //task failed to complete
                        updateText("I do not recognize any landmarks.");
                    }
                });

    }

    public void analyzeImageForHotDog(Bitmap bitmap){
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        final FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

        labelList = new ArrayList<>();
        labelList.clear();
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

                boolean isHotDog = false;

                Log.d(TAG, "onSuccess: labelList: " + labelList);

                for(ImageLabel label : labelList){
                    if(label.entityId.equals("Hot dog")){
                        if(label.confidence > .50){
                            isHotDog = true;
                            break;
                        }
                    }
                }

                if(isHotDog){
                    new AlertDialog.Builder(getActivity())
                            .setTitle("YAHOO!")
                            .setMessage("YOU FOUND A HOT DOG! CONGRATULATIONS!")
                            .setNeutralButton("Sweet", null)
                            .show();

                    updateText("It appears to be a hot dog.");
                }else {
                    updateText("Not a hot dog :(");
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed to analyze image for labels.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void analyzeImageForText(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        final Task<FirebaseVisionText> result = detector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    //successfully sound text
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String resultText = firebaseVisionText.getText();

                        if(resultText.equals(""))
                            updateText("I did not find any text.");
                        else
                            updateText("Found the text: \n" + resultText);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    //task failed
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(getActivity(), "Failed to analyze for text", Toast.LENGTH_SHORT).show();
                        updateText("Failed to analyze for text");
                    }
                });
    }


}
