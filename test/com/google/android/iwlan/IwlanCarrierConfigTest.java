/*
 * Copyright (C) 2023 The Android Open Source Project
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

import static com.android.dx.mockito.inline.extended.ExtendedMockito.mockitoSession;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;

public class IwlanCarrierConfigTest {
    private static final int DEFAULT_SUB_ID = 0;
    private static final int DEFAULT_SLOT_ID = 1;

    private static final String KEY_CONFIG_IN_SUB = "iwlan.key_config_in_sub";
    private static final String KEY_CONFIG_IN_DEFAULT = "iwlan.key_config_in_default";

    private static final int VALUE_CONFIG_IN_SUB = 10;
    private static final int VALUE_CONFIG_IN_DEFAULT = 20;

    @Mock private Context mMockContext;
    @Mock private CarrierConfigManager mMockCarrierConfigManager;
    @Mock private SubscriptionManager mMockSubscriptionManager;
    @Mock private SubscriptionInfo mMockSubscriptionInfo;

    private PersistableBundle mBundleForSub;
    private PersistableBundle mBundleForDefault;

    MockitoSession mStaticMockSession;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mStaticMockSession =
                mockitoSession()
                        .mockStatic(SubscriptionManager.class)
                        .mockStatic(CarrierConfigManager.class)
                        .startMocking();

        when(mMockContext.getSystemService(CarrierConfigManager.class))
                .thenReturn(mMockCarrierConfigManager);
        when(mMockContext.getSystemService(SubscriptionManager.class))
                .thenReturn(mMockSubscriptionManager);

        when(mMockSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(anyInt()))
                .thenReturn(mMockSubscriptionInfo);
        when(mMockSubscriptionInfo.getSubscriptionId()).thenReturn(DEFAULT_SUB_ID);

        mBundleForSub = new PersistableBundle();
        mBundleForSub.putInt(KEY_CONFIG_IN_SUB, VALUE_CONFIG_IN_SUB);
        lenient()
                .when(mMockCarrierConfigManager.getConfigForSubId(anyInt(), anyString()))
                .thenReturn(mBundleForSub);

        mBundleForDefault = new PersistableBundle();
        mBundleForDefault.putInt(KEY_CONFIG_IN_DEFAULT, VALUE_CONFIG_IN_DEFAULT);
        lenient().when(CarrierConfigManager.getDefaultConfig()).thenReturn(mBundleForDefault);
    }

    @After
    public void cleanUp() throws Exception {
        mStaticMockSession.finishMocking();
    }

    @Test
    public void testGetConfig_ValidRetrieval() {
        int result = IwlanCarrierConfig.getConfig(mMockContext, DEFAULT_SLOT_ID, KEY_CONFIG_IN_SUB);
        assertEquals(VALUE_CONFIG_IN_SUB, result);
    }

    @Test
    public void testGetConfig_KeyNotFound() {
        // Default value from getDefaultConfig
        int result =
                IwlanCarrierConfig.getConfig(mMockContext, DEFAULT_SLOT_ID, KEY_CONFIG_IN_DEFAULT);
        assertEquals(VALUE_CONFIG_IN_DEFAULT, result);
    }

    @Test
    public void testGetDefaultConfig_KeyFound() {
        int result = IwlanCarrierConfig.getDefaultConfig(KEY_CONFIG_IN_DEFAULT);
        assertEquals(VALUE_CONFIG_IN_DEFAULT, result);
    }

    @Test
    public void testGetDefaultConfig_KeyInHiddenDefault() {
        int result =
                IwlanCarrierConfig.getDefaultConfig(
                        IwlanCarrierConfig.KEY_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT);
        assertEquals(IwlanCarrierConfig.DEFAULT_HANDOVER_TO_WWAN_RELEASE_DELAY_SECOND_INT, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDefaultConfig_KeyNotFound() {
        IwlanCarrierConfig.getDefaultConfig("non_existing_key");
    }
}
