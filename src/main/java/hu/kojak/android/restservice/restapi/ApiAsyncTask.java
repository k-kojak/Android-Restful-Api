package hu.kojak.android.restservice.restapi;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.concurrent.RejectedExecutionException;

/**
 * This class has an exec() method, which calls the
 * execute() or executeOnExecutor() method depending on the API level.
 */
public abstract class ApiAsyncTask<Params, Progress, Result>
        extends AsyncTask<Params, Progress, Result> {

  @TargetApi(11)
  @SuppressWarnings("unchecked")
  /**
   * Returns true if execution started without problem, false if cannot start AsyncTask because
   * pool is full.
   */
  public final boolean exec(Params... params) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
      } else {
        execute(params);
      }
      return true;
    } catch (RejectedExecutionException e) {
      Log.d("retrofit-wrapper", "thread pool is full :(", e);
      return false;
    }
  }
}
