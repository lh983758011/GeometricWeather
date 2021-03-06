package wangdaye.com.geometricweather.basic;

import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.data.entity.model.Location;
import wangdaye.com.geometricweather.data.entity.model.weather.Weather;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.helpter.DatabaseHelper;
import wangdaye.com.geometricweather.utils.helpter.WeatherHelper;

/**
 * Geometric widget configuration activity.
 * */

public abstract class GeoWidgetConfigActivity extends GeoActivity
        implements WeatherHelper.OnRequestWeatherListener {
    // data
    private Location locationNow;
    private WeatherHelper weatherHelper;
    private boolean fahrenheit;

    /** <br> life cycle. */

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();

            if (locationNow.isLocal()) {
                if (locationNow.isUsable()) {
                    weatherHelper.requestWeather(this, locationNow, this);
                } else {
                    weatherHelper.requestWeather(this, Location.buildDefaultLocation(), this);
                }
            } else {
                weatherHelper.requestWeather(this, locationNow, this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weatherHelper.cancel();
    }

    public void initData() {
        this.locationNow = DatabaseHelper.getInstance(this).readLocationList().get(0);
        this.weatherHelper = new WeatherHelper();
        this.fahrenheit = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.key_fahrenheit), false);
    }

    public abstract void initWidget();

    public abstract void refreshWidgetView(Weather weather);

    public Location getLocationNow() {
        return locationNow;
    }

    public boolean isFahrenheit() {
        return fahrenheit;
    }

    /** <br> interface. */

    // on request realTimeWeather listener.

    @Override
    public void requestWeatherSuccess(Weather weather, Location requestLocation) {
        if (weather == null) {
            requestWeatherFailed(requestLocation);
        } else {
            refreshWidgetView(weather);
            DatabaseHelper.getInstance(this).writeWeather(requestLocation, weather);
            DatabaseHelper.getInstance(this).writeHistory(weather);
        }
    }

    @Override
    public void requestWeatherFailed(Location requestLocation) {
        Weather weather = DatabaseHelper.getInstance(this).readWeather(requestLocation);
        refreshWidgetView(weather);
        SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
    }
}
