<?php

class WebService {

  /**
   * Property: method
   * The HTTP method this request was made in, either GET, POST, PUT or DELETE
   */
  protected $method = '';

  /**
   * Property: args
   * Any additional URI components after the endpoint and verb have been removed, in our
   * case, an integer ID for the resource. eg: /<endpoint>/<verb>/<arg0>/<arg1>
   * or /<endpoint>/<arg0>
   */
  protected $args = Array();

  /**
   * Property: file
   * Stores the input of the PUT request
   */
  protected $data = Null;
  
  private $get = array();
  private $post = array();
  private $put = array();
  private $delete = array();
  
  private $pureRequest = '';

  public function __construct($request) {
    header("Content-Type: application/json");
    $this->pureRequest = ltrim(rtrim($request, '/'), '/');
    
    $this->args = explode('/', $this->pureRequest);

    $this->method = $_SERVER['REQUEST_METHOD'];
    if ($this->method == 'POST' && array_key_exists('HTTP_X_HTTP_METHOD', $_SERVER)) {
      if ($_SERVER['HTTP_X_HTTP_METHOD'] == 'DELETE') {
        $this->method = 'DELETE';
      } else if ($_SERVER['HTTP_X_HTTP_METHOD'] == 'PUT') {
        $this->method = 'PUT';
      } else {
        throw new Exception("Unexpected Header");
      }
    }

    $this->data = json_decode(file_get_contents("php://input"));
    
    switch ($this->method) {
      case 'DELETE':
      case 'POST':
      case 'GET':
      case 'PUT':
        break;
      default:
        $this->_response('Invalid Method', 405);
        break;
    }
  }

  public function processAPI() {
    
    $container = $this->{strtolower($this->method)};
    $callback = $this->getFunctionToPattern($container);
    if ($callback === FALSE) {
      return $this->_response("Uri not found: $this->pureRequest", 404);
    } else {
      // From client side you need to pass an object with 1 member, named "jsonData"
      $jsonData = $this->data->jsonData;
      return $this->_response($callback($this->args, json_decode($jsonData)));
    }
  }

  private function _response($data, $status = 200) {
    header("HTTP/1.1 " . $status . " " . $this->_requestStatus($status));
    return json_encode($data);
  }

  private function getFunctionToPattern($container) {
    foreach ($container as $patt => $func) {
      $saved_pattern = explode('/', $patt);
      if (count($saved_pattern) === count($this->args)) {
        $ok = TRUE;
        for ($i = 0; $i < count($saved_pattern); $i++) {
          $act_patt = $saved_pattern[$i];
          $preg_patt = '/^' . $act_patt . '$/';
          if (preg_match($preg_patt, $this->args[$i]) === 0) {
            $ok = FALSE;
            break;
          }
        }
        if ($ok === TRUE) {
          return $func;
        }
      }
    }
    return FALSE;
  }
  
  private function _requestStatus($code) {
    $status = array(
        200 => 'OK',
        404 => 'Not Found',
        405 => 'Method Not Allowed',
        500 => 'Internal Server Error',
    );
    return ($status[$code]) ? $status[$code] : $status[500];
  }
  
  public function get($path, $func) {
    $this->putPathToVar($this->get, $path, $func);
  }
  
  public function post($path, $func) {
    $this->putPathToVar($this->post, $path, $func);
  }
  
  public function put($path, $func) {
    $this->putPathToVar($this->put, $path, $func);
  }
  
  public function delete($path, $func) {
    $this->putPathToVar($this->delete, $path, $func);
  }
  
  private function putPathToVar(&$container, $path, $func) {
    $container[strtolower(ltrim(rtrim($path, '/'), '/'))] = $func;
  }

}

?>