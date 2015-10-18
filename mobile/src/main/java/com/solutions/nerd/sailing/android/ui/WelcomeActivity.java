package com.solutions.nerd.sailing.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
/**
 * Created by cberman on 12/16/2014.
 */
public class WelcomeActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);


        findViewById(R.id.button_accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.markTosAccepted(WelcomeActivity.this);

                // Need to be able to account for wide version
                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.button_decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
    @Override
    public void onResume() {
        super.onResume();


        // Shows the debug warning, if this is a debug build and the warning has not been shown yet
        if (Config.IS_DOGFOOD_BUILD && !PrefUtils.wasDebugWarningShown(this)) {
            new AlertDialog.Builder(this)

                    .setTitle(  Config.DOGFOOD_BUILD_WARNING_TITLE)
                    .setMessage(Config.DOGFOOD_BUILD_WARNING_TEXT)
                    .setPositiveButton(android.R.string.ok, null).show();
            PrefUtils.markDebugWarningShown(this);
        }
    }
}
