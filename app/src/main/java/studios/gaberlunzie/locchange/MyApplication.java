package studios.gaberlunzie.locchange;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}