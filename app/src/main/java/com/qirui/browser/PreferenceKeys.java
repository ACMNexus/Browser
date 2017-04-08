/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qirui.browser;

public interface PreferenceKeys {

    String PREF_AUTOFILL_ACTIVE_PROFILE_ID = "autofill_active_profile_id";
    String PREF_DEBUG_MENU = "debug_menu";

    // ----------------------
    // Keys for accessibility_preferences.xml
    // ----------------------
    String PREF_MIN_FONT_SIZE = "min_font_size";
    String PREF_TEXT_SIZE = "text_size";
    String PREF_TEXT_ZOOM = "text_zoom";
    String PREF_DOUBLE_TAP_ZOOM = "double_tap_zoom";
    String PREF_FORCE_USERSCALABLE = "force_userscalable";
    String PREF_INVERTED = "inverted";
    String PREF_INVERTED_CONTRAST = "inverted_contrast";

    // ----------------------
    // Keys for advanced_preferences.xml
    // ----------------------
    String PREF_AUTOFIT_PAGES = "autofit_pages";
    String PREF_BLOCK_POPUP_WINDOWS = "block_popup_windows";
    String PREF_DEFAULT_TEXT_ENCODING = "default_text_encoding";
    String PREF_DEFAULT_ZOOM = "default_zoom";
    String PREF_ENABLE_JAVASCRIPT = "enable_javascript";
    String PREF_LOAD_PAGE = "load_page";
    String PREF_OPEN_IN_BACKGROUND = "open_in_background";
    String PREF_PLUGIN_STATE = "plugin_state";
    String PREF_RESET_DEFAULT_PREFERENCES = "reset_default_preferences";
    String PREF_SEARCH_ENGINE = "search_engine";
    String PREF_SEARCH_ICON = "search_engine_icon";
    String PREF_WEBSITE_SETTINGS = "website_settings";
    String PREF_ALLOW_APP_TABS = "allow_apptabs";

    // ----------------------
    // Keys for debug_preferences.xml
    // ----------------------
    String PREF_ENABLE_HARDWARE_ACCEL = "enable_hardware_accel";
    String PREF_ENABLE_HARDWARE_ACCEL_SKIA = "enable_hardware_accel_skia";
    String PREF_USER_AGENT = "user_agent";

    // ----------------------
    // Keys for general_preferences.xml
    // ----------------------
    String PREF_AUTOFILL_ENABLED = "autofill_enabled";
    String PREF_AUTOFILL_PROFILE = "autofill_profile";
    String PREF_HOMEPAGE = "homepage";
    String PREF_SYNC_WITH_CHROME = "sync_with_chrome";

    // ----------------------
    // Keys for hidden_debug_preferences.xml
    // ----------------------
    String PREF_ENABLE_LIGHT_TOUCH = "enable_light_touch";
    String PREF_ENABLE_NAV_DUMP = "enable_nav_dump";
    String PREF_ENABLE_TRACING = "enable_tracing";
    String PREF_ENABLE_VISUAL_INDICATOR = "enable_visual_indicator";
    String PREF_ENABLE_CPU_UPLOAD_PATH = "enable_cpu_upload_path";
    String PREF_JAVASCRIPT_CONSOLE = "javascript_console";
    String PREF_JS_ENGINE_FLAGS = "js_engine_flags";
    String PREF_NORMAL_LAYOUT = "normal_layout";
    String PREF_SMALL_SCREEN = "small_screen";
    String PREF_WIDE_VIEWPORT = "wide_viewport";
    String PREF_RESET_PRELOGIN = "reset_prelogin";

    // ----------------------
    // Keys for lab_preferences.xml
    // ----------------------
    String PREF_ENABLE_QUICK_CONTROLS = "enable_quick_controls";
    String PREF_FULLSCREEN = "fullscreen";
    String PREF_EXIT_CONFIRM = "exit_confirm";
    String PREF_BLOCK_AD = "block_ad";

    // ----------------------
    // Keys for privacy_security_preferences.xml
    // ----------------------
    String PREF_ACCEPT_COOKIES = "accept_cookies";
    String PREF_ENABLE_GEOLOCATION = "enable_geolocation";
    String PREF_PRIVACY_CLEAR_CACHE = "privacy_clear_cache";
    String PREF_PRIVACY_CLEAR_COOKIES = "privacy_clear_cookies";
    String PREF_PRIVACY_CLEAR_FORM_DATA = "privacy_clear_form_data";
    String PREF_PRIVACY_CLEAR_GEOLOCATION_ACCESS = "privacy_clear_geolocation_access";
    String PREF_PRIVACY_CLEAR_HISTORY = "privacy_clear_history";
    String PREF_PRIVACY_CLEAR_PASSWORDS = "privacy_clear_passwords";
    String PREF_REMEMBER_PASSWORDS = "remember_passwords";
    String PREF_SAVE_FORMDATA = "save_formdata";
    String PREF_SHOW_SECURITY_WARNINGS = "show_security_warnings";

    // ----------------------
    // Keys for bandwidth_preferences.xml
    // ----------------------
    String PREF_DATA_PRELOAD = "preload_when";
    String PREF_LINK_PREFETCH = "link_prefetch_when";
    String PREF_LOAD_IMAGES = "load_images";
    String PREF_VISIT_MODE = "visite_mode";

    // ----------------------
    // Keys for browser recovery
    // ----------------------
    /**
     * The last time recovery was started as System.currentTimeMillis.
     * 0 if not set.
     */
    String KEY_LAST_RECOVERED = "last_recovered";

    /**
     * Key for whether or not the last run was paused.
     */
    String KEY_LAST_RUN_PAUSED = "last_paused";

}
