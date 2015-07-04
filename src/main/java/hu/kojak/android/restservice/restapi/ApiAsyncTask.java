package hu.kojak.android.restservice.restapi;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * This class has an exec() method, which calls the appropriate execute() method to be API
 * level safe!
 */
public abstract class ApiAsyncTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

  @TargetApi(11)
  @SuppressWarnings("unchecked")
  public final void exec(Params... params) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    } else {
      execute(params);
    }
  }
}
