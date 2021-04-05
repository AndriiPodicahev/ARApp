package com.grotstek.arapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class Model {
    private String modelPath;
    private int iconPath;
    private String name;
    private String description;
    private float pitchAngle;
    private float labelHeight;

    public Model(String modelPath, int iconPath, String name, String description, float pitchAngle, float lableHeight) {
        this.modelPath = modelPath;
        this.iconPath = iconPath;
        this.name = name;
        this.description = description;
        this.pitchAngle = pitchAngle;
        this.labelHeight = lableHeight;
    }

    public String getModelPath() {
        return modelPath;
    }

    public int getIconPath() {
        return iconPath;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getPitchAngle() {
        return pitchAngle;
    }

    public void modelToScreen(ArFragment arFragment, Anchor anchor, final Activity activity){
        ModelRenderable.builder().setSource(activity, Uri.parse(modelPath)).build()
                .thenAccept(modelRenderable -> {
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(modelRenderable);

                    arFragment.getArSceneView().getScene().addChild(anchorNode);
                    transformableNode.select();
                    transformableNode.getScaleController().setEnabled(false);

                    makeViewNode(transformableNode, activity, arFragment, anchorNode);

                })
                .exceptionally(throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(throwable.getMessage()).show();
                    return null;
                });


    }

    public void makeViewNode(TransformableNode transformableNode, Activity activity, ArFragment arFragment, AnchorNode anchorNode){
        transformableNode.setOnTapListener((hitTestResult, motionEvent) -> {
            Node node = hitTestResult.getNode();
            ViewRenderable.builder()
                    .setView(activity, R.layout.model_menu_layout)
                    .build()
                    .thenAccept(renderable -> {

                        renderable.setShadowCaster(false);
                        renderable.setShadowReceiver(false);
                        TransformableNode viewNode = new TransformableNode(arFragment.getTransformationSystem());
                        viewNode.setParent(anchorNode);
                        viewNode.setRenderable(renderable);
                        viewNode.getScaleController().setEnabled(false);
                        viewNode.getRotationController().setEnabled(false);
                        viewNode.getTranslationController().setEnabled(false);



                        Handler handler = new Handler();

                        Runnable rotator = new Runnable() {
                            @Override
                            public void run() {
                                if(viewNode != null) {
                                    Vector3 cameraPosition = viewNode.getScene().getCamera().getWorldPosition();
                                    Vector3 cardPosition = viewNode.getWorldPosition();
                                    Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
                                    Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
                                    viewNode.setWorldRotation(lookRotation);

                                    handler.postDelayed(this, 33);
                                }
                            }
                        };

                        handler.post(rotator);
                        viewNode.setLocalPosition(new Vector3(0,labelHeight,0));


                        TextView name = renderable.getView().findViewById(R.id.menuModelName);
                        name.setText(this.name);
                        TextView description = renderable.getView().findViewById(R.id.menuModelDescription);
                        description.setText(this.description);

                        Button button = renderable.getView().findViewById(R.id.menuModelDeleteButton);
                        button.setOnClickListener((View v) -> {
                            handler.removeCallbacks(rotator);
                            removeFromScene(arFragment, anchorNode, viewNode, activity, false);
                            removeFromScene(arFragment, anchorNode, node, activity, true);
                        });

                        transformableNode.setOnTapListener((hitTestResult1, motionEvent1) -> {
                            handler.removeCallbacks(rotator);
                            removeFromScene(arFragment, anchorNode, viewNode, activity, false);
                            makeViewNode(transformableNode, activity, arFragment, anchorNode);
                        });
                    });

        });
    }


    public void removeFromScene(ArFragment arFragment, AnchorNode anchorNode, Node node, final Activity activity, boolean buildToast){
        if (node != null) {
            if(buildToast) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setMessage(R.string.deleteAlertMessage)
                        .setTitle(R.string.deleteAlertTitle);
                dialogBuilder.setPositiveButton(R.string.deleteAlertOk, ((DialogInterface dialog, int which) -> {
                    anchorNode.removeChild(node);
                    arFragment.getArSceneView().getScene().removeChild(node);
                    Toast.makeText(activity, "Об’єкт успішно видалено!", Toast.LENGTH_LONG).show();
                }));
                dialogBuilder.setNegativeButton(R.string.deleteAlertCancel, ((DialogInterface dialog, int which) -> {
                    Toast.makeText(activity, "Об’єкт залишено!", Toast.LENGTH_LONG).show();
                }));
                dialogBuilder.create().show();

            }else{
                anchorNode.removeChild(node);
                arFragment.getArSceneView().getScene().removeChild(node);
            }
        }
    }
}
