package in.testpress.testpress.util;

import android.content.Context;
import android.util.Log;


import com.salesforce.marketingcloud.MCLogListener;
import com.salesforce.marketingcloud.MarketingCloudConfig;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions;
import com.salesforce.marketingcloud.sfmcsdk.BuildConfig;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkModuleConfig;
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogLevel;
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogListener;

import in.testpress.testpress.R;
import in.testpress.testpress.models.InstituteSettings;

public class SalesforceSdkInitializer {

    private final Context context;
    private InstituteSettings instituteSettings;

    public SalesforceSdkInitializer(Context context) {
        this.context = context;
    }

    public void initialize(InstituteSettings settings) {
        this.instituteSettings = settings;
        Log.d("TAG", "initialize1: "+instituteSettings.getBaseUrl());
        configureLogging();
        MarketingCloudConfig marketingCloudConfig = buildMarketingCloudConfig();
        SFMCSdkModuleConfig sdkModuleConfig = buildSFMCSdkModuleConfig(marketingCloudConfig);
        SFMCSdk.Companion.configure(context, sdkModuleConfig);
        Log.d("TAG", "initialize2: "+instituteSettings.getBaseUrl());
    }

    private void configureLogging() {
        if (BuildConfig.DEBUG) {
            MarketingCloudSdk.setLogLevel(MCLogListener.VERBOSE);
            MarketingCloudSdk.setLogListener(new MCLogListener.AndroidLogListener());
        }
    }

    private MarketingCloudConfig buildMarketingCloudConfig() {
        return MarketingCloudConfig.Companion.builder()
                .setApplicationId("b0562d0e-76e4-44a8-9725-ffe64392534b")
                .setAccessToken("orq2HdhvFgq4qJDdn7OH4J3U")
                .setSenderId("423776142021")
                .setMarketingCloudServerUrl("https://mczgg3rvb2rq3vslc0q19lzt-pp0.device.marketingcloudapis.com/")
                .setNotificationCustomizationOptions(
                        NotificationCustomizationOptions.create(R.drawable.ic_stat_notification)
                )
                .build(context);
    }

    private SFMCSdkModuleConfig buildSFMCSdkModuleConfig(MarketingCloudConfig marketingCloudConfig) {
        return SFMCSdkModuleConfig.Companion.build(builder -> {
            builder.setPushModuleConfig(marketingCloudConfig);
            return null;
        });
    }
}

