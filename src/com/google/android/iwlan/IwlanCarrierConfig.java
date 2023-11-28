/*
 * Copyright 2023 The Android Open Source Project
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

package com.google.android.iwlan;

import android.content.Context;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.telephony.CarrierConfigManager;

/** Class for handling IWLAN carrier configuration. */
public class IwlanCarrierConfig {

    /**
     * Key for setting the delay in seconds to release the IWLAN connection after a handover to
     * WWAN. Refer to {@link #DEFAULT_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT} for the default
     * value.
     */
    public static final String KEY_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT =
            "iwlan.handover_to_wwan_release_delay_second_int";

    /**
     * Default delay in seconds for releasing the IWLAN connection after a WWAN handover. This is
     * the default value for {@link #KEY_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT}.
     */
    public static final int DEFAULT_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT = 0;

    private static PersistableBundle mHiddenBundle = new PersistableBundle();

    static {
        mHiddenBundle = createHiddenDefaultConfig();
    }

    /**
     * Creates a hidden default configuration.
     *
     * @return a PersistableBundle containing the hidden default configuration
     */
    private static @NonNull PersistableBundle createHiddenDefaultConfig() {
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt(
                KEY_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT,
                DEFAULT_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT);
        return bundle;
    }

    /**
     * Gets a configuration value for a given slot ID and key.
     *
     * @param context the application context
     * @param slotId the slot ID
     * @param key the configuration key
     * @return the configuration value
     */
    // TODO: b/313425985 - Refactor to support explicit type handling (e.g., getIntConfig,
    // getStringConfig). This will enhance type safety by eliminating the need for unchecked casting
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T getConfig(Context context, int slotId, String key) {
        CarrierConfigManager carrierConfigManager =
                context.getSystemService(CarrierConfigManager.class);
        if (carrierConfigManager == null) {
            return getDefaultConfig(key);
        }

        int subId = IwlanHelper.getSubId(context, slotId);
        PersistableBundle bundle = carrierConfigManager.getConfigForSubId(subId, key);
        if (!bundle.containsKey(key)) {
            return getDefaultConfig(key);
        }

        return (T) bundle.get(key);
    }

    /**
     * Gets the default configuration value for a given key.
     *
     * <p>TODO(b/313425985): Refactor to support explicit type handling. This will enhance type
     * safety by eliminating the need for unchecked casting
     *
     * @param key the configuration key
     * @return the default configuration value
     * @throws IllegalArgumentException if the default configuration is null for the given key
     */
    // TODO: b/313425985 - Refactor to support explicit type handling. This will enhance type
    // safety by eliminating the need for unchecked casting
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public static <T> T getDefaultConfig(String key) {
        PersistableBundle bundle = CarrierConfigManager.getDefaultConfig();
        if (bundle.containsKey(key)) {
            return (T) bundle.get(key);
        }

        if (mHiddenBundle.containsKey(key)) {
            return (T) mHiddenBundle.get(key);
        }

        throw new IllegalArgumentException("Default config not found for key: " + key);
    }
}
