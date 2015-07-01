package hu.kojak.android.restservice.restapi;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class SimpleRequest<Progress, Return, RestInterface>
        extends Request<Progress, Return, RestInterface> {

  public SimpleRequest(@NonNull Context context, @NonNull Class<RestInterface> restClass) {
    this(context, restClass, null);
  }

  public SimpleRequest(@NonNull Context context, @NonNull Class<RestInterface> restClass,
                       @Nullable String queryID) {
    super(context, restClass, queryID);
  }

  @Override
  public void onPostExecute(Context context, Return result) {
  }

  @Override
  public void onCancelled(Context context) {
  }

  @Override
  public void onException(Context context, Exception error) {
  }

  @Override
  public void onFinally(Context context) {
  }

}
