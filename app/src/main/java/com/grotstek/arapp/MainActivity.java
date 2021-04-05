package com.grotstek.arapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private static final double MIN_OGL = 3.0;
    private ArFragment arFragment;
    private ModelRenderable modelRenderable;
    private Button menuButton;
    private boolean isMenuShowed;
    private LinearLayout menuLayout;
    private ArrayList<Model> chairs;
    private ArrayList<Model> tables;
    private ArrayList<Model> cupboards;
    private ArrayList<Model> lamps;
    private ArrayList<Model> plants;
    private ArrayList<Model> sofas;

    private Model selectedModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isDeviceSupported(this))
        {
            return;
        }

        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arUxFragment);


        chairs = new ArrayList<>();
        tables = new ArrayList<>();
        cupboards = new ArrayList<>();
        lamps = new ArrayList<>();
        plants = new ArrayList<>();
        sofas = new ArrayList<>();
        sofas = new ArrayList<>();

        initModels();
        initButtons();
        selectedModel = chairs.get(0);

        Button chairsButton = findViewById(R.id.chairsButton);
        Button tablesButton = findViewById(R.id.tablesButton);

        LinearLayout chairsMenu = findViewById(R.id.chairsMenu);
        LinearLayout tablesMenu = findViewById(R.id.tablesMenu);

        chairsButton.setOnClickListener((View v) -> {
            chairsMenu.setVisibility(View.VISIBLE);
            tablesMenu.setVisibility(View.GONE);
        });

        tablesButton.setOnClickListener((View v) -> {
            chairsMenu.setVisibility(View.GONE);
            tablesMenu.setVisibility(View.VISIBLE);
        });

        arFragment.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();

            selectedModel.modelToScreen(arFragment, anchor, this);
        });

        menuLayout = findViewById(R.id.menuLayout);
        menuButton = findViewById(R.id.menuButton);

        isMenuShowed = false;
        menuButton.setOnClickListener((View v) -> {
            if(!isMenuShowed)
            {
                isMenuShowed = true;
                menuLayout.setVisibility(View.VISIBLE);
                menuButton.setBackgroundResource(R.drawable.arrow_up);

            }else{
                isMenuShowed = false;
                menuLayout.setVisibility(View.GONE);
                menuButton.setBackgroundResource(R.drawable.arrow_down);
            }
        });
    }


    public void initModels()
    {
        chairs.add(new Model("CHAHIN_WOODEN_CHAIR.sfb", R.drawable.chair1_icon, "Стілець", "Простий дерев'яний стілець", 0, 1.2f));
        chairs.add(new Model("chairs/Arm_modern_chair_1_corona.sfb", R.drawable.chair2_icon, "Крісло", "Сучасне крісло", 0, 1.2f));
        tables.add(new Model("tables/10240_Office_Desk_v3_max2011.sfb", R.drawable.chair1_icon, "Table", "Table", 0, 1.2f));
        tables.add(new Model("tables/obj_mesa.sfb", R.drawable.chair1_icon, "Table2", "Table2", 0, 1.2f));

    }

    public void initButtons()
    {
        LinearLayout chairsMenu = findViewById(R.id.chairsMenu);
        LinearLayout tablesMenu = findViewById(R.id.tablesMenu);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for(Model model : chairs)
        {
            Button button = new Button(this);
            //button.setWidth(96);
            //button.setHeight(96);
            button.setBackgroundResource(model.getIconPath());;
            chairsMenu.addView(button, btnParams);


            button.setOnClickListener((View v) ->{
                selectedModel = model;
            });
        }

        for(Model model : tables)
        {
            Button button = new Button(this);
            button.setText(model.getName());
            tablesMenu.addView(button, btnParams);

            button.setOnClickListener((View v) ->{
                selectedModel = model;
            });
        }

    }


    public static boolean isDeviceSupported(final Activity activity){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1){
            Toast.makeText(activity, "This application requires Android 8.1 or late", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }

        String OGLVersion = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().getGlEsVersion();

        if(Double.parseDouble(OGLVersion) < MIN_OGL){
            Toast.makeText(activity, "This application requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        return true;
    }
}
