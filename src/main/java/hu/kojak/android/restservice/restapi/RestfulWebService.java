package hu.kojak.android.restservice.restapi;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayDeque;
import java.util.Queue;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

public class RestfulWebService {

  private static String ENDPOINT = null;

  private static IEncryptor sEncryptor = null;

  private static IRequest sCurrentQuery = null;

  private static Queue<QueryRunner> sRequestQueue = new ArrayDeque<>();


  private static class ServiceHolder {

    private static final RequestInterceptor interceptor = new RequestInterceptor() {
      @Override
      public void intercept(RequestFacade request) {
        request.addHeader("Charset", "UTF-8");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept-Encoding", "gzip");
      }
    };

    private static Object INSTANCE = null;

    private synchronized static void createInstance(Class restClass) {
      if (ENDPOINT == null) {
        throw new RuntimeException("Endpoint is not set. Call setEndpoint with a nonNull value before instance creation.");
      }
      if (INSTANCE == null) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setRequestInterceptor(interceptor)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        INSTANCE = restAdapter.create(restClass);
      }
    }

    private ServiceHolder() {
    }

  }

  private RestfulWebService() {}

  /**
   * Sets an encryptor for data encryption and decryption.
   * @param encryptor the encryptor to set
   */
  public static void setEncryptor(IEncryptor encryptor) {
    sEncryptor = encryptor;
  }

  /**
   * Returns the encryptor which is set to the WebService, null if not set.
   * @return
   */
  public static IEncryptor getEncryptor() {
    return sEncryptor;
  }

  /**
   * Sets the endpoint of the RestApi calls.
   * @param endPoint the endpoint uri
   */
  public static void setEndpoint(String endPoint) {
    ENDPOINT = endPoint;
  }

  @SuppressWarnings("unchecked")
  private synchronized static<T> T getService(Class<T> restClass) {
    if (ServiceHolder.INSTANCE == null) {
      ServiceHolder.createInstance(restClass);
    } else if (!ServiceHolder.INSTANCE.getClass().equals(restClass)) {
      throw new RuntimeException("Rest api interface changed, but must be the same through the whole app lifecycle!");
    }
    return (T) ServiceHolder.INSTANCE;
  }

  /**
   * Runs a query against the server.
   * If addToQueue is set to true and a request is already running
   * then appends the request to waiting queue for later run.
   *
   * @param context
   * @param query the query to run against the server
   * @param addToQueue if true and a query is already running adds this query to run later, otherwise the query will
   *                   be rejected
   * @param <Return> the return type of the run thread
   */
  public static synchronized <Return, RestInterface> void runQuery(Context context, IRequest<Return, RestInterface> query, boolean addToQueue) {
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
   * @param context
   * @param query the query to run against the server
   * @param <Return> the return type of the run thread
   */
  public static synchronized <Return, RestInterface> void runQuery(Context context, IRequest<Return, RestInterface> query) {
    runQuery(context, query, false);
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
