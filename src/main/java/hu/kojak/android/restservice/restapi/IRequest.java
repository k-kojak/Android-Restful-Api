package hu.kojak.android.restservice.restapi;

import android.content.Context;

public abstract class IRequest<Return, RestInterface> {

  private final Class<RestInterface> mRestClass;
  private final String mQueryID;

  public IRequest(Class<RestInterface> restInterfaceClass, String queryID) {
    if (restInterfaceClass == null) {
      throw new RuntimeException("Parameter cannot be null");
    }
    mRestClass = restInterfaceClass;
    mQueryID = queryID;
  }

  final Class<RestInterface> getRestClass() {
    return mRestClass;
  }

  final String getQueryID() {
    return mQueryID;
  }

  /**
   * Runs the request with the given restService.
   * @return
   */
  public abstract Return run(Context context, RestInterface restService) throws Exception;

  /**
   * Called after a request finished successfully and no error occured.
   *
   * Runs on UI thread.
   * @param context
   * @param result
   */
  public abstract void onPostExecute(Context context, Return result);

  /**
   * Called when the request was cancelled.
   * The reason could be a cancellation of the running thread.
   *
   * Called on the UI thread.
   *
   */
  public abstract void onCancelled(Context context);

  /**
   * Called when request is rejected, which happens when a query is already running.
   * First you have to wait for the already running queary, then you are allowed to make this call.
   *
   * Called on UI thread.
   */
  public abstract void onReject(Context context);

  /**
   * Called if a retrofit error occured while executing request.
   * This method is called on the UI thread.
   * @param context
   * @param error
   */
  public abstract void onException(Context context, Exception error);

  /**
   * Called when query is not available to run immediately and added to waiting queue.
   * @param context
   */
  public abstract void onQueue(Context context);


}
