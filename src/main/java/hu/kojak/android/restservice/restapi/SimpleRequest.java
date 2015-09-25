package hu.kojak.android.restservice.restapi;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class SimpleRequest<Params, Progress, Return, RestInterface>
        extends Request<Params, Progress, Return, RestInterface> {

  public SimpleRequest(@NonNull Context context, @NonNull Class<RestInterface> restClass) {
    super(context, restClass);
  }

  @Override
  public void onPostExecute(Context context, Return result) {
  }

  @Override
  public void onCancelled(Context context) {
  }

  @Override
  public void onFinally(Context context) {
  }

}
