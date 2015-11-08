package net.zno_ua.app.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import net.zno_ua.app.BuildConfig;
import net.zno_ua.app.R;
import net.zno_ua.app.ZNOApplication;

public class AdFragment extends Fragment {

    private Ad ad;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View v = inflater.inflate(R.layout.fragment_ad, container, false);
        ad = new Ad((RelativeLayout) v);
        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (ad != null) {
            ad.restart();
        }
    }

    @Override
    public void onPause() {
        if (ad != null) {
            ad.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ad != null) {
            ad.resume();
        }
    }

    @Override
    public void onDestroy() {
        if (ad != null) {
            ad.destroy();
        }
        super.onDestroy();
    }

    private class Ad {

        private final long AD_RECREATE_DELAY = 30000;
        private final String AD_UNIT_ID;
        private final AdRequest AD_REQUEST;
        private RelativeLayout parent;
        private AdView adView;
        private Handler handler = new Handler();

        public Ad(RelativeLayout parent) {
            this.parent = parent;

            if (BuildConfig.DEBUG) {
                AD_REQUEST = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();
                AD_UNIT_ID = getString(R.string.test_banner_ad_unit_id);
            } else {
                AD_REQUEST = new AdRequest.Builder().build();
                AD_UNIT_ID = getString(R.string.smart_banner_ad_unit_id);
            }

            recreate();
        }

        public void restart() {
            destroy();
            recreate();
        }

        public void pause() {
            adView.pause();
        }

        public void resume() {
            adView.resume();
        }

        public void destroy() {
            handler.removeCallbacksAndMessages(null);
            adView.destroy();
            parent.removeView(adView);
        }

        private void recreate() {
            adView = new AdView(getActivity());
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restart();
                        }
                    }, AD_RECREATE_DELAY);

                }
            });
            adView.setVisibility(View.GONE);
            adView.setAdUnitId(AD_UNIT_ID);
            adView.setAdSize(AdSize.SMART_BANNER);
            parent.addView(adView);
            adView.loadAd(AD_REQUEST);
        }

    }

}
