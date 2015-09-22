package hu.kojak.android.restservice.restapi;

import java.util.HashMap;

import retrofit.RestAdapter;

public class AndroidRetrofitRestAdapter {

  private static HashMap<String, RetrofitAdapter<?>> restAdapters = new HashMap<>();

  /**
   * Puts a new Retrofit adapter with given rest interface to later use.
   * Extend Request class to get an asynctask with the rest interface defined here.
   *
   * @param restInterface
   * @param adapter
   * @param <T>
   */
  public static<T> void putAdapter(Class<T> restInterface, RestAdapter adapter) {
    restAdapters.put(restInterface.getName(), new RetrofitAdapter<>(restInterface, adapter));
  }

  @SuppressWarnings("unchecked")
  protected synchronized static<T> T getService(Class<T> restClass) {
    RetrofitAdapter adapter = restAdapters.get(restClass.getName());
    if (adapter == null || !adapter.clazz.equals(restClass)) {
      throw new RuntimeException("Missing adapter initialization: " + restClass.getName()
              + ". Call RestfulWebService.putAdapter() first to add this adapter.");
    } else {
      return (T) adapter.restInterface;
    }
  }

  private static class RetrofitAdapter<T> {
    private final Class<T> clazz;
    private final T restInterface;

    public RetrofitAdapter(Class<T> clazz, RestAdapter adapter) {
      this.clazz = clazz;
      this.restInterface = adapter.create(clazz);
    }
  }

}
