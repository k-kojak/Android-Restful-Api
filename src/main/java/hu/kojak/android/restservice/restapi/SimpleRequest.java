package hu.kojak.android.restservice.restapi;


import android.content.Context;

public abstract class SimpleRequest<T, U> extends IRequest<T, U> {

  public SimpleRequest(Class<U> restClass) {
    super(restClass);
  }

  @Override
  public void onPostExecute(Context context, T result) {
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
