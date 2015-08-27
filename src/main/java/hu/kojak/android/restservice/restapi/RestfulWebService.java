package hu.kojak.android.restservice.restapi;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.Converter;

public class RestfulWebService<T> {

  private static RestfulWebService<?> INSTANCE = null;

  private final T mRestInterface;

  private RestfulWebService(Builder<T> builder) {

    if (builder.endPoint == null) {
      throw new RuntimeException("Endpoint cannot be null.");
    }
    if (builder.restInterface == null) {
      throw new RuntimeException("RestInterface cannot be null.");
    }

    RestAdapter.Builder restAdapter = new RestAdapter.Builder()
            .setEndpoint(builder.endPoint);

    if (builder.debug) {
      restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
    }

    if (builder.converter != null) {
      restAdapter.setConverter(builder.converter);
    }
    if (builder.interceptor != null) {
      restAdapter.setRequestInterceptor(builder.interceptor);
    }

    mRestInterface = restAdapter.build().create(builder.restInterface);
  }


  private static synchronized<T> void createInstance(Builder<T> builder) {
    if (INSTANCE != null) {
      throw new RuntimeException("Instance already created, cannot create it more than once.");
    } else {
      INSTANCE = new RestfulWebService<>(builder);
    }
  }

  public static class Builder<T> {
    private final String endPoint;
    private final Class<T> restInterface;


    private RequestInterceptor interceptor = null;
    private Converter converter = null;
    private boolean debug = false;

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

    public Builder setDebug(boolean debug) {
      this.debug = debug;
      return this;
    }

    public void build() {
      RestfulWebService.createInstance(this);
    }

  }


  @SuppressWarnings("unchecked")
  protected synchronized static<T> T getService(Class<T> restClass) {
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

}
