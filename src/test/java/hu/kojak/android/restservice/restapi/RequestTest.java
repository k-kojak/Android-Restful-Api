package hu.kojak.android.restservice.restapi;


import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import retrofit.RestAdapter;
import retrofit.http.Body;
import retrofit.http.POST;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricGradleTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequestTest {

  interface Methods {
    @POST("/") String getRequest(@Body String body);
  }

  private static final MockWebServer server = new MockWebServer();

  private Application application;

  @Before
  public void setup() {
    application = RuntimeEnvironment.application;

    Robolectric.getBackgroundThreadScheduler().pause();
    Robolectric.getForegroundThreadScheduler().pause();
  }


  private void initAdapter() {
    RestAdapter adapter = new RestAdapter.Builder()
            .setEndpoint(server.url("/").toString())
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .build();
    AndroidRetrofitRestAdapter.putAdapter(Methods.class, adapter);

  }

  @Test
  public void a_testAdapterNotInitialized() throws Exception {
    TestRequest test = new TestRequest(application, false);
    try {
      test.execute();
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception should not have thrown here!");
    }

    Robolectric.getBackgroundThreadScheduler().runOneTask();
    Robolectric.getForegroundThreadScheduler().runOneTask();

    assertThat(test.exceptionString).isEqualTo("Missing adapter initialization: " + Methods.class.getName() + ". Call RestfulWebService.putAdapter() first to add this adapter.");

    // init adapter at the end of this test to ensure further tests run correctly!
    initAdapter();
  }


  @Test
  public void testFunctionCallSequenceWhenCancelled() throws Exception {

    TestRequest test = new TestRequest(application, true);
    try {
      test.execute();
    } catch (Exception e) {
      fail("Exception should not have thrown here!");
    }

    Robolectric.getBackgroundThreadScheduler().runOneTask();
    Robolectric.getForegroundThreadScheduler().runOneTask();

    assertThat(test.callSequence).isEqualTo("runonCancelledonFinally");
  }

  @Test
  public void testFunctionCallSequenceOnValidCall() throws Exception {

    server.enqueue(new MockResponse().setBody("\"body text\""));

    TestRequest test = new TestRequest(application, false);
    try {
      test.execute();
    } catch (Exception e) {
      fail("Exception should not have thrown here!");
    }

    Robolectric.getBackgroundThreadScheduler().runOneTask();
    Robolectric.getForegroundThreadScheduler().runOneTask();

    assertThat(test.callSequence).isEqualTo("runonPostExecuteonFinally");
  }

  @Test
  public void testRetrofitResultOnValidCall() throws Exception {

    server.enqueue(new MockResponse().setBody("\"body text\""));

    TestRequest test = new TestRequest(application, false);
    try {
      test.execute();
    } catch (Exception e) {
      fail("Exception should not have thrown here!");
    }

    Robolectric.getBackgroundThreadScheduler().runOneTask();
    Robolectric.getForegroundThreadScheduler().runOneTask();

    assertThat(test.result).isEqualTo("body text");
  }

  private static class TestRequest extends Request<Void, String, Methods> {

    private final boolean cancelOnRun;

    public TestRequest(Context context, boolean cancelOnRun) {
      super(context, Methods.class, "");
      this.cancelOnRun = cancelOnRun;
    }

    private String callSequence = "";
    private String exceptionString = null;
    private String result = null;

    @Override
    public String run(Context context, Methods restService) throws Exception {
      callSequence += "run";
      if (cancelOnRun) {
        cancel(true);
        return null;
      } else {
        return restService.getRequest("post data");
      }
    }

    @Override
    public void onPostExecute(Context context, String result) {
      this.result = result;
      callSequence += "onPostExecute";
    }

    @Override
    public void onCancelled(Context context) {
      callSequence += "onCancelled";
    }

    @Override
    public void onException(Context context, Exception exception) {
      exceptionString = exception.getMessage();
      callSequence += "onException";
    }

    @Override
    public void onFinally(Context context) {
      callSequence += "onFinally";
    }
  }



}
