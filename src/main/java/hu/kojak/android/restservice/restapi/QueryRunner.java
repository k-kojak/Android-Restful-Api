package hu.kojak.android.restservice.restapi;

import android.content.Context;
import android.os.AsyncTask;

public class QueryRunner<Progress, Result, RestInterface> extends AsyncTask<Void, Progress, Result> {

  private final Context mContext;
  private final IRequest<Progress, Result, RestInterface> mQuery;
  private Exception mException = null;

  public QueryRunner(Context context, IRequest<Progress, Result, RestInterface> query) {
    mContext = context;
    mQuery = query;
    query.setProgressDelegate(new ProgressDelegate<Progress>() {
      @Override
      public void publish(Progress progress) {
        publishProgress(progress);
      }
    });
  }

  @Override
  protected Result doInBackground(Void... params) {
    try {
      return mQuery.run(mContext, RestfulWebService.getService(mQuery.getRestClass()));
    } catch (Exception error) {
      mException = error;
      cancel(true);
    }
    return null;
  }

  @Override
  protected void onPostExecute(Result result) {
    finished();
    mQuery.onPostExecute(mContext, result);
  }

  @Override
  protected void onCancelled() {
    cancelled();
  }

  @Override
  protected void onCancelled(Result result) {
    cancelled();
  }

  @Override
  protected void onProgressUpdate(Progress... values) {
    mQuery.onProgressUpdate(values[0]);
  }

  private void cancelled() {
    finished();
    if (mException != null) {
      mQuery.onException(mContext, mException);
    } else {
      mQuery.onCancelled(mContext);
    }

  }

  private static synchronized void finished() {
    RestfulWebService.sCurrentQuery = null;
    if (!RestfulWebService.sRequestQueue.isEmpty()) {
      QueryRunner<?, ?, ?> runner = RestfulWebService.sRequestQueue.poll();
      RestfulWebService.sCurrentQuery = runner.mQuery;
      runner.execute();
    }
  }

  protected interface ProgressDelegate<Progress> {
    void publish(Progress progress);
  }
}
