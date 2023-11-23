/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.iwlan.epdg;

import android.net.ipsec.ike.SaProposal;
import android.util.Log;
import android.util.Pair;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

abstract class EpdgSaProposal {
    private static final String TAG = EpdgSaProposal.class.getSimpleName();
    private static final Set<Integer> VALID_DH_GROUPS;
    private static final Set<Integer> VALID_KEY_LENGTHS;
    protected static final Set<Integer> VALID_PRF_ALGOS;
    private static final Set<Integer> VALID_INTEGRITY_ALGOS;
    private static final Set<Integer> VALID_ENCRYPTION_ALGOS;
    private static final Set<Integer> VALID_AEAD_ALGOS;

    private static final String CONFIG_TYPE_DH_GROUP = "dh group";
    private static final String CONFIG_TYPE_KEY_LEN = "algorithm key length";
    protected static final String CONFIG_TYPE_PRF_ALGO = "prf algorithm";
    private static final String CONFIG_TYPE_INTEGRITY_ALGO = "integrity algorithm";
    private static final String CONFIG_TYPE_ENCRYPT_ALGO = "encryption algorithm";
    private static final String CONFIG_TYPE_AEAD_ALGO = "AEAD algorithm";

    static {
        VALID_DH_GROUPS =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                List.of(
                                        SaProposal.DH_GROUP_1024_BIT_MODP,
                                        SaProposal.DH_GROUP_1536_BIT_MODP,
                                        SaProposal.DH_GROUP_2048_BIT_MODP,
                                        SaProposal.DH_GROUP_3072_BIT_MODP,
                                        SaProposal.DH_GROUP_4096_BIT_MODP)));

        VALID_KEY_LENGTHS =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                List.of(
                                        SaProposal.KEY_LEN_AES_128,
                                        SaProposal.KEY_LEN_AES_192,
                                        SaProposal.KEY_LEN_AES_256)));

        VALID_ENCRYPTION_ALGOS =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                List.of(
                                        SaProposal.ENCRYPTION_ALGORITHM_AES_CBC,
                                        SaProposal.ENCRYPTION_ALGORITHM_AES_CTR)));

        VALID_INTEGRITY_ALGOS =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                List.of(
                                        SaProposal.INTEGRITY_ALGORITHM_HMAC_SHA1_96,
                                        SaProposal.INTEGRITY_ALGORITHM_AES_XCBC_96,
                                        SaProposal.INTEGRITY_ALGORITHM_HMAC_SHA2_256_128,
                                        SaProposal.INTEGRITY_ALGORITHM_HMAC_SHA2_384_192,
                                        SaProposal.INTEGRITY_ALGORITHM_HMAC_SHA2_512_256)));

        VALID_AEAD_ALGOS =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                List.of(
                                        SaProposal.ENCRYPTION_ALGORITHM_AES_GCM_8,
                                        SaProposal.ENCRYPTION_ALGORITHM_AES_GCM_12,
                                        SaProposal.ENCRYPTION_ALGORITHM_AES_GCM_16)));

        VALID_PRF_ALGOS =
                Collections.unmodifiableSet(
                        new LinkedHashSet<Integer>(
                                List.of(
                                        SaProposal.PSEUDORANDOM_FUNCTION_HMAC_SHA1,
                                        SaProposal.PSEUDORANDOM_FUNCTION_AES128_XCBC,
                                        SaProposal.PSEUDORANDOM_FUNCTION_SHA2_256,
                                        SaProposal.PSEUDORANDOM_FUNCTION_SHA2_384,
                                        SaProposal.PSEUDORANDOM_FUNCTION_SHA2_512)));
    }

    protected final LinkedHashSet<Integer> mProposedDhGroups = new LinkedHashSet<>();
    protected final LinkedHashSet<Integer> mProposedIntegrityAlgos = new LinkedHashSet<>();
    protected final LinkedHashSet<Pair<Integer, Integer>> mProposedEncryptAlgos =
            new LinkedHashSet<>();
    protected final LinkedHashSet<Pair<Integer, Integer>> mProposedAeadAlgos =
            new LinkedHashSet<>();

    /**
     * Add proposed DH groups by the carrier.
     *
     * @param dhGroups proposed DH groups
     */
    public void addProposedDhGroups(int[] dhGroups) {
        for (int dhGroup : dhGroups) {
            if (validateConfig(dhGroup, VALID_DH_GROUPS, CONFIG_TYPE_DH_GROUP)) {
                mProposedDhGroups.add(dhGroup);
            }
        }
    }

    /**
     * Add proposed integrity algorithms by the carrier.
     *
     * @param integrityAlgos proposed integrity algorithms
     */
    public void addProposedIntegrityAlgorithm(int[] integrityAlgos) {
        for (int integrityAlgo : integrityAlgos) {
            if (validateConfig(integrityAlgo, VALID_INTEGRITY_ALGOS, CONFIG_TYPE_INTEGRITY_ALGO)) {
                mProposedIntegrityAlgos.add(integrityAlgo);
            }
        }
    }

    /**
     * Add proposed encryption algorithms and respective key lengths by the carrier.
     *
     * @param encryptionAlgo proposed encryption algorithm
     * @param keyLens proposed key lengths for the encryption algorithm
     */
    public void addProposedEncryptionAlgorithm(int encryptionAlgo, int[] keyLens) {
        if (validateConfig(encryptionAlgo, VALID_ENCRYPTION_ALGOS, CONFIG_TYPE_ENCRYPT_ALGO)) {
            for (int keyLen : keyLens) {
                if (validateConfig(keyLen, VALID_KEY_LENGTHS, CONFIG_TYPE_KEY_LEN)) {
                    mProposedEncryptAlgos.add(new Pair<Integer, Integer>(encryptionAlgo, keyLen));
                }
            }
        }
    }

    /**
     * Add proposed AEAD algorithms and respective key lengths by the carrier.
     *
     * @param aeadAlgo proposed AEAD algorithm
     * @param keyLens proposed key lengths for the encryption algorithm
     */
    public void addProposedAeadAlgorithm(int aeadAlgo, int[] keyLens) {
        if (validateConfig(aeadAlgo, VALID_AEAD_ALGOS, CONFIG_TYPE_AEAD_ALGO)) {
            for (int keyLen : keyLens) {
                if (validateConfig(keyLen, VALID_KEY_LENGTHS, CONFIG_TYPE_KEY_LEN)) {
                    mProposedAeadAlgos.add(new Pair<Integer, Integer>(aeadAlgo, keyLen));
                }
            }
        }
    }

    protected int[] getDhGroups() {
        return mProposedDhGroups.stream().mapToInt(Integer::intValue).toArray();
    }

    protected int[] getSupportedDhGroups() {
        return VALID_DH_GROUPS.stream().mapToInt(Integer::intValue).toArray();
    }

    protected int[] getIntegrityAlgos() {
        return mProposedIntegrityAlgos.stream().mapToInt(Integer::intValue).toArray();
    }

    protected int[] getSupportedIntegrityAlgos() {
        return VALID_INTEGRITY_ALGOS.stream().mapToInt(Integer::intValue).toArray();
    }

    protected Pair<Integer, Integer>[] getEncryptionAlgos() {
        return mProposedEncryptAlgos.toArray(new Pair[mProposedEncryptAlgos.size()]);
    }

    protected Pair<Integer, Integer>[] getSupportedEncryptionAlgos() {
        Pair<Integer, Integer>[] encrAlgos =
                new Pair[VALID_ENCRYPTION_ALGOS.size() * VALID_KEY_LENGTHS.size()];
        int index = 0;
        for (int algo : VALID_ENCRYPTION_ALGOS) {
            for (int len : VALID_KEY_LENGTHS) {
                encrAlgos[index++] = new Pair(algo, len);
            }
        }

        return encrAlgos;
    }

    protected Pair<Integer, Integer>[] getAeadAlgos() {
        return mProposedAeadAlgos.toArray(new Pair[mProposedAeadAlgos.size()]);
    }

    protected Pair<Integer, Integer>[] getSupportedAeadAlgos() {
        Pair<Integer, Integer>[] aeadAlgos =
                new Pair[VALID_AEAD_ALGOS.size() * VALID_KEY_LENGTHS.size()];
        int index = 0;
        for (int algo : VALID_AEAD_ALGOS) {
            for (int len : VALID_KEY_LENGTHS) {
                aeadAlgos[index++] = new Pair(algo, len);
            }
        }

        return aeadAlgos;
    }

    protected static boolean validateConfig(
            int config, Set<Integer> validConfigValues, String configType) {
        if (validConfigValues.contains(config)) {
            return true;
        }

        Log.e(TAG, "Invalid config value for " + configType + ":" + config);
        return false;
    }
}
