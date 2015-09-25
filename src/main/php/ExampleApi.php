<?php

require_once('RestApi.php');

/**
 * You will return this as a response.
 */
class ExampleClass {
  public $a;
  public $b;
  
}
  
/**
 * $args is technically the result of explode('/', $_REQUEST['request'])
 * $data is the Retrofit @Body Object or NULL if not sent
 */
$handlerFunction = function($args, $data) {
  $klass = new ExampleClass();
  
  // establish result...
  $klass->a = $args;
  $klass->b = print_r($data, true);
  
  return $klass;
};

try {
  // create new WebService with passing REQUEST
  $API = new WebService($_REQUEST['request']);
  
  // add GET handler for the given path, you can define regular expressions as well
  $API->get("/user/get/age/[0-9]{1,8}", $handlerFunction);
  
  // add POST handler 
  $API->post("/user/set/name", $handlerFunction);
  
  // add PUT or DELETE...
  $API->put("/some/url/for/put", $handlerFunction);
  $API->delete("/some/url/for/delete", $handlerFunction);
  
  echo $API->processAPI();
} catch (Exception $e) {
  echo json_encode(Array('error' => $e->getMessage()));
}

?>