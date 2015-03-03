package hu.kojak.android.restservice.restapi;


import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.fail;
import static org.assertj.core.api.Assertions.*;


import retrofit.http.GET;

public class RestfulWebServiceBuilderTest {

  private static final String testEndpoint = "http://example.com";

  private static interface Example {
    @GET("/")
    void exampleMethod();
  }

  @Before
  public void resetStaticVarsWithReflection() {
    try {
      final Field field = RestfulWebService.class.getDeclaredField("INSTANCE");
      field.setAccessible(true);
      field.set(null, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void endpointIsNull() {
    try {
      RestfulWebService.Builder<Example> builder
              = new RestfulWebService.Builder<>(null, Example.class);
      builder.build();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).isEqualTo("Endpoint cannot be null.");
    }
  }

  @Test
  public void restInterfaceIsNull() {
    try {
      RestfulWebService.Builder<Example> builder
              = new RestfulWebService.Builder<>(testEndpoint, null);
      builder.build();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).isEqualTo("RestInterface cannot be null.");
    }
  }

  @Test
  public void bothRestInterfaceAndEndpointIsNull() {
    try {
      RestfulWebService.Builder<Example> builder
              = new RestfulWebService.Builder<>(null, null);
      builder.build();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).isEqualTo("Endpoint cannot be null.");
    }
  }

  @Test
  public void createWebserviceTwice() {
    try {
      RestfulWebService.Builder<Example> builder
              = new RestfulWebService.Builder<>(testEndpoint, Example.class);
      builder.build();
      builder.build();
    } catch (Exception e) {
      assertThat(e.getMessage()).isEqualTo("Instance already created, cannot create it more than once.");
    }
  }


}
