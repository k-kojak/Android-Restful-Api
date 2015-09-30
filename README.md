Android Retrofit Wrapper with RESTful PHP
=============================
This library makes it easy to use [Retrofit][1] with Android `AsyncTask`s.  
In addition, you can find a simple PHP skeleton to get started with RESTful webservices on the server side.

#### Info
This library supports `Retrofit 1.9.0` only.  
When `Retrofit 2.0` becomes a stable, this library probably will be updated.

Usage - Android
---------------
Let's assume we have a Retrofit REST interface like this:
```java
public interface IRestInterface {
  @GET("/user/get/age/{user_id}")
  Integer getUserAge(@Path("user_id") int userId);
}
```
Probably you want to do this in your custom Application's onCreate method
```java
// build your Retrofit adapter as you would do it normally
RestAdapter adapter = new RestAdapter.Builder()
        .setEndpoint("http://example.com")
        .build();
        
// attach adapter to Android Retrofit Wrapper with a REST Interface
AndroidRetrofitRestAdapter.putAdapter(IRestInterface.class, adapter);
```
Create your custom request with extending `Request` class
```java
// the first 3 generic types stands for the AsynTask's generic types (Params, Progress, Result)
// and the last one is the Class type of the RestInterface you would like to get when using this AsyncTask
public class AgeRequest extends Request<Void, Void, Integer, IRestInterface> {

  public AgeRequest(@NonNull Context context) {
    super(context, IRestInterface.class);
  }

  // this function runs on the background thread, called from doInBackground()
  // so do the heavy work here, use your RestService
  @Override
  public Integer run(Context context, IRestInterface restService, Void... params) throws Exception {
    int someId = 1;
    Integer age = restService.getUserAge(someId);
    // ....
    return age;
  }

  @Override
  public void onPostExecute(Context context, Integer age) {
    // normal on post execute method
  }

  @Override
  public void onCancelled(Context context) {
    // called when asynctask is cancelled
  }

  @Override
  public void onException(Context context, Exception error) {
    // called when an exception happens in run() method
  }

  @Override
  public void onFinally(Context context) {
    // this function is called in every case: if cancelled, if finished normally or even if an exception occured
    // this function called as the last one
  }
}
```

Or alternatively you can extend the `SimpleRequest` class where some of the functions have a stub implementation,
so you only have to implement these ones:
```java
public abstract Result run(Context context, RestInterface restService, Params... params) throws Exception;
// and 
public abstract void onException(Context context, Exception exception);
```
Then just call your `AsyncTask` as you would normally do:
```java
  AgeRequest request = new AgeRequest(getContext());
  // this will call executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params) if API >= 11
  // and will call execute(params) if API < 11
  // and will return false if RejectedExecutionException was thrown inside
  request.exec();
```

Usage - PHP
---------------

See `src/main/php/ExampleApi.php` for an example usage.


[1]: http://square.github.com/otto/
