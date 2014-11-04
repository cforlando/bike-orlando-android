package com.Orlando.opensource.bikeorlando.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.Orlando.opensource.bikeorlando.R;
import com.google.android.gms.maps.model.LatLng;

public class FragmentRack extends Fragment implements View.OnClickListener {

    private static final String EXTRA_LAT_LNG = "EXTRA_LAT_LNG";

    private ImageView imageViewRackClose;
    private TextView textViewRackType;
    private WebView webViewRackStreetView;

    private LatLng latLng;

    public static Fragment newInstance(LatLng latLng) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_LAT_LNG, latLng);

        FragmentRack fragment = new FragmentRack();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        latLng = getArguments().getParcelable(EXTRA_LAT_LNG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rack, container, false);

        imageViewRackClose = (ImageView) view.findViewById(R.id.rack_imageview_close);
        textViewRackType = (TextView) view.findViewById(R.id.rack_textview_type);
        webViewRackStreetView = (WebView) view.findViewById(R.id.rack_webview);

        textViewRackType.setText(latLng.latitude + ", " + latLng.longitude);

        imageViewRackClose.setOnClickListener(this);

        webViewRackStreetView.getSettings().setJavaScriptEnabled(true);
        webViewRackStreetView.getSettings().setLoadWithOverviewMode(true);
        webViewRackStreetView.getSettings().setUseWideViewPort(true);
        webViewRackStreetView.getSettings().setSupportZoom(false);
        webViewRackStreetView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String executionUrl = "javascript:initialize(" + latLng.latitude + ", " + latLng.longitude + ")";
                webViewRackStreetView.loadUrl(executionUrl);
            }
        });
        webViewRackStreetView.loadUrl(getString(R.string.streetview_urlpath));

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rack_imageview_close:
                getFragmentManager().beginTransaction().remove(this).commit();
                break;
        }
    }
}
