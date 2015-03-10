package hu.kojak.android.restservice.restapi;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayDeque;
import java.util.Queue;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.Converter;

public class RestfulWebService<T> {

  private static IRequest sCurrentQuery = null;
  private static Queue<QueryRunner> sRequestQueue = new ArrayDeque<>();
  private static RestfulWebService<?> INSTANCE = null;

  private final T mRestInterface;

  private RestfulWebService(String endPoint,
                            Class<T> restInterface,
                            Converter converter,
                            RequestInterceptor interceptor) {
    if (endPoint == null) {
      throw new RuntimeException("Endpoint cannot be null.");
    }
    if (restInterface == null) {
      throw new RuntimeException("RestInterface cannot be null.");
    }

    RestAdapter.Builder restAdapter = new RestAdapter.Builder()
            .setEndpoint(endPoint)
            .setLogLevel(RestAdapter.LogLevel.FULL);

    if (converter != null) {
      restAdapter.setConverter(converter);
    }
    if (interceptor != null) {
      restAdapter.setRequestInterceptor(interceptor);
    }

    mRestInterface = restAdapter.build().create(restInterface);
  }


  private static synchronized<T> void createInstance(String endPoint,
                                                     Class<T> restInterface,
                                                     Converter converter,
                                                     RequestInterceptor interceptor) {
    if (INSTANCE != null) {
      throw new RuntimeException("Instance already created, cannot create it more than once.");
    } else {
      INSTANCE = new RestfulWebService<>(endPoint, restInterface, converter, interceptor);
    }
  }

  public static class Builder<T> {
    private final String endPoint;
    private final Class<T> restInterface;

    private RequestInterceptor interceptor = null;
    private Converter converter = null;

    public Builder(String endPoint, Class<T> restInterface) {
      this.endPoint = endPoint;
      this.restInterface = restInterface;
    }

    public Builder setInterceptor(RequestInterceptor interceptor) {
      this.interceptor = interceptor;
      return this;
    }

    public Builder setConverter(Converter converter) {
      this.converter = converter;
      return this;
    }

    public void build() {
      RestfulWebService.createInstance(endPoint, restInterface, converter, interceptor);
    }

  }


  @SuppressWarnings("unchecked")
  private synchronized static<T> T getService(Class<T> restClass) {
    if (INSTANCE == null) {
      throw new RuntimeException("RestfulWebservice is not instantiated. Call Builder.build() first.");
    }
    try {
      return (T) INSTANCE.mRestInterface;
    } catch (ClassCastException e) {
      throw new RuntimeException("Incompatible rest interface, expected: "
              + restClass.getClass().getName()
              + ", but found: " + INSTANCE.mRestInterface.getClass().getName());
    }
  }

  /**
   * Runs a query against the server.
   * If addToQueue is set to true and a request is already running
   * then appends the request to waiting queue for later run.
   *
   * @param context context object
   * @param query the query to run against the server
   * @param addToQueue if true and a query is already running adds this query to run later, otherwise the query will
   *                   be rejected
   * @param <Return> the return type of the run thread
   */
  public static synchronized <Return, RestInterface> void runQuery(Context context,
                                                                   IRequest<Return, RestInterface> query,
                                                                   boolean addToQueue) {
    if (sCurrentQuery != null) {
      if (addToQueue) {
        sRequestQueue.add(new QueryRunner<>(context, query));
        query.onQueue(context);
      } else {
        query.onReject(context);
      }
    } else {
      sCurrentQuery = query;
      new QueryRunner<>(context, query).execute();
    }
  }

  /**
   * Runs the {@link #runQuery(android.content.Context, IRequest, boolean)}
   * method with false <code>addToQueue</code> parameter.
   *
   * @param context context object
   * @param query the query to run against the server
   * @param <Return> the return type of the run thread
   */
  public static synchronized <Return, RestInterface> void runQuery(Context context, IRequest<Return, RestInterface> query) {
    runQuery(context, query, false);
  }

  /**
   * Returns the String ID of the currently running query.
   * This function returns null in 2 cases: if there is no query running currently,
   * or if the query did not specified an id.
   * @return the ID of the currently running query.
   */
  public static String getCurrentQueryId() {
    if (sCurrentQuery != null) {
      return sCurrentQuery.getQueryID();
    } else {
      return null;
    }
  }

  private static class QueryRunner<Result, RestInterface> extends AsyncTask<Void, Void, Result> {

    private final Context mContext;
    private final IRequest<Result, RestInterface> mQuery;
    private Exception mException = null;

    public QueryRunner(Context context, IRequest<Result, RestInterface> query) {
      mContext = context;
      mQuery = query;
    }

    @Override
    protected Result doInBackground(Void... params) {
      try {
        return mQuery.run(mContext, getService(mQuery.getRestClass()));
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

    private void cancelled() {
      finished();
      if (mException != null) {
        mQuery.onException(mContext, mException);
      } else {
        mQuery.onCancelled(mContext);
      }

    }

    private static synchronized void finished() {
      sCurrentQuery = null;
      if (!sRequestQueue.isEmpty()) {
        QueryRunner<?, ?> runner = sRequestQueue.poll();
        sCurrentQuery = runner.mQuery;
        runner.execute();
      }
    }
  }

}
