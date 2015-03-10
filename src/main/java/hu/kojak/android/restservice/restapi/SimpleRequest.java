package hu.kojak.android.restservice.restapi;


import android.content.Context;

public abstract class SimpleRequest<Return, RestInterface> extends IRequest<Return, RestInterface> {

  public SimpleRequest(Class<RestInterface> restClass) {
    this(restClass, null);
  }

  public SimpleRequest(Class<RestInterface> restClass, String queryID) {
    super(restClass, queryID);
  }

  @Override
  public void onPostExecute(Context context, Return result) {
  }

  @Override
  public void onCancelled(Context context) {
  }

  @Override
  public void onReject(Context context) {
  }

  @Override
  public void onException(Context context, Exception error) {
  }

  @Override
  public void onQueue(Context context) {
  }

}
