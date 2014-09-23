package com.Orlando.opensource.bikeorlando.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.RawRes;
import android.util.SparseArray;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.geojson.FeatureCollection;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tencent on 9/19/2014.
 */
public final class FeatureCollectionLoader {

    static SparseArray<FeatureCollectionLoader> featureCollectionLoaders = new SparseArray<>();

    final int resourceId;

    FeatureCollectionLoaderListener featureCollectionLoaderListener;
    FeatureCollection featureCollection;

    private FeatureCollectionLoader(FeatureCollectionLoaderListener listener, @RawRes int resourceId) {
        this.resourceId = resourceId;
    }

    public synchronized static FeatureCollectionLoader getInstance(FeatureCollectionLoaderListener
                                                                           featureCollectionLoaderListener,
                                                                   @RawRes int resourceId) {
        FeatureCollectionLoader loader = featureCollectionLoaders.get(resourceId);

        if (loader == null) {
            loader = new FeatureCollectionLoader(featureCollectionLoaderListener, resourceId);
            loader.setFeatureCollectionLoaderListener(featureCollectionLoaderListener);
            featureCollectionLoaders.put(resourceId, loader);

            new FeatureCollectionLoaderTask(loader).execute((Void) null);
        } else {
            loader.setFeatureCollectionLoaderListener(featureCollectionLoaderListener);

            if (loader.featureCollection != null)
                loader.setFeatureCollection(loader.featureCollection);
        }

        return loader;
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
