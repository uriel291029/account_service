package account.utils;

import java.util.Base64;

public class AuthorizationUtil {

  public static String getUsernameFromAuthHeader(String authorization){
    authorization = authorization.split(" ")[1];
    byte[] decodedBytes = Base64.getDecoder().decode(authorization);
    String decodedString = new String(decodedBytes);
    return decodedString.split(":")[0];
  }
}
