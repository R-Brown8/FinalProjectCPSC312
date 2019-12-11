package com.example.spektrumv3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class colorActivity extends AppCompatActivity {
    String TAG = "ColorActivity";
    private ImageView selectedImage;
    private ColorGridFragment colorGridFragment;
    private List<ImageLabel> labelList;
    private ProgressBar progressBar;
    private TextView textView;
    private Uri myURI;
    private TextView color1GridFragment;
    private TextView color2GridFragment;
    private TextView color3GridFragment;
    private TextView color4GridFragment;
    private TextView color5GridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
        initViews();

        //we want a back button on the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Get the Intent that started this activity and extract the URI string
        Intent intent = getIntent();
        String uriString = intent.getStringExtra("selectedImageURI");

        myURI = Uri.parse(uriString);
        selectedImage.setImageURI(myURI);

        //show the fragment that will display our information
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .show(colorGridFragment).commit();
        Thread colorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                colorGridFragment.paintTextBackground(uriToBitmap(myURI));
            }
        });
        colorThread.start();

        //we want to use a spinner and add the options to it
        String[] spinnerOptions = {"Colors", "Labels", "Landmarks", "Faces", "Text", "Hot Dog?"};
        final Spinner spinner = findViewById(R.id.spinnerOptions);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                spinnerOptions);
        spinner.setAdapter(adapter);

        Thread thread = new Thread(new Runnable() {
                public void run() {
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, final long id) {
                            if (id == 0) {
                                setTextVisible(false);
                            } else if (id == 1) {
                                toggleProgressBar(true);
                                setTextVisible(false);
                                analyzeImageForLabels(uriToBitmap(myURI));
                                setTextVisible(true);
                            } else if (id == 2) {
                                toggleProgressBar(true);
                                setTextVisible(false);
                                analyzeImageForLandmark(uriToBitmap(myURI));
                                setTextVisible(true);
                            } else if (id == 3) {
                                setTextVisible(false);
                                toggleProgressBar(true);
                                analyzeImageForFaces(uriToBitmap(myURI));
                                setTextVisible(true);
                            } else if (id == 4) {
                                setTextVisible(false);
                                toggleProgressBar(true);
                                analyzeImageForText(uriToBitmap(myURI));
                                setTextVisible(true);
                            } else if (id == 5) {
                                setTextVisible(false);
                                toggleProgressBar(true);
                                analyzeImageForHotDog(uriToBitmap(myURI));
                                setTextVisible(true);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            FragmentManager fm = getSupportFragmentManager();
                            fm.beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                                    .show(colorGridFragment).commit();
                            setTextVisible(false);
                        }
                    });
                }
        });
        thread.start();
    }//end onCreate

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initViews() {
        colorGridFragment  = (ColorGridFragment) getSupportFragmentManager().findFragmentById(R.id.colorGridFragment);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        selectedImage = findViewById(R.id.analyzeImageView);
        color1GridFragment = findViewById(R.id.fragColorTextView1);
        color2GridFragment = findViewById(R.id.fragColorTextView2);
        color3GridFragment = findViewById(R.id.fragColorTextView3);
        color4GridFragment = findViewById(R.id.fragColorTextView4);
        color5GridFragment = findViewById(R.id.fragColorTextView5);
    }

    private Bitmap uriToBitmap(Uri uri) {
        Bitmap returnValue = null;
        try{
            returnValue = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (Exception e) { }
        return returnValue;
    }

    public void updateText(String text){
        textView.setText(text);
        toggleProgressBar(false);
    }

    public void toggleProgressBar(Boolean showProgress) {
        if (showProgress) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setTextVisible(boolean visible){
        if(visible){
            color1GridFragment.setVisibility(View.GONE);
            color2GridFragment.setVisibility(View.GONE);
            color3GridFragment.setVisibility(View.GONE);
            color4GridFragment.setVisibility(View.GONE);
            color5GridFragment.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        }else{
            color1GridFragment.setVisibility(View.VISIBLE);
            color2GridFragment.setVisibility(View.VISIBLE);
            color3GridFragment.setVisibility(View.VISIBLE);
            color4GridFragment.setVisibility(View.VISIBLE);
            color5GridFragment.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
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

            }
        });

    }

    public void analyzeImageForFaces(Bitmap bitmap){
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
                        double latitude = 0.0;
                        double longitude = 0.0;
                        for(FirebaseVisionCloudLandmark landmark : firebaseVisionCloudLandmarks){
                            String name = landmark.getLandmark();
                            String id = landmark.getEntityId();
                            float confidence = landmark.getConfidence();

                            for(FirebaseVisionLatLng loc : landmark.getLocations()){
                                latitude = loc.getLatitude();
                                longitude = loc.getLongitude();
                            }


                            LandmarkLabel label;
                            label = new LandmarkLabel(confidence, name, id, latitude, longitude);
                            landmarkList.add(label);
                        }


                        String landmarkString;
                        if(!landmarkList.isEmpty()) {
                            landmarkString = "I am " + new DecimalFormat("#0.00%").format(landmarkList.get(0).confidence)
                                    + " confident this is the  \n"
                                    + landmarkList.get(0).label;
                            if(landmarkList.get(0).latitude != 0.0){
                                landmarkString += " \n (" + landmarkList.get(0).latitude + ", " + landmarkList.get(0).longitude + ")";
                            }

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
                        if(label.confidence > .33){
                            isHotDog = true;
                            break;
                        }
                    }
                }

                if(isHotDog){
                    new AlertDialog.Builder(colorActivity.this)
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
                Toast.makeText(colorActivity.this, "Failed to analyze image for labels.", Toast.LENGTH_SHORT).show();
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

