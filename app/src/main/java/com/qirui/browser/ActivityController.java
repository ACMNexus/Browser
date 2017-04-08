package com.qirui.browser;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

public interface ActivityController {

    String INCOGNITO_URI = "browser:incognito";

    // public message ids
    int LOAD_URL = 1001;
    int STOP_LOAD = 1002;

    // Message Ids
    int EMPTY_MENU = -1;
    int OPEN_BOOKMARKS = 201;
    int FOCUS_NODE_HREF = 102;
    int RELEASE_WAKELOCK = 107;
    int UPDATE_BOOKMARK_THUMBNAIL = 108;

    // activity requestCode
    int COMBO_VIEW = 1;
    int VOICE_RESULT = 6;
    int FILE_SELECTED = 4;
    int PREFERENCES_PAGE = 3;
    int WAKELOCK_TIMEOUT = 5 * 60 * 1000; // 5 minutes

    // As the ids are dynamically created, we can't guarantee that they will
    // be in sequence, so this static array maps ids to a window number.
    int[] WINDOW_SHORTCUT_ID_ARRAY = {
                                        R.id.window_one_menu_id, R.id.window_two_menu_id,
                                        R.id.window_three_menu_id, R.id.window_four_menu_id,
                                        R.id.window_five_menu_id, R.id.window_six_menu_id,
                                        R.id.window_seven_menu_id, R.id.window_eight_menu_id
                                     };

    void start(Intent intent);

    void onSaveInstanceState(Bundle outState);

    void handleNewIntent(Intent intent);

    void onResume();

    void onPause();

    void onDestroy();

    void onConfgurationChanged(Configuration newConfig);

    void onLowMemory();

    boolean onOptionsItemSelected(MenuItem item);

    void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo, float touchX, float touchY);

    boolean onContextItemSelected(int id);

    boolean onKeyDown(int keyCode, KeyEvent event);

    boolean onKeyLongPress(int keyCode, KeyEvent event);

    boolean onKeyUp(int keyCode, KeyEvent event);

    void onActionModeStarted(ActionMode mode);

    void onActionModeFinished(ActionMode mode);

    void onActivityResult(int requestCode, int resultCode, Intent intent);

    boolean onSearchRequested();

    boolean dispatchKeyEvent(KeyEvent event);

    boolean dispatchKeyShortcutEvent(KeyEvent event);

    boolean dispatchTouchEvent(MotionEvent ev);

    boolean dispatchTrackballEvent(MotionEvent ev);

    boolean dispatchGenericMotionEvent(MotionEvent ev);
}
