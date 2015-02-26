package hu.kojak.android.restservice.restapi;

import android.content.Context;

public interface IEncryptor {

  /**
   * Encrypts the given data and returns the encrypted key and data (RSA/AES encryption for example).
   * @param context
   * @param data the String data to encrypt
   * @return returns the encrypted data
   * @throws Exception
   */
  public EncryptedData encrypt(Context context, String data) throws Exception;

  /**
   * Tells if the handshake with the server is already done or not.
   * Should return true if handshake is totally done, not just partly.
   * @param context
   * @return true if handshake is done, false otherwise
   */
  public boolean isHandshakeDone(Context context);

  /**
   * Returns true if client is ready to send data encrypted to server.
   * If true that does not mean the handshake is fully done,
   * that just means the client has the public key to encrypt data and the service can encrypt data.
   * @param context
   * @return true if client can
   */
  public boolean isDataReadyToEncrypt(Context context);

  /**
   * Returns a WebService command to be executed in case of handshake is not done.
   * @param context
   * @return a WebService command to execute
   */
  public IRequest getHandshakeCommand(Context context);

  public static class EncryptedData {
    public final String encryptedKey;
    public final String encryptedData;

    public EncryptedData(String encryptedKey, String encryptedData) {
      this.encryptedKey = encryptedKey;
      this.encryptedData = encryptedData;
    }
  }

}
