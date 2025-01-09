package in.testpress.testpress.util;

import android.content.Context;
import android.util.Log;


import androidx.annotation.NonNull;

import com.salesforce.marketingcloud.MCLogListener;
import com.salesforce.marketingcloud.MarketingCloudConfig;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.notifications.NotificationCustomizationOptions;
import com.salesforce.marketingcloud.sfmcsdk.BuildConfig;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkModuleConfig;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdkReadyListener;
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogLevel;
import com.salesforce.marketingcloud.sfmcsdk.components.logging.LogListener;
import com.salesforce.marketingcloud.sfmcsdk.modules.push.PushModuleInterface;
import com.salesforce.marketingcloud.sfmcsdk.modules.push.PushModuleReadyListener;

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
        configureLogging();
        MarketingCloudConfig marketingCloudConfig = buildMarketingCloudConfig();
        SFMCSdkModuleConfig sdkModuleConfig = buildSFMCSdkModuleConfig(marketingCloudConfig);
        SFMCSdk.Companion.configure(context, sdkModuleConfig);
    }

    private void configureLogging() {
        if (BuildConfig.DEBUG) {
            MarketingCloudSdk.setLogLevel(MCLogListener.VERBOSE);
            MarketingCloudSdk.setLogListener(new MCLogListener.AndroidLogListener());
        }
    }

    private MarketingCloudConfig buildMarketingCloudConfig() {
        return MarketingCloudConfig.Companion.builder()
                .setApplicationId(instituteSettings.getSalesforceMcApplicationId())
                .setAccessToken(instituteSettings.getSalesforceMcAccessToken())
                .setSenderId(instituteSettings.getSalesforceFcmSenderId())
                .setMarketingCloudServerUrl(instituteSettings.getSalesforceMarketingCloudUrl())
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

    public static void notificationPermissionGranted() {
        SFMCSdk.Companion.requestSdk(new SFMCSdkReadyListener() {
            @Override
            public void ready(@NonNull SFMCSdk sfmcSdk) {
                sfmcSdk.mp(new PushModuleReadyListener() {
                    @Override
                    public void ready(@NonNull PushModuleInterface pushModuleInterface) {
                        pushModuleInterface.getPushMessageManager().enablePush();
                    }
                });
            }
        });
    }
}
