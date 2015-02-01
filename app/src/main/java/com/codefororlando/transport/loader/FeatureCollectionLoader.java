/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.codefororlando.transport.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.util.SparseArray;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.geojson.FeatureCollection;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for asynchronously loading {@link FeatureCollection}s from disk. Once loaded, feature instances are retained
 * in memory for future use.
 *
 * @author Ian Thomas <toxicbakery@gmail.com>
 */
public final class FeatureCollectionLoader {

    private static final SparseArray<FeatureCollectionLoader> featureCollectionLoaders = new SparseArray<>();

    private final int resourceId;

    private FeatureCollectionLoaderListener featureCollectionLoaderListener;
    private FeatureCollection featureCollection;

    private FeatureCollectionLoader(@RawRes int resourceId) {
        this.resourceId = resourceId;
    }

    public synchronized static void getInstance(FeatureCollectionLoaderListener
                                                        featureCollectionLoaderListener,
                                                @RawRes int resourceId) {

        FeatureCollectionLoader loader = featureCollectionLoaders.get(resourceId);

        if (loader == null) {
            loader = new FeatureCollectionLoader(resourceId);
            loader.setFeatureCollectionLoaderListener(featureCollectionLoaderListener);
            featureCollectionLoaders.put(resourceId, loader);

            new FeatureCollectionLoaderTask(loader).execute((Void) null);
        } else {
            loader.setFeatureCollectionLoaderListener(featureCollectionLoaderListener);

            if (loader.featureCollection != null)
                loader.setFeatureCollection(loader.featureCollection);
        }
    }

    void setFeatureCollection(FeatureCollection featureCollection) {
        if (featureCollection == null)
            return;

        this.featureCollection = featureCollection;
        if (featureCollectionLoaderListener != null)
            featureCollectionLoaderListener.onFeatureCollectionLoaded(resourceId, featureCollection);
    }

    void setFeatureCollectionLoaderListener(FeatureCollectionLoaderListener featureCollectionLoaderListener) {
        this.featureCollectionLoaderListener = featureCollectionLoaderListener;
    }

    public static interface FeatureCollectionLoaderListener {

        @NonNull
        Context getContext();

        void onFeatureCollectionLoaded(@RawRes int resourceId, FeatureCollection featureCollection);

    }

    static class FeatureCollectionLoaderTask extends AsyncTask<Void, Void, FeatureCollection> {

        final FeatureCollectionLoader featureCollectionLoader;

        FeatureCollectionLoaderTask(FeatureCollectionLoader featureCollectionLoader) {
            this.featureCollectionLoader = featureCollectionLoader;
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {

            InputStream is = featureCollectionLoader.featureCollectionLoaderListener.getContext()
                    .getResources().openRawResource(featureCollectionLoader.resourceId);
            FeatureCollection featureCollection = null;
            try {
                featureCollection = new ObjectMapper().readValue(is, FeatureCollection.class);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return featureCollection;
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            if (featureCollection == null)
                return;

            featureCollectionLoader.setFeatureCollection(featureCollection);
        }

    }

}
