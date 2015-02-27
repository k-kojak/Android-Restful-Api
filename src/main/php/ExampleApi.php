<?php

require_once('RestApi.php');

class ExampleClass {
  public $a;
  public $b;
  
  function __construct() {
    $this->a = array();
    $this->b = array();
  }
}
  
$exampleFunction = function($args, $data) {
  $klass = new ExampleClass();
  $klass->a = $args;
  
  foreach ($args as $arg) {
    for ($i = 0; $i < (int)$data->num; $i++) {
      $klass->b[] = "arg($arg) - $i";
    }
  }
  
  return $klass;
};

try {
  $API = new WebService($_REQUEST['request']);
  $API->post("/example/[0-9]{1,3}", $exampleFunction);
  echo $API->processAPI();
} catch (Exception $e) {
  echo json_encode(Array('error' => $e->getMessage()));
}

?>